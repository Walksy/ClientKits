package walksy.clientkits.kit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import walksy.clientkits.kit.KitIO;
import walksy.clientkits.kit.KitSerializer;
import walksy.clientkits.manager.KitManager;
import walksy.clientkits.screen.KitPreviewScreen;

public class ClientKitCommands {

    public static void create() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal("ck")
                        .then(ClientCommands.literal("save")
                                .then(ClientCommands.argument("name", StringArgumentType.word())
                                        .executes(c -> save(c, StringArgumentType.getString(c, "name")))))
                        .then(ClientCommands.literal("load")
                                .then(ClientCommands.argument("name", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(KitIO.listKits(), b))
                                        .executes(c -> load(c, StringArgumentType.getString(c, "name")))))
                        .then(ClientCommands.literal("delete")
                                .then(ClientCommands.argument("name", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(KitIO.listKits(), b))
                                        .executes(c -> delete(c, StringArgumentType.getString(c, "name")))))
                        .then(ClientCommands.literal("preview")
                                .then(ClientCommands.argument("name", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(KitIO.listKits(), b))
                                        .executes(c -> preview(c, StringArgumentType.getString(c, "name")))))
                ));
    }

    private static int save(CommandContext<FabricClientCommandSource> ctx, String name) {
        if (KitIO.exists(name)) {
            sendMsg(ctx, "A kit named '" + name + "' already exists.", ChatFormatting.RED);
            return 0;
        }
        if (KitManager.saveKit(name)) {
            sendMsg(ctx, "Saved kit: ", name, ChatFormatting.GREEN);
        } else {
            sendMsg(ctx, "Failed to save kit.", ChatFormatting.RED);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int load(CommandContext<FabricClientCommandSource> ctx, String name) {
        String rsp = KitManager.loadKit(name);
        if (rsp == null) {
            sendMsg(ctx, "Equipped kit: ", name, ChatFormatting.GREEN);
        } else {
            sendMsg(ctx, rsp, ChatFormatting.RED);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int delete(CommandContext<FabricClientCommandSource> ctx, String name) {
        try {
            KitIO.moveToTrash(name);
            sendMsg(ctx, "Deleted kit: ", name, ChatFormatting.RED);
        } catch (Exception e) {
            sendMsg(ctx, "Could not delete kit (file error).", ChatFormatting.RED);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int preview(CommandContext<FabricClientCommandSource> ctx, String name) {
        if (!KitIO.exists(name)) {
            sendMsg(ctx, "No kit named '" + name + "' exists.", ChatFormatting.RED);
            return 0;
        }

        String data = KitIO.load(name);
        if (!KitManager.isValid(data)) {
            sendMsg(ctx, "Kit data is corrupted or missing.", ChatFormatting.RED);
            return 0;
        }

        Inventory kitInventory;
        try {
            kitInventory = KitSerializer.deserialize(data);
        } catch (Exception e) {
            sendMsg(ctx, "Failed to deserialize kit.", ChatFormatting.RED);
            return 0;
        }

        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.setScreen(new KitPreviewScreen(mc.player, kitInventory, name)));
        return Command.SINGLE_SUCCESS;
    }

    private static void sendMsg(CommandContext<FabricClientCommandSource> ctx, String msg, ChatFormatting color) {
        ctx.getSource().sendFeedback(
                Component.empty()
                        .append(Component.literal("ClientKits").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                        .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(msg).withStyle(color))
        );
    }

    private static void sendMsg(CommandContext<FabricClientCommandSource> ctx, String label, String name, ChatFormatting color) {
        ctx.getSource().sendFeedback(
                Component.empty()
                        .append(Component.literal("ClientKits").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                        .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(label).withStyle(color))
                        .append(Component.literal(name).withStyle(ChatFormatting.WHITE))
        );
    }
}