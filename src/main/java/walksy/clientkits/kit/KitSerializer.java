package walksy.clientkits.kit;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class KitSerializer {

    public static String serialize(Inventory inventory) {
        var client = Minecraft.getInstance();
        var root = new CompoundTag();
        root.put("data_version", IntTag.valueOf(SharedConstants.getCurrentVersion().dataVersion().version()));
        root.put("mc_version", StringTag.valueOf(SharedConstants.getCurrentVersion().name()));
        root.put("creator", StringTag.valueOf(client.player.getName().getString()));

        var list = new ListTag();
        var ops = client.player.registryAccess().createSerializationContext(NbtOps.INSTANCE);

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;

            var slotTag = new CompoundTag();
            slotTag.putByte("Slot", (byte) mapLocalToRemoteSlot(i));
            ItemStack.CODEC.encode(stack, ops, slotTag).result().ifPresent(list::add);
        }

        root.put("inv", list);
        return root.toString();
    }

    public static Inventory deserialize(String data) throws CommandSyntaxException {
        var client = Minecraft.getInstance();
        var root = TagParser.parseCompoundFully(data);
        var currentVersion = SharedConstants.getCurrentVersion().dataVersion().version();
        var kitVersion = root.getInt("data_version").orElse(currentVersion);
        var list = (ListTag) root.get("inv");

        var inventory = new Inventory(client.player, new EntityEquipment());
        inventory.clearContent();
        var ops = client.player.registryAccess().createSerializationContext(NbtOps.INSTANCE);

        for (int i = 0; i < list.size(); i++) {
            var tag = list.getCompound(i).orElseThrow();
            int slot = mapRemoteToLocalSlot(tag.contains("Slot") ? (tag.getByte("Slot").orElse((byte) 0) & 255) : i);

            Tag itemData = (currentVersion == kitVersion) ? tag : client.getFixerUpper().update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, tag), kitVersion, currentVersion).getValue();

            inventory.setItem(slot, ItemStack.CODEC.parse(ops, itemData).result().orElse(ItemStack.EMPTY));
        }
        return inventory;
    }

    private static int mapLocalToRemoteSlot(int i) {
        if (i >= Inventory.INVENTORY_SIZE && i < Inventory.SLOT_OFFHAND) return i + (100 - Inventory.INVENTORY_SIZE);
        return (i == Inventory.SLOT_OFFHAND) ? 150 : i;
    }

    private static int mapRemoteToLocalSlot(int s) {
        if (s >= 100 && s < 150) return s - (100 - Inventory.INVENTORY_SIZE);
        return (s >= 150) ? Inventory.SLOT_OFFHAND : s;
    }
}