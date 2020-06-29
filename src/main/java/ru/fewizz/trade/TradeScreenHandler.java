package ru.fewizz.trade;

import java.util.UUID;
import java.util.function.Function;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import ru.fewizz.trade.client.ClientTradeScreenHandler;
import ru.fewizz.trade.client.OtherClientTradeScreenHandler;

public abstract class TradeScreenHandler<T extends TradeScreenHandler<T, O>, O extends TradeScreenHandler<O, T>> extends ScreenHandler {
	public final TradeInventory tradeInventory;
	public final O other;
	protected TradeState state = TradeState.NOT_READY;
	
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
	
	@SuppressWarnings("unchecked")
	public TradeScreenHandler(ScreenHandlerType<?> type, int syncID, Function<T, O> otherTSHFactory) {
		super(type, syncID);
		this.tradeInventory = new TradeInventory();
		this.other = otherTSHFactory.apply((T) this);
	}
	
	public TradeState getState() {
		return state;
	}
	
	public void setState(TradeState ts) {
		this.state = ts;
	}

	@Override
	public boolean canUse(PlayerEntity playerIn) {return true;}

}
