package walksy.customkits.manager;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import walksy.customkits.main.CustomKitsMod;

import java.util.List;


public class KitCommandManager {

    //TODO Make Checks, make them look good

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
                            handleLoadCommand(context, StringArgumentType.getString(context, "name"));
                            return 1;
                        })
                    )
                )
            )
        );
    }



    void handleSaveCommand(CommandContext<FabricClientCommandSource> source, String name)
    {
        KitManager.kits.put(name, source.getSource().getPlayer().getInventory().writeNbt(new NbtList()));
        ConfigManager.saveKitToFile(name);
        CustomKitsMod.debugMessage("Saved kit: " + name);
    }

    void handleLoadCommand(CommandContext<FabricClientCommandSource> source, String name)
    {
        NbtList kit = KitManager.kits.get(name);
        PlayerInventory tempInv = new PlayerInventory(source.getSource().getPlayer());
        tempInv.readNbt(kit);
        source.getSource().getPlayer().getInventory().clear();
        List<Slot> slots = source.getSource().getPlayer().playerScreenHandler.slots;
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).inventory == source.getSource().getPlayer().getInventory()) {
                ItemStack itemStack = tempInv.getStack(slots.get(i).getIndex());
                if (!itemStack.isEmpty()) {
                    source.getSource().getClient().interactionManager.clickCreativeStack(itemStack, i);
                }
            }
        }

        source.getSource().getPlayer().playerScreenHandler.sendContentUpdates();
    }
}
