package ru.fewizz.trade.client;

import java.util.UUID;
import java.util.function.Function;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import ru.fewizz.trade.Trade;
import ru.fewizz.trade.TradeScreenHandlerWithPlayer;
import ru.fewizz.trade.TradeState;

public class ClientTradeScreenHandler
extends TradeScreenHandlerWithPlayer<
	ClientTradeScreenHandler,
	OtherClientTradeScreenHandler,
	ClientPlayerEntity
> {

	public static final ScreenHandlerType<ClientTradeScreenHandler> TYPE =
		ScreenHandlerRegistry.registerExtended(
			new Identifier("trade", "trade"),
			(int syncId, PlayerInventory inventory, PacketByteBuf bb) -> {
				UUID otherPlayerUUID = bb.readUuid();
				return new ClientTradeScreenHandler(
					syncId,
					tsh ->
					new OtherClientTradeScreenHandler(
						otherPlayerUUID,
						tsh1 -> tsh
					)
				); 						
			}
		);
			
	@SuppressWarnings("resource")
	private ClientTradeScreenHandler(int syncID, Function<ClientTradeScreenHandler,
			OtherClientTradeScreenHandler> other)
	{
		super(null, syncID, MinecraftClient.getInstance().player, other);
	}
	
	@Override
	public void setState(TradeState s) {
		setState(s, true);
	}
	
	public void setState(TradeState s, boolean sendChanges) {
		if(sendChanges) {
			PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
			packet.writeInt(syncId);
			packet.writeInt(s.ordinal());
		
			ClientSidePacketRegistry.INSTANCE.sendToServer(Trade.TRADE_STATE_C2S, packet);
		}
		
		super.setState(s);
	}
}
