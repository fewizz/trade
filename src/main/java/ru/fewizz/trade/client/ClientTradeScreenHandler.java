package ru.fewizz.trade.client;

import java.util.function.Function;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import ru.fewizz.trade.Trade;
import ru.fewizz.trade.TradeScreenHandlerWithPlayer;
import ru.fewizz.trade.TradeState;

public class ClientTradeScreenHandler
extends TradeScreenHandlerWithPlayer<
	ClientTradeScreenHandler,
	OtherClientTradeScreenHandler,
	ClientPlayerEntity
> {
	
	public TradeCountdown countdown = null;
			
	@SuppressWarnings("resource")
	public ClientTradeScreenHandler(int syncID, Function<ClientTradeScreenHandler,
			OtherClientTradeScreenHandler> otherTSHFactory)
	{
		super(null, syncID, MinecraftClient.getInstance().player, otherTSHFactory);
		
		countdown = new TradeCountdown();
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
		if(s.isNotReady())
			countdown.disable();
	}
}
