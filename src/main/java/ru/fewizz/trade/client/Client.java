package ru.fewizz.trade.client;

import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import ru.fewizz.trade.Trade;
import ru.fewizz.trade.TradeState;
import ru.fewizz.trade.Trader;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MinecraftClient client = MinecraftClient.getInstance();
		KeyBinding tradeKey = new KeyBinding("key.trade", GLFW.GLFW_KEY_V, "key.categories.gameplay");
		KeyBindingHelper.registerKeyBinding(tradeKey);

		ScreenRegistry.register(ClientTradeScreenHandler.TYPE, TradeScreen.FACTORY);
		
		ClientSidePacketRegistry.INSTANCE.register(
			Trade.TRADE_STATE_S2C,
			(ctx, buffer) -> {
				int syncID = buffer.readInt();
				Trader trader = Trader.fromOrdinal(buffer.readInt());
				TradeState state = TradeState.fromOrdinal(buffer.readInt());
				
				client.execute(() -> {
					ScreenHandler sh = client.player.currentScreenHandler;
					if(sh.syncId != syncID)
						return;
					
					ClientTradeScreenHandler tsh = (ClientTradeScreenHandler) sh;
					if(trader == Trader.MAIN)
						tsh.setState(state, false);
					else
						tsh.other.setState(state);
				});
			}
		);
		ClientSidePacketRegistry.INSTANCE.register(
			Trade.TRADE_START,
			(ctx, buffer) -> {
				int syncID = buffer.readInt();
				int seconds = buffer.readInt();
				
				ctx.getTaskQueue().execute(() -> {
					ScreenHandler sh = client.player.currentScreenHandler;
					if(sh.syncId != syncID)
						return;
					((ClientTradeScreenHandler) sh).countdown.enableForSeconds(seconds);
				});
			}
		);
		
		ClientTickEvents.START_CLIENT_TICK.register(client0 -> {
			if(tradeKey.isPressed() &&
				client.targetedEntity != null &&
				client.targetedEntity instanceof PlayerEntity &&
				client.currentScreen == null)
			{
				PlayerEntity player = (PlayerEntity) client.targetedEntity;
				sendRequest(player.getUuid());
			}
		});
	}
	
	static void sendRequest(UUID uuid) {
		PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
		packet.writeUuid(uuid);
		
		ClientSidePacketRegistry.INSTANCE.sendToServer(
			Trade.TRADE_REQUEST,
			packet
		);
	}
}