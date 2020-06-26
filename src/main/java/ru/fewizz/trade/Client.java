package ru.fewizz.trade;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import ru.fewizz.trade.client.gui.TradeScreen;
import ru.fewizz.trade.inventory.ClientTradeScreenHandler;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MinecraftClient client = MinecraftClient.getInstance();
		KeyBinding tradeKey = new KeyBinding("key.trade", GLFW.GLFW_KEY_V, "key.categories.gameplay");
		KeyBindingHelper.registerKeyBinding(tradeKey);

		ScreenRegistry.register(ClientTradeScreenHandler.TYPE, TradeScreen.FACTORY);
		
		class TradeStatePacketConsumer implements PacketConsumer {
			final Consumer<TradeState> tsc;
			
			public TradeStatePacketConsumer(Consumer<TradeState> tsc) {
				this.tsc = tsc;
			}
			
			@Override
			public void accept(PacketContext context, PacketByteBuf buffer) {
				int syncID = buffer.readInt();
				TradeState state = TradeState.fromOrdinal(buffer.readInt());
				
				client.execute(() -> {
					if(client.player.currentScreenHandler.syncId != syncID)
						return;
					tsc.accept(state);
				});
			}
		}
		ClientSidePacketRegistry.INSTANCE.register(
			Trade.TRADE_STATE,
			new TradeStatePacketConsumer(state ->
				((ClientTradeScreenHandler)client.player.currentScreenHandler).setState(state, false)
			)
		);
		ClientSidePacketRegistry.INSTANCE.register(
			Trade.OTHER_TRADE_STATE,
			new TradeStatePacketConsumer(state ->
				((ClientTradeScreenHandler)client.player.currentScreenHandler).other.setState(state)
			)
		);
		
		ClientTickCallback.EVENT.register(client0 -> {
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