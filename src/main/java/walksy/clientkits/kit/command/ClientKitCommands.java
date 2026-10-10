package walksy.clientkits.kit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import main.walksy.lib.core.WalksyLib;
import main.walksy.lib.api.config.value.ItemInventory;
import main.walksy.lib.core.gui.WalksyLibPanelScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import main.walksy.lib.core.platform.WLClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import walksy.clientkits.config.ClientKitsConfig;
import walksy.clientkits.kit.KitCodes;
import walksy.clientkits.manager.KitManager;
import walksy.clientkits.screen.KitPreviewScreen;

public final class ClientKitCommands {

    private ClientKitCommands() {
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    private static <T> RequiredArgumentBuilder<FabricClientCommandSource, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static void create() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(literal("ck")
                .then(literal("save")
                    .then(argument("name", StringArgumentType.word())
                        .executes(c -> save(c, StringArgumentType.getString(c, "name")))))
                .then(literal("load")
                    .then(argument("name", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(ClientKitsConfig.kitNames(), b))
                        .executes(c -> load(c, StringArgumentType.getString(c, "name")))))
                .then(literal("delete")
                    .then(argument("name", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(ClientKitsConfig.kitNames(), b))
                        .executes(c -> delete(c, StringArgumentType.getString(c, "name")))))
                .then(literal("preview")
                    .then(argument("name", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(ClientKitsConfig.kitNames(), b))
                        .executes(c -> preview(c, StringArgumentType.getString(c, "name")))))
                .then(literal("copy")
                    .then(argument("name", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(ClientKitsConfig.kitNames(), b))
                        .executes(c -> result(KitManager.copyKit(KitCodes.Kind.INVENTORY, StringArgumentType.getString(c, "name"))))))
                .then(literal("paste")
                    .executes(c -> result(KitManager.pasteKit(KitCodes.Kind.INVENTORY))))
                .then(literal("list")
                    .executes(ClientKitCommands::list))
                .then(literal("ender")
                    .then(literal("save")
                        .then(argument("name", StringArgumentType.word())
                            .executes(c -> saveEnder(StringArgumentType.getString(c, "name")))))
                    .then(literal("load")
                        .then(argument("name", StringArgumentType.word())
                            .suggests((c, b) -> SharedSuggestionProvider.suggest(ClientKitsConfig.enderKitNames(), b))
                            .executes(c -> loadEnder(StringArgumentType.getString(c, "name")))))
                    .then(literal("delete")
                        .then(argument("name", StringArgumentType.word())
                            .suggests((c, b) -> SharedSuggestionProvider.suggest(ClientKitsConfig.enderKitNames(), b))
                            .executes(c -> deleteEnder(StringArgumentType.getString(c, "name")))))
                    .then(literal("copy")
                        .then(argument("name", StringArgumentType.word())
                            .suggests((c, b) -> SharedSuggestionProvider.suggest(ClientKitsConfig.enderKitNames(), b))
                            .executes(c -> result(KitManager.copyKit(KitCodes.Kind.ENDER, StringArgumentType.getString(c, "name"))))))
                    .then(literal("paste")
                        .executes(c -> result(KitManager.pasteKit(KitCodes.Kind.ENDER))))
                    .then(literal("list")
                        .executes(ClientKitCommands::listEnder)))
                .then(literal("config")
                    .executes(ClientKitCommands::openConfig))
            ));
    }

    private static int save(final CommandContext<FabricClientCommandSource> ctx, final String name) {
        if (ClientKitsConfig.hasKit(name)) {
            WalksyLib.getInstance().getToastManager().error("Client Kits", "A kit named '" + name + "' already exists.");
            return 0;
        }
        if (KitManager.saveKit(name)) {
            WalksyLib.getInstance().getToastManager().success("Client Kits", "Saved kit: " + name);
        } else {
            WalksyLib.getInstance().getToastManager().error("Client Kits", "Failed to save kit.");
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int load(final CommandContext<FabricClientCommandSource> ctx, final String name) {
        final String error = KitManager.loadKit(name);
        if (error != null) {
            WalksyLib.getInstance().getToastManager().error("Client Kits", error);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int delete(final CommandContext<FabricClientCommandSource> ctx, final String name) {
        if (!ClientKitsConfig.hasKit(name)) {
            WalksyLib.getInstance().getToastManager().error("Client Kits", "No kit named '" + name + "' exists.");
            return 0;
        }
        ClientKitsConfig.removeKit(name);
        WalksyLib.getInstance().getToastManager().success("Client Kits", "Deleted kit: " + name);
        return Command.SINGLE_SUCCESS;
    }

    private static int preview(final CommandContext<FabricClientCommandSource> ctx, final String name) {
        if (!ClientKitsConfig.hasKit(name)) {
            WalksyLib.getInstance().getToastManager().error("Client Kits", "No kit named '" + name + "' exists.");
            return 0;
        }
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            WalksyLib.getInstance().getToastManager().error("Client Kits", "You are not in a world.");
            return 0;
        }
        final ItemInventory kit = ClientKitsConfig.kit(name);
        mc.execute(() -> WLClient.setScreen(new KitPreviewScreen(mc.player, KitManager.previewInventory(mc.player, kit), name, kit)));
        return Command.SINGLE_SUCCESS;
    }

    private static int result(final boolean success) {
        return success ? Command.SINGLE_SUCCESS : 0;
    }

    private static int saveEnder(final String name) {
        final String error = KitManager.saveEnderKit(name);
        if (error == null) {
            WalksyLib.getInstance().getToastManager().success("Client Kits", "Saved ender chest kit: " + name);
            return Command.SINGLE_SUCCESS;
        }
        WalksyLib.getInstance().getToastManager().error("Client Kits", error);
        return 0;
    }

    private static int loadEnder(final String name) {
        final String error = KitManager.loadEnderKit(name);
        if (error == null) {
            return Command.SINGLE_SUCCESS;
        }
        WalksyLib.getInstance().getToastManager().error("Client Kits", error);
        return 0;
    }

    private static int deleteEnder(final String name) {
        if (!ClientKitsConfig.hasEnderKit(name)) {
            WalksyLib.getInstance().getToastManager().error("Client Kits", "No ender chest kit named '" + name + "' exists.");
            return 0;
        }
        ClientKitsConfig.removeEnderKit(name);
        WalksyLib.getInstance().getToastManager().success("Client Kits", "Deleted ender chest kit: " + name);
        return Command.SINGLE_SUCCESS;
    }

    private static int listEnder(final CommandContext<FabricClientCommandSource> ctx) {
        final var names = ClientKitsConfig.enderKitNames();
        if (names.isEmpty()) {
            sendMsg(ctx, "No ender chest kits yet. Open an ender chest and use /ck ender save <name>, or /ck config.", ChatFormatting.YELLOW);
        } else {
            sendMsg(ctx, "Ender chest kits (" + names.size() + "): ", String.join(", ", names), ChatFormatting.GREEN);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int list(final CommandContext<FabricClientCommandSource> ctx) {
        final var names = ClientKitsConfig.kitNames();
        if (names.isEmpty()) {
            sendMsg(ctx, "No kits yet. Use /ck save <name> or /ck config.", ChatFormatting.YELLOW);
        } else {
            sendMsg(ctx, "Kits (" + names.size() + "): ", String.join(", ", names), ChatFormatting.GREEN);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int openConfig(final CommandContext<FabricClientCommandSource> ctx) {
        final Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> WLClient.setScreen(new WalksyLibPanelScreen(WLClient.screen(), "Client Kits").preselect("clientkits")));
        return Command.SINGLE_SUCCESS;
    }

    private static void sendMsg(final CommandContext<FabricClientCommandSource> ctx, final String msg, final ChatFormatting color) {
        ctx.getSource().sendFeedback(prefix().append(Component.literal(msg).withStyle(color)));
    }

    private static void sendMsg(final CommandContext<FabricClientCommandSource> ctx, final String label, final String value, final ChatFormatting color) {
        ctx.getSource().sendFeedback(prefix()
            .append(Component.literal(label).withStyle(color))
            .append(Component.literal(value).withStyle(ChatFormatting.WHITE)));
    }

    private static MutableComponent prefix() {
        return Component.empty()
            .append(Component.literal("ClientKits").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY));
    }
}
