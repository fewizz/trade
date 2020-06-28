package ru.fewizz.trade;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public abstract class AbstractTradeScreenHandler<T extends AbstractTradeScreenHandler<T, O>, O extends AbstractTradeScreenHandler<O, T>> extends ScreenHandler {
	public final TradeInventory tradeInventory;
	public final O other;
	public Long swapTimeMillis = null;
	protected TradeState state = TradeState.NOT_READY;
	
	@SuppressWarnings("unchecked")
	public AbstractTradeScreenHandler(ScreenHandlerType<?> type, int syncID, Function<T, O> other) {
		super(type, syncID);
		this.tradeInventory = new TradeInventory();
		this.other = other.apply((T) this);
	}
	
	public TradeState getState() {
		return state;
	}
	
	public void setState(TradeState s) {
		state = s;
		
		if (s.isReady() && other.getState().isReady()) {
			swapTimeMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3);
			other.swapTimeMillis = swapTimeMillis;
		}

		if (s.isNotReady() || other.getState().isNotReady()) {
			swapTimeMillis = null;
			other.swapTimeMillis = null;
		}
	}

	@Override
	public boolean canUse(PlayerEntity playerIn) {return true;}

}
