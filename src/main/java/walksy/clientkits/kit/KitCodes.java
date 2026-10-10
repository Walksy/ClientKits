package walksy.clientkits.kit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import main.walksy.lib.core.config.serialization.adapters.ItemInventoryAdapter;
import main.walksy.lib.api.config.value.ItemInventory;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class KitCodes {
    private static final String PREFIX = "clientkits:";
    private static final int FORMAT_VERSION = 1;
    private static final int MAX_DECODED_BYTES = 1 << 20;
    private static final String KEY_VERSION = "v";
    private static final String KEY_KIND = "kind";
    private static final String KEY_NAME = "name";
    private static final String KEY_ITEMS = "items";
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(ItemInventory.class, new ItemInventoryAdapter()).create();

    public enum Kind {
        INVENTORY("inventory"),
        ENDER("ender");

        private final String id;

        Kind(final String id) {
            this.id = id;
        }

        private static @Nullable Kind byId(final String id) {
            for (final Kind kind : values()) {
                if (kind.id.equals(id)) {
                    return kind;
                }
            }
            return null;
        }
    }

    public record Decoded(Kind kind, String name, ItemInventory inventory) {
    }

    private KitCodes() {
    }

    public static void copyToClipboard(final Kind kind, final String name, final ItemInventory inventory) {
        Minecraft.getInstance().keyboardHandler.setClipboard(encode(kind, name, inventory));
    }

    public static @Nullable Decoded readClipboard() {
        return decode(Minecraft.getInstance().keyboardHandler.getClipboard());
    }

    public static String encode(final Kind kind, final String name, final ItemInventory inventory) {
        final JsonObject root = new JsonObject();
        root.addProperty(KEY_VERSION, FORMAT_VERSION);
        root.addProperty(KEY_KIND, kind.id);
        root.addProperty(KEY_NAME, name);
        root.add(KEY_ITEMS, GSON.toJsonTree(inventory, ItemInventory.class));
        return PREFIX + Base64.getEncoder().encodeToString(deflate(root.toString().getBytes(StandardCharsets.UTF_8)));
    }

    public static @Nullable Decoded decode(@Nullable final String text) {
        if (text == null) {
            return null;
        }
        final int start = text.indexOf(PREFIX);
        if (start < 0) {
            return null;
        }
        final StringBuilder payload = new StringBuilder();
        for (int i = start + PREFIX.length(); i < text.length(); i++) {
            final char c = text.charAt(i);
            if (isBase64(c)) {
                payload.append(c);
            } else if (!Character.isWhitespace(c)) {
                break;
            }
        }
        try {
            final byte[] json = inflate(Base64.getDecoder().decode(payload.toString()));
            final JsonObject root = JsonParser.parseString(new String(json, StandardCharsets.UTF_8)).getAsJsonObject();
            final Kind kind = Kind.byId(root.get(KEY_KIND).getAsString());
            final JsonElement items = root.get(KEY_ITEMS);
            if (kind == null || items == null) {
                return null;
            }
            final JsonElement name = root.get(KEY_NAME);
            final ItemInventory inventory = GSON.fromJson(items, ItemInventory.class);
            return new Decoded(kind, name == null ? "" : name.getAsString(), inventory == null ? new ItemInventory() : inventory);
        } catch (final RuntimeException | DataFormatException invalid) {
            return null;
        }
    }

    private static boolean isBase64(final char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '+' || c == '/' || c == '=';
    }

    private static byte[] deflate(final byte[] input) {
        final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        try {
            deflater.setInput(input);
            deflater.finish();
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                out.write(buffer, 0, deflater.deflate(buffer));
            }
            return out.toByteArray();
        } finally {
            deflater.end();
        }
    }

    private static byte[] inflate(final byte[] input) throws DataFormatException {
        final Inflater inflater = new Inflater();
        try {
            inflater.setInput(input);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                final int read = inflater.inflate(buffer);
                if (read == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                    throw new DataFormatException("Truncated kit code");
                }
                out.write(buffer, 0, read);
                if (out.size() > MAX_DECODED_BYTES) {
                    throw new DataFormatException("Kit code is too large");
                }
            }
            return out.toByteArray();
        } finally {
            inflater.end();
        }
    }
}
