package ru.fewizz.trade;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

import java.util.function.Function;

public abstract class TradeScreenHandler<T extends TradeScreenHandler<T, O>, O extends TradeScreenHandler<O, T>> extends ScreenHandler {
	public final TradeInventory tradeInventory;
	public final O other;
	protected TradeState state = TradeState.NOT_READY;
	
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
