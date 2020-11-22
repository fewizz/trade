package ru.fewizz.trade;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.level.ServerWorldProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class ServerWrapper {
	public static final Logger LOGGER = LogManager.getLogger();

	final MinecraftServer server;
	final Map<PlayerEntity, Set<PlayerEntity>> requests = new HashMap<>();

	public boolean unlimited_trade;
	public int trade_distance;
	public int request_time;
	public int swapTime;

	ServerWrapper(MinecraftServer server) {
		this.server = server;

		loadProps();
	}

	public void loadProps() {
		Path config = FabricLoader.getInstance().getConfigDir().resolve("trade.properties");
		Properties properties = new Properties();

		if (Files.exists(config)) {
			try (Reader bufferedReader = Files.newBufferedReader(config)) {
				properties.load(bufferedReader);
			} catch (IOException e) {
				LOGGER.warn("Could not read property file '" + config.toAbsolutePath() + "'", e);
			}
		}

		unlimited_trade = Boolean.parseBoolean((String)properties.computeIfAbsent("unlimited_trade", str -> "false"));
		trade_distance = Integer.parseInt((String)properties.computeIfAbsent("trade_distance", str -> "5"));
		request_time = Integer.parseInt((String)properties.computeIfAbsent("request_time_second", str -> "10"));
		swapTime = Integer.parseInt((String)properties.computeIfAbsent("trade_swap_time_second", str -> "3"));

		try (Writer writer = Files.newBufferedWriter(config)) {
			properties.store(writer, "Note that all commands are applied only on (dedicated/integrated) server side");
		} catch (IOException e) {
			LOGGER.warn("Could not store property file '" + config.toAbsolutePath() + "'", e);
		}
	}
	
	void tradeStateChange(PacketContext context, PacketByteBuf buf) {
		int syncID = buf.readInt();
		TradeState state = TradeState.fromOrdinal(buf.readInt());
		
		server.execute(() -> {
			ScreenHandler sh = context.getPlayer().currentScreenHandler;
			if(sh.syncId != syncID)
				return;
			((ServerTradeScreenHandler)sh).setState(state, Trader.OTHER);
		});
	}
	
	void tradeRequest(ServerPlayerEntity requester, ServerPlayerEntity acquirer) {
		if(requester == acquirer) {
			requester.sendMessage(
				new TranslatableText("trade.request.yourself"),
				false
			);
			return;
		}
		if(!unlimited_trade) {
			if(requester.world != acquirer.world) {
				requester.sendMessage(
					new TranslatableText("trade.request.diff_world"),
					false
				);
				return;
			}
			
			if(requester.distanceTo(acquirer) > trade_distance) {
				requester.sendMessage(
					new TranslatableText("trade.request.too_far", trade_distance),
					false
				);
				return;
			}
		}
		
		Set<PlayerEntity> requestedPlayers = requests.computeIfAbsent(requester, p -> new HashSet<>());
		Set<PlayerEntity> acquirerRequestedPlayers =
				requests.computeIfAbsent(acquirer, p -> new HashSet<>());
		
		if(acquirerRequestedPlayers.contains(requester)) {
			ServerTradeScreenHandler.openFor(requester, acquirer, this);
			removeRequest(acquirer, requester);
		}
		else if(!requestedPlayers.contains(acquirer)) {
			requestedPlayers.add(acquirer);
			requester.sendMessage(new TranslatableText("trade.request.sent"), false);
			acquirer.sendMessage(
				new TranslatableText("trade.request.from", requester.getEntityName()),
				false
			);
			ServerWorldProperties wp = server
				.getSaveProperties()
				.getMainWorldProperties();

			wp
				.getScheduledEvents()
				.setEvent(
					requestEventName(requester, acquirer),
					wp.getTime()+request_time*20,
					new NotSerializableTimerCallback((server, timer, time) -> {
						requester.sendMessage(
							new TranslatableText("trade.request.not_acquired", acquirer.getEntityName()),
							false
						);
						removeRequest(requester, acquirer);
					})
				);
		}
	}
	
	void removeRequest(ServerPlayerEntity requester, ServerPlayerEntity acquirer) {
		Set<PlayerEntity> requestedPlayers = requests.computeIfAbsent(requester, p -> new HashSet<>());
		requestedPlayers.remove(acquirer);
		
		server
			.getSaveProperties()
			.getMainWorldProperties()
			.getScheduledEvents()
			.method_22593(requestEventName(requester, acquirer));
	}
	
	private String requestEventName(ServerPlayerEntity requester, ServerPlayerEntity acquirer) {
		return "trade:request"+"\\"+requester.getUuidAsString()+"\\"+acquirer.getUuidAsString();
	}
	
	void onTradeRequestPacket(PacketContext context, PacketByteBuf buf) {
		UUID acqUUID = buf.readUuid();
		
		server.execute(() -> {
			ServerPlayerEntity req = (ServerPlayerEntity) context.getPlayer();
			ServerPlayerEntity acq = server.getPlayerManager().getPlayer(acqUUID);
			
			tradeRequest(req, acq);
		});
	}
}