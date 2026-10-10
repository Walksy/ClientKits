package walksy.clientkits.manager;

import main.walksy.lib.core.WalksyLib;
import main.walksy.lib.api.config.value.ItemInventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public final class PreviewInventory {

    private PreviewInventory() {
    }

    public static Inventory of(final Player owner, final ItemInventory kit) {
        final Inventory inventory = new Inventory(owner);
        for (int slot = 0; slot <= ItemInventory.OFFHAND; slot++) {
            inventory.setItem(slot, kit.stackAt(slot));
        }
        return inventory;
    }

    public static LivingEntity model(final Player fallback, final ItemInventory kit) {
        final Entity preview = WalksyLib.getInstance().getPreviewEntities().player();
        if (!(preview instanceof final LivingEntity living)) {
            return fallback;
        }
        for (int slot = ItemInventory.ARMOR_START; slot <= ItemInventory.OFFHAND; slot++) {
            final EquipmentSlot equipment = ItemInventory.equipmentSlotFor(slot);
            if (equipment != null) {
                living.setItemSlot(equipment, kit.stackAt(slot));
            }
        }
        return living;
    }
}
