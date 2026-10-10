package walksy.clientkits.manager;

import main.walksy.lib.core.WalksyLib;
import main.walksy.lib.api.config.value.ItemInventory;
import main.walksy.lib.api.config.value.WLKeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import walksy.clientkits.config.ClientKitsConfig;
import walksy.clientkits.kit.EnderChests;
import walksy.clientkits.kit.KitCodes;

public final class KitManager {

    private static final int STAGING_SLOT = 0;

    private static final int SWITCH_TIMEOUT_TICKS = 60;

    private static State state = State.IDLE;
    private static GameType originalGameMode;
    private static ItemInventory pendingKit;
    private static ItemInventory pendingEnderKit;
    private static String pendingSuccess;
    private static int switchTimer;
    private static int restoreTimer;

    private KitManager() {
    }

    public static boolean saveKit(final String name) {
        final Minecraft client = Minecraft.getInstance();
        if (client.player == null || ClientKitsConfig.hasKit(name)) {
            return false;
        }
        final ItemInventory captured = ItemInventory.captureFrom(client.player);
        if (captured == null) {
            return false;
        }
        ClientKitsConfig.putKit(name, captured);
        return true;
    }

    public static boolean copyKit(final KitCodes.Kind kind, final String name) {
        final boolean inventory = kind == KitCodes.Kind.INVENTORY;
        if (inventory ? !ClientKitsConfig.hasKit(name) : !ClientKitsConfig.hasEnderKit(name)) {
            WalksyLib.getInstance().getToastManager().error("Client Kits", "No " + (inventory ? "kit" : "ender chest kit") + " named '" + name + "'");
            return false;
        }
        KitCodes.copyToClipboard(kind, name, inventory ? ClientKitsConfig.kit(name) : ClientKitsConfig.enderKit(name));
        WalksyLib.getInstance().getToastManager().success("Client Kits", "Copied '" + name + "' to your clipboard. Paste it anywhere to share it.");
        return true;
    }

    public static boolean pasteKit(final KitCodes.Kind kind) {
        final KitCodes.Decoded decoded = KitCodes.readClipboard();
        if (decoded == null) {
            WalksyLib.getInstance().getToastManager().error("Client Kits", "Your clipboard doesn't contain a Client Kits code.");
            return false;
        }
        if (decoded.kind() != kind) {
            WalksyLib.getInstance().getToastManager().error("Client Kits", kind == KitCodes.Kind.INVENTORY
                ? "That's an ender chest kit. Paste it under Ender Chest Kits or with /ck ender paste."
                : "That's an inventory kit. Paste it under Inventory Kits or with /ck paste.");
            return false;
        }
        final String name;
        if (kind == KitCodes.Kind.INVENTORY) {
            name = ClientKitsConfig.uniqueKitName(decoded.name());
            ClientKitsConfig.putKit(name, decoded.inventory());
        } else {
            name = ClientKitsConfig.uniqueEnderKitName(decoded.name());
            ClientKitsConfig.putEnderKit(name, decoded.inventory());
        }
        WalksyLib.getInstance().getToastManager().success("Client Kits", "Imported kit: " + name);
        return true;
    }

    public static String loadKit(final String name) {
        if (!ClientKitsConfig.hasKit(name)) {
            return "No kit named '" + name + "'";
        }
        final Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameMode == null) {
            return "You are not in a world";
        }

        final ItemInventory kit = ClientKitsConfig.kit(name);
        final String success = "Equipped kit: " + name;

        if (client.player.isCreative()) {
            return report(applyKit(kit), success);
        }
        final String blocked = beginCreativeSwitch();
        if (blocked != null) {
            return blocked;
        }
        pendingKit = kit;
        pendingSuccess = success;
        return null;
    }

    public static String saveEnderKit(final String name) {
        if (ClientKitsConfig.hasEnderKit(name)) {
            return "An ender chest kit named '" + name + "' already exists";
        }
        return captureEnderKit(name);
    }

    public static String replaceEnderKit(final String name) {
        if (!ClientKitsConfig.hasEnderKit(name)) {
            return "No ender chest kit named '" + name + "'";
        }
        return captureEnderKit(name);
    }

    private static String captureEnderKit(final String name) {
        final ChestMenu menu = EnderChests.openMenu();
        if (menu == null) {
            return "Open an ender chest to save its contents";
        }
        ClientKitsConfig.putEnderKit(name, EnderChests.capture(menu));
        return null;
    }

    public static String loadEnderKit(final String name) {
        if (!ClientKitsConfig.hasEnderKit(name)) {
            return "No ender chest kit named '" + name + "'";
        }
        final Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameMode == null) {
            return "You are not in a world";
        }
        if (EnderChests.openMenu() == null) {
            return "Open an ender chest first";
        }

        final ItemInventory kit = ClientKitsConfig.enderKit(name);
        final String success = "Loaded ender chest kit: " + name;

        if (client.player.isCreative()) {
            return report(applyEnderKit(kit), success);
        }
        final String blocked = beginCreativeSwitch();
        if (blocked != null) {
            return blocked;
        }
        pendingEnderKit = kit;
        pendingSuccess = success;
        return null;
    }

    private static String report(final String error, final String success) {
        if (error == null) {
            WalksyLib.getInstance().getToastManager().success("Client Kits", success);
        }
        return error;
    }

    private static String beginCreativeSwitch() {
        if (state != State.IDLE) {
            return "Still applying the last kit";
        }
        if (!ClientKitsConfig.autoCreative()) {
            return "Enable 'Auto Switch To Creative', or join creative, to load a kit";
        }
        final Minecraft client = Minecraft.getInstance();
        if (client.player.connection.getCommands().getRoot().getChild("gamemode") == null) {
            return "You don't have permission to switch to creative, so the kit can't be loaded";
        }
        originalGameMode = client.gameMode.getPlayerMode();
        state = State.SWITCHING_TO_CREATIVE;
        switchTimer = SWITCH_TIMEOUT_TICKS;
        client.player.connection.sendCommand("gamemode creative");
        return null;
    }

    public static void tick() {
        checkKeybinds();
        if (state == State.IDLE) {
            return;
        }
        final Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            reset();
            return;
        }

        switch (state) {
            case SWITCHING_TO_CREATIVE -> {
                if (client.player.isCreative()) {
                    final String error = pendingKit != null ? applyKit(pendingKit)
                        : pendingEnderKit != null ? applyEnderKit(pendingEnderKit)
                        : null;
                    if (error != null) {
                        WalksyLib.getInstance().getToastManager().error("Client Kits", error);
                    } else if (pendingSuccess != null) {
                        WalksyLib.getInstance().getToastManager().success("Client Kits", pendingSuccess);
                    }
                    pendingKit = null;
                    pendingEnderKit = null;
                    pendingSuccess = null;
                    restoreTimer = 20;
                    state = State.RESTORING_GAMEMODE;
                } else if (--switchTimer <= 0) {
                    WalksyLib.getInstance().getToastManager().error("Client Kits",
                        "Could not switch to creative - the kit was not applied.");
                    reset();
                }
            }
            case RESTORING_GAMEMODE -> {
                if (--restoreTimer <= 0) {
                    if (originalGameMode != null) {
                        client.player.connection.sendCommand("gamemode " + originalGameMode.getName());
                    }
                    reset();
                }
            }
            default -> {
            }
        }
    }

    private static void checkKeybinds() {
        final Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameMode == null) {
            return;
        }
        for (final String name : ClientKitsConfig.kitNames()) {
            final WLKeyMapping keybind = ClientKitsConfig.keybindFor(name);
            if (keybind.consumeClick()) {
                final String error = KitManager.loadKit(name);
                if (error != null) {
                    WalksyLib.getInstance().getToastManager().error("Client Kits", error);
                }
            }
        }
    }

    private static String applyKit(final ItemInventory kit) {
        try {
            final Minecraft client = Minecraft.getInstance();
            final Player player = client.player;
            final Inventory inventory = player.getInventory();
            for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
                inventory.setItem(slot, kit.stackAt(slot));
            }
            for (int slot = ItemInventory.ARMOR_START; slot <= ItemInventory.OFFHAND; slot++) {
                final EquipmentSlot equipment = ItemInventory.equipmentSlotFor(slot);
                if (equipment != null) {
                    player.setItemSlot(equipment, kit.stackAt(slot));
                }
            }
            final AbstractContainerMenu menu = player.containerMenu;
            for (int i = 0; i < menu.slots.size(); i++) {
                client.gameMode.handleCreativeModeItemAdd(menu.slots.get(i).getItem(), i);
            }
            menu.broadcastChanges();
            return null;
        } catch (final Exception e) {
            return "Failed to apply kit: " + e.getMessage();
        }
    }

    private static String applyEnderKit(final ItemInventory kit) {
        final ChestMenu menu = EnderChests.openMenu();
        if (menu == null) {
            return "The ender chest was closed";
        }
        try {
            final Minecraft client = Minecraft.getInstance();
            final Player player = client.player;
            final ItemStack staged = player.getInventory().getItem(STAGING_SLOT).copy();
            for (int slot = 0; slot < EnderChests.SLOT_COUNT; slot++) {
                final ItemStack target = EnderChests.stackAt(kit, slot);
                if (ItemStack.matches(menu.getSlot(slot).getItem(), target)) {
                    continue;
                }
                stage(client, player, target);
                client.gameMode.handleContainerInput(menu.containerId, slot, STAGING_SLOT,
                    ContainerInput.SWAP, player);
            }
            stage(client, player, staged);
            return null;
        } catch (final Exception e) {
            return "Failed to load ender chest kit: " + e.getMessage();
        }
    }

    private static void stage(final Minecraft client, final Player player, final ItemStack stack) {
        player.getInventory().setItem(STAGING_SLOT, stack.copy());
        client.gameMode.handleCreativeModeItemAdd(stack.copy(),
            InventoryMenu.USE_ROW_SLOT_START + STAGING_SLOT);
    }

    public static Inventory previewInventory(final Player owner, final ItemInventory kit) {
        return PreviewInventory.of(owner, kit);
    }

    private static void reset() {
        state = State.IDLE;
        originalGameMode = null;
        pendingKit = null;
        pendingEnderKit = null;
        pendingSuccess = null;
        switchTimer = 0;
        restoreTimer = 0;
    }

    private enum State {
        IDLE,
        SWITCHING_TO_CREATIVE,
        RESTORING_GAMEMODE
    }
}
