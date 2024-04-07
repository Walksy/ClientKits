package walksy.customkits.other;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import walksy.customkits.main.CustomKitsMod;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class JsonUtil {

    public static String configDir = FabricLoader.getInstance().getConfigDir().toString();

    public static JsonElement getKeyValue(File file, String key) {
        return JsonParser.parseString(readFile(file)).getAsJsonObject().get(key);
    }


    public static String readFile(File file) {
        StringBuilder builder = new StringBuilder();
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                builder.append(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error has occurred when reading the json file: " + e.getMessage());
        }
        return builder.toString();
    }

    public static void writeJson(File file, JsonObject data){
        writeFile(file, data.toString());
    }

    private static void writeFile(File file, String data) {
        try (FileWriter fr = new FileWriter(file)) {
            fr.write(data);
        } catch (IOException ignored) {

        }
    }

    public static JsonArray itemListToJsonArray(List<ItemStack> itemList) {
        JsonArray jsonArray = new JsonArray();
        for (ItemStack itemStack : itemList) {
            jsonArray.add(itemStackToJsonObject(itemStack));
        }
        return jsonArray;
    }

    public static JsonObject itemStackToJsonObject(ItemStack itemStack) {
        JsonObject jsonObject = new JsonObject();
        if (!itemStack.isEmpty()) {
            jsonObject.addProperty("item", itemStack.getItem().getRegistryEntry().toString());
            jsonObject.addProperty("count", itemStack.getCount());
            // Add more properties as needed
        }
        return jsonObject;
    }
}
