package ru.fewizz.trade;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import ru.fewizz.trade.client.ClientTradeScreenHandler;
import ru.fewizz.trade.client.OtherClientTradeScreenHandler;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

public class Trade implements ModInitializer {
	public static final Identifier
		TRADE_REQUEST = new Identifier("trade", "request"),
		TRADE_STATE_C2S = new Identifier("trade", "state_c2s"),
		TRADE_STATE_S2C = new Identifier("trade", "state_s2c"),
		TRADE_START = new Identifier("trade", "start");

	public static final ScreenHandlerType<ClientTradeScreenHandler> TRADE_SCREEN_HANDLER_SCREEN_HANDLER_TYPE =
			ScreenHandlerRegistry.registerExtended(
				new Identifier("trade", "trade"),
				(int syncId, PlayerInventory inventory, PacketByteBuf bb) -> {
					UUID otherPlayerUUID = bb.readUuid();
					return new ClientTradeScreenHandler(
						syncId,
						tsh -> new OtherClientTradeScreenHandler(
							otherPlayerUUID,
							tsh1 -> tsh
						)
					);
				}
			);

	@Override
	public void onInitialize() {
		NotSerializableTimerCallback.DummyTimerCallbackSerializer.register();

		// SERVER_STARTED called from newly created server thread, so this collection must be thread-safe
		Map<MinecraftServer, ServerWrapper> serverToWrapper = Collections.synchronizedMap(new IdentityHashMap<>());
		
		ServerLifecycleEvents.SERVER_STARTED.register(server -> serverToWrapper.put(server, new ServerWrapper(server)));
		ServerLifecycleEvents.SERVER_STOPPED.register(serverToWrapper::remove);

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher
				.register(
				CommandManager.literal("trade").
				then(
					CommandManager.argument("player", EntityArgumentType.player())
					.executes(cmd -> {
						serverToWrapper.get(
							cmd.getSource()
							.getServer()
						).tradeRequest(
							cmd.getSource().getPlayer(),
							EntityArgumentType.getPlayer(cmd, "player")
						);

						return Command.SINGLE_SUCCESS;
					})
				)
				.then(
					CommandManager.literal("reload")
					.requires(src -> src.hasPermissionLevel(src.getServer().getOpPermissionLevel()))
					.executes(cmd -> {
						try {
							serverToWrapper.get(cmd.getSource().getServer()).loadProps();
						}
						catch (Exception e) {
							cmd.getSource().sendError(new LiteralText(e.getMessage()));
							return 0;
						}
						cmd.getSource().sendFeedback(new LiteralText("Reloaded successfully"), true);
						return Command.SINGLE_SUCCESS;
					})
				)
			);
		});
		ServerPlayNetworking.registerGlobalReceiver(TRADE_REQUEST, (server, player, handler, buf, responseSender) -> {
			serverToWrapper.get(server).onTradeRequestPacket(player, buf);
		});
		ServerPlayNetworking.registerGlobalReceiver(TRADE_STATE_C2S, (server, player, handler, buf, responseSender) -> {
			serverToWrapper.get(server).tradeStateChange(player, buf);
		});
	}
}
