package walksy.clientkits.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import walksy.clientkits.kit.KitIO;
import walksy.clientkits.kit.KitSerializer;

public final class KitManager {

    private static State state = State.IDLE;
    private static GameType originalGameMode = null;
    private static String pendingData = null;
    private static int restoreTimer = 0;

    public static boolean saveKit(String name) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return false;
        if (KitIO.exists(name)) return false;

        String data = KitSerializer.serialize(client.player.getInventory());
        if (!isValid(data)) return false;

        try {
            KitIO.save(name, data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String loadKit(String name) {
        String data = KitIO.load(name);
        if (!isValid(data)) return "Kit data is corrupted or missing";

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return "player not found";

        if (client.player.isCreative()) {
            return applyData(data);
        }

        pendingData = data;
        originalGameMode = client.gameMode.getPlayerMode();
        state = State.SWITCHING_TO_CREATIVE;
        client.player.connection.sendCommand("gamemode creative");
        return null;
    }

    public static void tick() {
        if (state == State.IDLE) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            reset();
            return;
        }

        switch (state) {
            case SWITCHING_TO_CREATIVE -> {
                if (client.player.isCreative() && pendingData != null) {
                    applyData(pendingData);
                    pendingData = null;
                    restoreTimer = 20;
                    state = State.RESTORING_GAMEMODE;
                }
            }
            case RESTORING_GAMEMODE -> {
                if (--restoreTimer <= 0) {
                    client.player.connection.sendCommand("gamemode " + originalGameMode.getName());
                    reset();
                }
            }
        }
    }

    private static String applyData(String data) {
        try {
            Minecraft client = Minecraft.getInstance();
            Inventory loaded = KitSerializer.deserialize(data);
            Inventory playerInv = client.player.getInventory();
            AbstractContainerMenu menu = client.player.containerMenu;

            for (int i = 0; i < playerInv.getContainerSize(); i++) {
                playerInv.setItem(i, ItemStack.EMPTY);
            }

            for (int i = 0; i < loaded.getContainerSize(); i++) {
                playerInv.setItem(i, loaded.getItem(i).copy());
            }

            for (int i = 0; i < menu.slots.size(); i++) {
                client.gameMode.handleCreativeModeItemAdd(menu.slots.get(i).getItem(), i);
            }

            menu.broadcastChanges();
            return null;
        } catch (Exception e) {
            return "Failed to apply kit data: " + e.getMessage();
        }
    }

    private static void reset() {
        state = State.IDLE;
        originalGameMode = null;
        pendingData = null;
        restoreTimer = 0;
    }

    public static boolean isValid(String data) {
        try {
            return data != null && !data.isEmpty() && TagParser.parseCompoundFully(data).contains("inv");
        } catch (Exception e) {
            return false;
        }
    }

    private enum State {
        IDLE,
        SWITCHING_TO_CREATIVE,
        RESTORING_GAMEMODE
    }

}