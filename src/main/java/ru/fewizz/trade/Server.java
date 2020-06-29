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
import net.minecraft.text.TranslatableText;
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
				new TranslatableText("trade.request.yourself"),
				false
			);
			return;
		}
		if(!Trade.unlimited_trade) {
			if(requester.world != acquirer.world) {
				requester.sendMessage(
					new TranslatableText("trade.request.diff_world"),
					false
				);
				return;
			}
			
			if(requester.distanceTo(acquirer) > Trade.trade_distance) {
				requester.sendMessage(
					new TranslatableText("trade.request.too_far", Trade.trade_distance),
					false
				);
				return;
			}
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
					wp.getTime()+Trade.request_time*20,
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