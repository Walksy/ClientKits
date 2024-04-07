package walksy.customkits.manager;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.*;
import net.minecraft.util.Util;
import walksy.customkits.other.JsonUtil;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    public static void saveKitToFile(String kitName) {
        File configDir = new File(JsonUtil.configDir, "ClientKits");
        boolean directoryCreated = configDir.mkdirs();

        if (!directoryCreated && !configDir.exists()) {
            System.out.println("Failed to create directory: " + configDir.getAbsolutePath());
            return;
        }

        try {
            if (KitManager.kits.containsKey(kitName)) {
                NbtCompound rootTag = new NbtCompound();
                NbtCompound compoundTag = new NbtCompound();
                compoundTag.put(kitName, KitManager.kits.get(kitName));
                rootTag.putInt("DataVersion", SharedConstants.getGameVersion().getSaveVersion().getId());
                rootTag.put("Kit", compoundTag);

                File newFile = new File(configDir, kitName + ".dat");
                NbtIo.write(rootTag, newFile);

                File backupFile = new File(configDir, kitName + ".dat_old");
                File currentFile = new File(configDir, kitName + ".dat");
                Util.backupAndReplace(currentFile, newFile, backupFile);
            } else {
                System.out.println("Kit with name " + kitName + " does not exist.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadKitsFromFiles() throws IOException {
        KitManager.kits.clear();
        File configDir = new File(JsonUtil.configDir, "ClientKits");
        if (!configDir.exists()) {
            return;
        }

        final int currentVersion = SharedConstants.getGameVersion().getSaveVersion().getId();
        DataFixer dataFixer = MinecraftClient.getInstance().getDataFixer();

        for (File file : configDir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".dat")) {
                NbtCompound rootTag = NbtIo.read(file);
                if (rootTag == null) {
                    continue;
                }
                final int fileVersion = rootTag.getInt("DataVersion");
                NbtCompound compoundTag = rootTag.getCompound("Kit");

                for (String key : compoundTag.getKeys()) {
                    if (fileVersion >= currentVersion) {
                        KitManager.kits.put(key, compoundTag.getList(key, NbtElement.COMPOUND_TYPE));
                    } else {
                        NbtCompound kitData = compoundTag.getCompound(key);
                        NbtList updatedListTag = new NbtList();
                        kitData.getKeys().forEach(entryKey -> {
                            NbtElement tag = kitData.get(entryKey);
                            Dynamic<NbtElement> oldTagDynamic = new Dynamic<>(NbtOps.INSTANCE, tag);
                            Dynamic<NbtElement> newTagDynamic = dataFixer.update(TypeReferences.ITEM_STACK, oldTagDynamic, fileVersion, currentVersion);
                            updatedListTag.add(newTagDynamic.getValue());
                        });
                        KitManager.kits.put(key, updatedListTag);
                    }
                }
            }
        }
    }
}
