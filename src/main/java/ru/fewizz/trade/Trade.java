package ru.fewizz.trade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.Command;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Identifier;

public class Trade implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger();
	
	public static final Identifier
		TRADE_REQUEST = new Identifier("trade", "request"),
		TRADE_STATE_C2S = new Identifier("trade", "state_c2s"),
		TRADE_STATE_S2C = new Identifier("trade", "state_s2c"),
		TRADE_START = new Identifier("trade", "start");
	
	static public boolean unlimited_trade;
	static public int trade_distance;
	static public int request_time;
	static public int swap_time;
	
	@Override
	public void onInitialize() {
		loadProps();
		Map<MinecraftServer, Server> serverRef = new IdentityHashMap<>();
		
		ServerLifecycleEvents.SERVER_STARTED.register(server -> serverRef.put(server, new Server(server)));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> serverRef.remove(server));
		
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
						loadProps();
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
	
	public static void loadProps() {
		File config = new File(FabricLoader.getInstance().getConfigDirectory(), "trade.properties");
		Properties properties = new Properties();
		
		if (config.exists()) {
			try (FileInputStream stream = new FileInputStream(config)) {
				properties.load(stream);
			} catch (IOException e) {
				LOGGER.warn("Could not read property file '" + config.getAbsolutePath() + "'", e);
			}
		}
		
		unlimited_trade = Boolean.valueOf((String)properties.computeIfAbsent("unlimited_trade", str -> "false"));
		trade_distance = Integer.valueOf((String)properties.computeIfAbsent("trade_distance", str -> "5"));
		request_time = Integer.valueOf((String)properties.computeIfAbsent("request_time_second", str -> "10"));
		swap_time = Integer.valueOf((String)properties.computeIfAbsent("trade_swap_time_second", str -> "3"));
		
		try (FileOutputStream stream = new FileOutputStream(config)) {
			properties.store(stream, "Note that all commands are applied only on (dedicated/integrated) server side");
		} catch (IOException e) {
			LOGGER.warn("Could not store property file '" + config.getAbsolutePath() + "'", e);
		}
	}
}
