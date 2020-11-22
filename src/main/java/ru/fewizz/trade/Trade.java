package ru.fewizz.trade;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public class Trade implements ModInitializer {
	public static final Identifier
		TRADE_REQUEST = new Identifier("trade", "request"),
		TRADE_STATE_C2S = new Identifier("trade", "state_c2s"),
		TRADE_STATE_S2C = new Identifier("trade", "state_s2c"),
		TRADE_START = new Identifier("trade", "start");

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
							.getMinecraftServer()
						).tradeRequest(
							cmd.getSource().getPlayer(),
							EntityArgumentType.getPlayer(cmd, "player")
						);

						return Command.SINGLE_SUCCESS;
					})
				)
				.then(
					CommandManager.literal("reload")
					.requires(src -> src.hasPermissionLevel(src.getMinecraftServer().getOpPermissionLevel()))
					.executes(cmd -> {
						try {
							serverToWrapper.get(cmd.getSource().getMinecraftServer()).loadProps();
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
		ServerSidePacketRegistry.INSTANCE.register(TRADE_REQUEST, (context, buffer) -> {
			MinecraftServer mcs = context.getPlayer().getServer();
			serverToWrapper.get(mcs).onTradeRequestPacket(context, buffer);
		});
		ServerSidePacketRegistry.INSTANCE.register(TRADE_STATE_C2S, (context, buffer) -> {
			MinecraftServer mcs = context.getPlayer().getServer();
			serverToWrapper.get(mcs).tradeStateChange(context, buffer);
		});
	}
}
