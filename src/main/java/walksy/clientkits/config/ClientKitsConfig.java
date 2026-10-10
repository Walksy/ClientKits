package walksy.clientkits.config;

import com.mojang.blaze3d.platform.InputConstants;
import main.walksy.lib.api.WalksyLibApi;
import main.walksy.lib.api.config.Config;
import main.walksy.lib.core.config.ConfigRegistry;
import main.walksy.lib.api.config.option.*;
import main.walksy.lib.api.config.value.ItemInventory;
import main.walksy.lib.api.config.value.WLKeyMapping;
import main.walksy.lib.api.config.PathUtils;
import walksy.clientkits.kit.KitCodes;
import walksy.clientkits.manager.KitManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClientKitsConfig implements WalksyLibApi {
    private static final String KEYBIND_CATEGORY = "Client Kits";
    private static List<String> kitNames = new ArrayList<>();
    private static final Map<String, ItemInventory> KITS = new LinkedHashMap<>();
    private static final Map<String, WLKeyMapping> KIT_KEYBINDS = new LinkedHashMap<>();
    private static List<String> enderKitNames = new ArrayList<>();
    private static final Map<String, ItemInventory> ENDER_KITS = new LinkedHashMap<>();
    private static boolean autoCreative = true;
    private static boolean glassEnderButtons = false;

    public static Config config() {
        return ConfigRegistry.get(ClientKitsConfig.class);
    }

    public static void save() {
        config().save();
    }

    public static List<String> kitNames() {
        return List.copyOf(kitNames);
    }

    public static boolean hasKit(final String name) {
        return kitNames.contains(name);
    }

    public static ItemInventory kit(final String name) {
        return KITS.computeIfAbsent(name, ignored -> new ItemInventory());
    }

    public static WLKeyMapping keybindFor(final String name) {
        return KIT_KEYBINDS.computeIfAbsent(name, n -> WLKeyMapping.createLocal(n, InputConstants.UNKNOWN.getValue(), KEYBIND_CATEGORY));
    }

    public static void putKit(final String name, final ItemInventory inventory) {
        if (!kitNames.contains(name)) {
            kitNames.add(name);
        }
        KITS.put(name, inventory == null ? new ItemInventory() : inventory);
        keybindFor(name);
        save();
    }

    public static void removeKit(final String name) {
        kitNames.remove(name);
        KITS.remove(name);
        final WLKeyMapping keybind = KIT_KEYBINDS.remove(name);
        if (keybind != null) {
            keybind.unregister();
        }
        save();
    }

    public static List<String> enderKitNames() {
        return List.copyOf(enderKitNames);
    }

    public static boolean hasEnderKit(final String name) {
        return enderKitNames.contains(name);
    }

    public static ItemInventory enderKit(final String name) {
        return ENDER_KITS.computeIfAbsent(name, ignored -> new ItemInventory());
    }

    public static void putEnderKit(final String name, final ItemInventory inventory) {
        if (!enderKitNames.contains(name)) {
            enderKitNames.add(name);
        }
        ENDER_KITS.put(name, inventory == null ? new ItemInventory() : inventory);
        save();
    }

    public static String uniqueKitName(final String requested) {
        return uniqueName(requested, "Kit", kitNames);
    }

    public static String uniqueEnderKitName(final String requested) {
        return uniqueName(requested, "EnderKit", enderKitNames);
    }

    private static String uniqueName(final String requested, final String fallback, final List<String> taken) {
        final String cleaned = requested == null ? "" : requested.trim().replaceAll("\\s+", "_");
        final String stem = cleaned.isEmpty() ? fallback : cleaned;
        if (!taken.contains(stem)) {
            return stem;
        }
        for (int n = 2; ; n++) {
            final String candidate = stem + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }

    public static String freeEnderKitName(final String stem) {
        for (int n = 1; ; n++) {
            final String candidate = stem + n;
            if (!hasEnderKit(candidate)) {
                return candidate;
            }
        }
    }

    public static void removeEnderKit(final String name) {
        enderKitNames.remove(name);
        ENDER_KITS.remove(name);
        save();
    }

    private static void renameKeybind(final String from, final String to) {
        final WLKeyMapping old = KIT_KEYBINDS.remove(from);
        if (old == null) {
            return;
        }
        final WLKeyMapping renamed = WLKeyMapping.createLocal(to, old.currentKey().getValue(), KEYBIND_CATEGORY);
        old.unregister();
        KIT_KEYBINDS.put(to, renamed);
    }

    public static boolean autoCreative() {
        return autoCreative;
    }

    public static boolean glassEnderButtons() {
        return glassEnderButtons;
    }

    @Override
    public Config define() {
        return Config.builder()
            .path(PathUtils.ofConfigDir("clientkits"))
            .category(Category.builder("Kits")
                .group(OptionGroup.builder("General")
                    .option(Options.ofBoolean("Auto Switch To Creative", () -> autoCreative, true, value -> autoCreative = value).description("Switch to creative to load a kit, then switch back").build())
                    .build())
                .group(OptionGroup.builder("Inventory Kits")
                    .option(Options.ofButton("Paste Kit From Clipboard", () -> KitManager.pasteKit(KitCodes.Kind.INVENTORY)).build())
                    .option(Options.ofPageList("Kits",
                            () -> kitNames, List.of(),
                            value -> kitNames = new ArrayList<>(value))
                        .newEntryName("Kit")
                        .noSpaces()
                        .options(name -> List.of(
                            Options.ofInventory("Items",
                                    () -> kit(name), new ItemInventory(),
                                    value -> KITS.put(name, value))
                                .build(),
                            Options.ofKeybind("Keybind", keybindFor(name))
                                .build(),
                            Options.ofButton("Copy Kit To Clipboard", () -> KitManager.copyKit(KitCodes.Kind.INVENTORY, name))
                                .build()))
                        .onRenamed((from, to) -> {
                            final ItemInventory moved = KITS.remove(from);
                            if (moved != null) {
                                KITS.put(to, moved);
                            }
                            renameKeybind(from, to);
                        })
                        .onRemoved(name -> {
                            KITS.remove(name);
                            final WLKeyMapping keybind = KIT_KEYBINDS.remove(name);
                            if (keybind != null) {
                                keybind.unregister();
                            }
                        })
                        .build())
                    .build())
                .group(OptionGroup.builder("Ender Chest Kits")
                    .option(Options.ofBoolean("Liquid Glass Buttons",
                            () -> glassEnderButtons, false, value -> glassEnderButtons = value)
                        .description("Use glass kit buttons on the ender chest instead of vanilla ones")
                        .build())
                    .option(Options.ofButton("Paste Kit From Clipboard", () -> KitManager.pasteKit(KitCodes.Kind.ENDER)).build())
                    .option(Options.ofPageList("Kits",
                            () -> enderKitNames, List.of(),
                            value -> enderKitNames = new ArrayList<>(value))
                        .newEntryName("Ender Kit")
                        .noSpaces()
                        .options(name -> List.of(
                            Options.ofInventory("Items",
                                    () -> enderKit(name), new ItemInventory(),
                                    value -> ENDER_KITS.put(name, value))
                                .container()
                                .build(),
                            Options.ofButton("Copy Kit To Clipboard", () -> KitManager.copyKit(KitCodes.Kind.ENDER, name))
                                .build()))
                        .onRenamed((from, to) -> {
                            final ItemInventory moved = ENDER_KITS.remove(from);
                            if (moved != null) {
                                ENDER_KITS.put(to, moved);
                            }
                        })
                        .onRemoved(ENDER_KITS::remove)
                        .build())
                    .build())
                .build())
            .build();
    }
}
