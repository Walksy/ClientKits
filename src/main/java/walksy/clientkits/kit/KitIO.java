package walksy.clientkits.kit;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class KitIO {
    private static final Path ROOT_DIR = Paths.get("config", "clientkits", "kits");
    private static final Path TRASH_DIR = ROOT_DIR.resolve("deleted");
    private static final String EXT = ".json";

    public static void init() throws IOException {
        if (!Files.exists(ROOT_DIR)) Files.createDirectories(ROOT_DIR);
        if (!Files.exists(TRASH_DIR)) Files.createDirectories(TRASH_DIR);
    }

    public static void save(String name, String data) throws IOException {
        Files.createDirectories(ROOT_DIR);
        Files.writeString(ROOT_DIR.resolve(name + EXT), data);
    }

    public static String load(String name) {
        try {
            return Files.readString(ROOT_DIR.resolve(name + EXT));
        } catch (IOException e) { return ""; }
    }

    public static void moveToTrash(String name) throws IOException {
        Path source = ROOT_DIR.resolve(name + EXT);
        if (Files.exists(source)) {
            Files.createDirectories(TRASH_DIR);
            Files.move(source, TRASH_DIR.resolve(name + EXT), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static List<String> listKits() {
        try (var stream = Files.list(ROOT_DIR)) {
            return stream.filter(p -> p.toString().endsWith(EXT))
                .map(p -> p.getFileName().toString().replace(EXT, ""))
                .collect(Collectors.toList());
        } catch (IOException e) { return Collections.emptyList(); }
    }

    public static boolean exists(String name) {
        return Files.exists(ROOT_DIR.resolve(name + EXT));
    }
}
