package ru.fewizz.trade;

import org.lwjgl.glfw.GLFW;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MinecraftClient client = MinecraftClient.getInstance();
		KeyBinding tradeKey = new KeyBinding("key.trade", GLFW.GLFW_KEY_V, "key.categories.gameplay");
		KeyBindingHelper.registerKeyBinding(tradeKey);

		ScreenRegistry.register(ClientTradeScreenHandler.TYPE, TradeScreen.FACTORY);
		
		ClientSidePacketRegistry.INSTANCE.register(
			Trade.TRADE_STATE,
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
		
		ClientLifecycleEvents.CLIENT_STARTED.register(client0 -> {
			if(tradeKey.isPressed() &&
				client.targetedEntity != null &&
				client.targetedEntity instanceof PlayerEntity &&
				client.currentScreen == null)
			{
				PlayerEntity player = (PlayerEntity) client.targetedEntity;
				PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
				packet.writeUuid(player.getUuid());
				
				ClientSidePacketRegistry.INSTANCE.sendToServer(
					Trade.TRADE_REQUEST,
					packet
				);
			}
		});
	}
}