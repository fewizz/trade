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

class Server {
	final MinecraftServer server;
	final Map<PlayerEntity, Set<PlayerEntity>> requests = new HashMap<>();
	
	Server(MinecraftServer server) {
		this.server = server;
	}
	
	void onTradeStateChange(PacketContext context, PacketByteBuf buf) {
		int syncID = buf.readInt();
		TradeState state = TradeState.fromOrdinal(buf.readInt());
		
		server.execute(() -> {
			ScreenHandler sh = context.getPlayer().currentScreenHandler;
			if(sh.syncId != syncID)
				return;
			((ServerTradeScreenHandler)sh).setState(state, Trader.OTHER);
		});
	}
	
	void onTradeRequestPacket(PacketContext context, PacketByteBuf buf) {
		UUID otherPlayerUUID = buf.readUuid();
		
		server.execute(() -> {
			ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
			ServerPlayerEntity otherPlayer = server.getPlayerManager().getPlayer(otherPlayerUUID);
			
			Set<PlayerEntity> requested = requests.computeIfAbsent(player, p -> new HashSet<>());
			Set<PlayerEntity> otherRequested =
					requests.computeIfAbsent(otherPlayer, p -> new HashSet<>());
			
			if(otherRequested.contains(player)) {
				ServerTradeScreenHandler.openFor(player, otherPlayer);
				otherRequested.remove(player);
			}
			else if(!requested.contains(otherPlayer)){
				requested.add(otherPlayer);
				player.sendMessage(new LiteralText("Request sent"), false);
				otherPlayer.sendMessage(
					new LiteralText("Trade request from player " + player.getEntityName()),
					false
				);
			}
		});
	}
}