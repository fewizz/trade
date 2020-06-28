package ru.fewizz.trade;

import java.util.function.Function;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public abstract class AbstractTradeScreenHandler<T extends AbstractTradeScreenHandler<T, O>, O extends AbstractTradeScreenHandler<O, T>> extends ScreenHandler {
	public final TradeInventory tradeInventory;
	public final O other;
	protected TradeState state = TradeState.NOT_READY;
	public TradeCountdown countdown = null;
	
	@SuppressWarnings("unchecked")
	public AbstractTradeScreenHandler(ScreenHandlerType<?> type, int syncID, Function<T, O> other) {
		super(type, syncID);
		this.tradeInventory = new TradeInventory();
		this.other = other.apply((T) this);
		if(countdown == null) {
			countdown = new TradeCountdown();
			this.other.countdown = countdown;
		}
	}
	
	public TradeState getState() {
		return state;
	}
	
	public void setState(TradeState ts) {
		this.state = ts;
		
		if(state.isNotReady())
			countdown.disable();
	}

	@Override
	public boolean canUse(PlayerEntity playerIn) {return true;}

}
