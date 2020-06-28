package ru.fewizz.trade;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.world.level.ServerWorldProperties;

class Server {
	final MinecraftServer server;
	final Map<PlayerEntity, Set<PlayerEntity>> requests = new HashMap<>();
	
	Server(MinecraftServer server) {
		this.server = server;
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
				new LiteralText("You can't trade with yourseld =P"),
				false
			);
			return;
		}
		if(!Trade.unlimited_trade && requester.distanceTo(acquirer) > Trade.trade_distance) {
			requester.sendMessage(
				new LiteralText("Your'e too far, permitted distance is " + Trade.trade_distance),
				false
			);
			return;
		}
		
		Set<PlayerEntity> requestedPlayers = requests.computeIfAbsent(requester, p -> new HashSet<>());
		Set<PlayerEntity> acquirerRequestedPlayers =
				requests.computeIfAbsent(acquirer, p -> new HashSet<>());
		
		if(acquirerRequestedPlayers.contains(requester)) {
			ServerTradeScreenHandler.openFor(requester, acquirer);
			removeRequest(acquirer, requester);
		}
		else if(!requestedPlayers.contains(acquirer)) {
			requestedPlayers.add(acquirer);
			requester.sendMessage(new LiteralText("Request sent"), false);
			acquirer.sendMessage(
				new LiteralText("Trade request from player " + requester.getEntityName()),
				false
			);
			ServerWorldProperties wp = server
					.getSaveProperties()
					.getMainWorldProperties();
			wp
				.getScheduledEvents()
				.setEvent(
					requestEventName(requester, acquirer),
					wp.getTime()+Trade.request_time*20,
					(server, timer, time) -> {
						requester.sendMessage(
							new LiteralText("Trade request for player " + acquirer.getEntityName() + " is not acquired"),
							false
						);
						removeRequest(requester, acquirer);
					}
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