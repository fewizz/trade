package ru.fewizz.trade;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import ru.fewizz.trade.client.gui.TradeScreen;
import ru.fewizz.trade.inventory.TradeScreenHandler;

public class Trade implements ModInitializer {
	public static final Identifier TRADE_REQUEST_ID =
			new Identifier("trade", "request");
	public static final Identifier TRADE_ID =
			new Identifier("trade", "trade");
	
	@Override
	public void onInitialize() {
		Map<MinecraftServer, Server> serverRef = new IdentityHashMap<>();
		
		ServerStartCallback.EVENT.register(server -> {
			serverRef.put(server, new Server(server));
		});
		ServerStopCallback.EVENT.register(server -> {
			serverRef.remove(server);
		});
		
		ServerSidePacketRegistry.INSTANCE.register(TRADE_REQUEST_ID, (context, buffer) -> {
			MinecraftServer mcs = context.getPlayer().getServer();
			serverRef.get(mcs).onTradeRequestPacket(context, buffer);
			
			/*context
			.getPlayer()
			.openHandledScreen(
				new ExtendedScreenHandlerFactory() {
					@Override
					public TradeScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
						// TODO Auto-generated method stub
						return null;
					}
					@Override
					public Text getDisplayName() {
						return new LiteralText("");
					}
					@Override
					public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
						buf.write
					}
				}
			);*/
		});
	}
	
	class Server {
		final MinecraftServer server;
		final Map<PlayerEntity, Set<PlayerEntity>> requests = new HashMap<>();
		
		Server(MinecraftServer server) {
			this.server = server;
		}
		
		void onTradeRequestPacket(PacketContext context, PacketByteBuf buf) {
			String otherPlayerName = buf.readString();
			
			server.execute(() -> {
				PlayerEntity player = context.getPlayer();
				PlayerEntity otherPlayer = server.getPlayerManager().getPlayer(otherPlayerName);
				
				Set<PlayerEntity> requested = requests.computeIfAbsent(player, p -> new HashSet<>());
				Set<PlayerEntity> otherRequested =
						requests.computeIfAbsent(otherPlayer, p -> new HashSet<>());
				
				if(otherRequested.contains(player)) {
					//Accept
					
					otherRequested.remove(otherPlayer);
				}
				else {
					requested.add(otherPlayer);
					player.sendMessage(new LiteralText("Request sent"), false);
					otherPlayer.sendMessage(
						new LiteralText("Trade request from player " + player.getName()),
						false
					);
				}
			});
		}
	}

	public static class Client implements ClientModInitializer {
		static KeyBinding tradeKey;
			
		@Override
		public void onInitializeClient() {
			tradeKey = new KeyBinding("key.trade", GLFW.GLFW_KEY_V, "key.categories.gameplay");
			KeyBindingHelper.registerKeyBinding(tradeKey);

			ScreenRegistry.register(TradeScreenHandler.TYPE, TradeScreen.FACTORY);
			
			ClientTickCallback.EVENT.register(client -> {
				if(tradeKey.isPressed() &&
					client.targetedEntity != null &&
					client.targetedEntity instanceof PlayerEntity)
				{
					PlayerEntity player = (PlayerEntity) client.targetedEntity;
					PacketByteBuf pbf = new PacketByteBuf(Unpooled.buffer());
					pbf.writeString(player.getEntityName());
					
					ClientSidePacketRegistry.INSTANCE.sendToServer(
						TRADE_REQUEST_ID,
						pbf
					);
				}
			});
		}
	}
	
	public enum TradeState {
		READY, NOT_READY;

		public TradeState opposite() {
			return this == READY ? TradeState.NOT_READY : READY;
		}
	}
}
