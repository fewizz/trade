package ru.fewizz.trade;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Identifier;

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
		
		Map<MinecraftServer, ServerWrapper> serverRef = new IdentityHashMap<>();
		
		ServerLifecycleEvents.SERVER_STARTED.register(server -> serverRef.put(server, new ServerWrapper(server)));
		ServerLifecycleEvents.SERVER_STOPPED.register(serverRef::remove);
		
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher
				.register(
				CommandManager.literal("trade").
				then(
					CommandManager.argument("player", EntityArgumentType.player())
					.executes(cmd -> {
						serverRef.get(
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
						serverRef.get(cmd.getSource().getMinecraftServer()).loadProps();
						return Command.SINGLE_SUCCESS;
					})
				)
			);
		});
		ServerSidePacketRegistry.INSTANCE.register(TRADE_REQUEST, (context, buffer) -> {
			MinecraftServer mcs = context.getPlayer().getServer();
			serverRef.get(mcs).onTradeRequestPacket(context, buffer);
		});
		ServerSidePacketRegistry.INSTANCE.register(TRADE_STATE_C2S, (context, buffer) -> {
			MinecraftServer mcs = context.getPlayer().getServer();
			serverRef.get(mcs).tradeStateChange(context, buffer);
		});
	}
}
