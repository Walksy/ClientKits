package walksy.clientkits.kit;

import main.walksy.lib.api.config.value.ItemInventory;
import main.walksy.lib.api.config.value.ItemSpec;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import walksy.clientkits.config.ClientKitsConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class LegacyKits {
    private static final Logger LOGGER = LoggerFactory.getLogger("ClientKits");
    private static final String EXT = ".json";
    private static final int ARMOR_REMOTE_START = 100;
    private static final int OFFHAND_REMOTE = 150;

    private LegacyKits() {
    }

    public static void migrate() {
        final Path root = FabricLoader.getInstance().getConfigDir().resolve("clientkits").resolve("kits");
        if (!Files.isDirectory(root)) {
            return;
        }
        final List<Path> files;
        try (final Stream<Path> listing = Files.list(root)) {
            files = listing.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(EXT)).sorted().toList();
        } catch (final IOException e) {
            LOGGER.error("Could not read old kits from " + root, e);
            return;
        }
        if (ItemSpec.registryAccess() == null) {
            LOGGER.warn("Item registries are not ready; old kits will be imported on the next launch");
            return;
        }
        final Map<String, ItemInventory> kits = new LinkedHashMap<>();
        for (final Path file : files) {
            final String name = file.getFileName().toString();
            try {
                kits.put(name.substring(0, name.length() - EXT.length()), read(Files.readString(file)));
            } catch (final IOException e) {
                LOGGER.error("Could not read old kit " + file + "; leaving the old kits where they are", e);
                return;
            }
        }
        kits.forEach((name, inventory) -> ClientKitsConfig.putKit(ClientKitsConfig.uniqueKitName(name), inventory));
        try {
            Files.move(root, root.resolveSibling("kits.migrated"));
        } catch (final IOException e) {
            LOGGER.error("Imported " + kits.size() + " old kits but could not rename " + root + "; they may be imported again", e);
            return;
        }
        LOGGER.info("Imported " + kits.size() + " old kits into the new config");
    }

    private static ItemInventory read(final String text) {
        final ItemInventory inventory = new ItemInventory();
        final String list = topLevelValue(text, "inv");
        if (list == null) {
            return inventory;
        }
        for (final String entry : elements(list)) {
            final String slotText = topLevelValue(entry, "Slot");
            final int slot = slotText == null ? -1 : toLocalSlot(parseByte(slotText));
            if (!ItemInventory.isValidSlot(slot)) {
                continue;
            }
            final ItemSpec spec = new ItemSpec("minecraft:stone");
            final String error = spec.applySnbt(entry);
            if (error == null) {
                inventory.set(slot, spec);
            } else {
                LOGGER.warn("Skipping an item in an old kit: " + error);
            }
        }
        return inventory;
    }

    private static int toLocalSlot(final int remote) {
        if (remote >= ARMOR_REMOTE_START && remote < OFFHAND_REMOTE) {
            return ItemInventory.ARMOR_START + remote - ARMOR_REMOTE_START;
        }
        return remote >= OFFHAND_REMOTE ? ItemInventory.OFFHAND : remote;
    }

    private static int parseByte(final String text) {
        final String digits = text.trim().replaceAll("[bBsSlL]$", "");
        try {
            return Integer.parseInt(digits) & 0xFF;
        } catch (final NumberFormatException e) {
            return -1;
        }
    }

    private static @Nullable String topLevelValue(final String compound, final String key) {
        final int open = compound.indexOf('{');
        if (open < 0) {
            return null;
        }
        int i = open + 1;
        while (i < compound.length()) {
            i = skipSpace(compound, i);
            if (i >= compound.length() || compound.charAt(i) == '}') {
                return null;
            }
            final int keyEnd = compound.charAt(i) == '"' || compound.charAt(i) == '\'' ? endOfQuoted(compound, i) : compound.indexOf(':', i);
            if (keyEnd < 0) {
                return null;
            }
            final String found = unquote(compound.substring(i, keyEnd).trim());
            final int colon = compound.indexOf(':', keyEnd);
            if (colon < 0) {
                return null;
            }
            final int valueStart = skipSpace(compound, colon + 1);
            final int valueEnd = endOfValue(compound, valueStart);
            if (found.equals(key)) {
                return compound.substring(valueStart, valueEnd).trim();
            }
            i = skipSpace(compound, valueEnd);
            if (i < compound.length() && compound.charAt(i) == ',') {
                i++;
            }
        }
        return null;
    }

    private static List<String> elements(final String list) {
        final List<String> out = new ArrayList<>();
        int i = skipSpace(list, list.indexOf('[') + 1);
        while (i < list.length() && list.charAt(i) != ']') {
            final int end = endOfValue(list, i);
            out.add(list.substring(i, end).trim());
            i = skipSpace(list, end);
            if (i < list.length() && list.charAt(i) == ',') {
                i = skipSpace(list, i + 1);
            }
        }
        return out;
    }

    private static int endOfValue(final String text, final int start) {
        int depth = 0;
        int i = start;
        while (i < text.length()) {
            final char c = text.charAt(i);
            if (c == '"' || c == '\'') {
                i = endOfQuoted(text, i);
                continue;
            }
            if (c == '{' || c == '[') {
                depth++;
            } else if (c == '}' || c == ']') {
                if (depth == 0) {
                    return i;
                }
                depth--;
                if (depth == 0) {
                    return i + 1;
                }
            } else if (c == ',' && depth == 0) {
                return i;
            }
            i++;
        }
        return i;
    }

    private static int endOfQuoted(final String text, final int start) {
        final char quote = text.charAt(start);
        int i = start + 1;
        while (i < text.length()) {
            final char c = text.charAt(i);
            if (c == '\\') {
                i += 2;
                continue;
            }
            if (c == quote) {
                return i + 1;
            }
            i++;
        }
        return i;
    }

    private static int skipSpace(final String text, final int start) {
        int i = start;
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        return i;
    }

    private static String unquote(final String key) {
        if (key.length() >= 2 && (key.charAt(0) == '"' || key.charAt(0) == '\'') && key.charAt(key.length() - 1) == key.charAt(0)) {
            return key.substring(1, key.length() - 1);
        }
        return key;
    }
}
