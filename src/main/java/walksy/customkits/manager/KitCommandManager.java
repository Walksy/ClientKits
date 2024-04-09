package walksy.customkits.manager;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import walksy.customkits.main.ClientKitsMod;

import java.util.List;


public class KitCommandManager {

    public static boolean loadKit = false, shouldChangeBack = false;
    private static GameMode oldGM = null;
    private static String tempName = null;
    public static int i = 0;
    private static CommandContext<FabricClientCommandSource> tempSource = null;

    public KitCommandManager() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("ck")
                .then(ClientCommandManager.literal("save")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            handleSaveCommand(context, StringArgumentType.getString(context, "name"));
                            return 1;
                        })
                    )
                )
                .then(ClientCommandManager.literal("load")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(KitManager.kits.keySet(), builder))
                        .executes(context -> {
                            loadKit = true;
                            tempName = StringArgumentType.getString(context, "name");
                            tempSource = context;
                            return 1;
                        })
                    )
                )
                .then(ClientCommandManager.literal("delete")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(KitManager.kits.keySet(), builder))
                        .executes(context -> {
                            handleDeleteCommand(StringArgumentType.getString(context, "name"));
                            return 1;
                        })
                    )
                )
            )
        );
    }


    void handleSaveCommand(CommandContext<FabricClientCommandSource> source, String name) {
        KitManager.kits.put(name, source.getSource().getPlayer().getInventory().writeNbt(new NbtList()));
        ConfigManager.saveKitToFile(name);
        ClientKitsMod.debugMessage("§aSaved kit: " + name);
    }

    void handleDeleteCommand(String name) {
        if (KitManager.kits.get(name) != null) {
            KitManager.kits.remove(name);
            ClientKitsMod.debugMessage("§aDeleted Kit: " + name + ".");
            ConfigManager.deleteKit(name);
        } else {
            ClientKitsMod.debugMessage("§cCannot find the kit '" + name + "' to delete.");
        }
    }
    public static void tick() {
        if (loadKit) {
            if (!tempSource.getSource().getPlayer().getAbilities().creativeMode) {
                if (tempSource.getSource().getPlayer().hasPermissionLevel(2) || tempSource.getSource().getPlayer().hasPermissionLevel(4)) {
                    PlayerListEntry playerListEntry = tempSource.getSource().getClient().getNetworkHandler().getPlayerListEntry(tempSource.getSource().getPlayer().getUuid());
                    oldGM = playerListEntry.getGameMode();
                    tempSource.getSource().getPlayer().networkHandler.sendChatCommand("gamemode creative");
                    shouldChangeBack = true;
                }
                if (!shouldChangeBack) {
                    ClientKitsMod.debugMessage("§cMust be in creative mode to receive kit.");
                    reset();
                    return;
                }
            }
            if (!tempSource.getSource().getPlayer().getAbilities().creativeMode) return;
            NbtList kit = KitManager.kits.get(tempName);
            if (kit == null) {
                ClientKitsMod.debugMessage("§cKit not found.");
                reset();
                return;
            }
            PlayerInventory tempInv = new PlayerInventory(tempSource.getSource().getPlayer());
            tempInv.readNbt(kit);
            List<Slot> slots = tempSource.getSource().getPlayer().playerScreenHandler.slots;
            for (int i = 0; i < slots.size(); i++) {
                if (slots.get(i).inventory == tempSource.getSource().getPlayer().getInventory()) {
                    ItemStack existingItemStack = tempSource.getSource().getPlayer().getInventory().getStack(slots.get(i).getIndex());
                    if (!existingItemStack.isEmpty()) {
                        tempSource.getSource().getClient().interactionManager.clickCreativeStack(ItemStack.EMPTY, i); //clear out old items
                    }
                    ItemStack itemStack = tempInv.getStack(slots.get(i).getIndex());
                    if (!itemStack.isEmpty()) {
                        tempSource.getSource().getClient().interactionManager.clickCreativeStack(itemStack, i);
                    }
                }
            }
            if (shouldChangeBack) {
                String command = switch (oldGM) {
                    case SURVIVAL -> "survival";
                    case ADVENTURE -> "adventure";
                    case SPECTATOR -> "spectator";
                    default -> "";
                };
                tempSource.getSource().getPlayer().networkHandler.sendChatCommand("gamemode " + command);
                shouldChangeBack = false;
            }

            tempSource.getSource().getPlayer().playerScreenHandler.sendContentUpdates();
            ClientKitsMod.debugMessage("§aLoaded kit: " + tempName + ".");
            reset();
        }
        if (i != 0)
        {
            i--;
        }
    }

    static void reset()
    {
        loadKit = false;
        shouldChangeBack = false;
        tempName = null;
        tempSource = null;
        oldGM = null;
        i = 3;
    }
}


