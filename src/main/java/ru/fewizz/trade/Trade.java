package ru.fewizz.trade;

import java.util.IdentityHashMap;
import java.util.Map;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class Trade implements ModInitializer {
	public static final Identifier
		TRADE_REQUEST = new Identifier("trade", "request"),
		TRADE_STATE = new Identifier("trade", "state"),
		OTHER_TRADE_STATE = new Identifier("trade", "other_state");
	
	@Override
	public void onInitialize() {
		Map<MinecraftServer, Server> serverRef = new IdentityHashMap<>();
		
		ServerStartCallback.EVENT.register(server -> serverRef.put(server, new Server(server)));
		ServerStopCallback.EVENT.register(server -> serverRef.remove(server));
		
		ServerSidePacketRegistry.INSTANCE.register(TRADE_REQUEST, (context, buffer) -> {
			MinecraftServer mcs = context.getPlayer().getServer();
			serverRef.get(mcs).onTradeRequestPacket(context, buffer);
		});
		ServerSidePacketRegistry.INSTANCE.register(TRADE_STATE, (context, buffer) -> {
			MinecraftServer mcs = context.getPlayer().getServer();
			serverRef.get(mcs).onTradeStateChange(context, buffer);
		});
	}
}
