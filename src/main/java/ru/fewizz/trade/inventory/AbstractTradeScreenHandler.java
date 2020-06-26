package ru.fewizz.trade.inventory;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import ru.fewizz.trade.TradeState;

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
		
		if (s == other.getState() && s == TradeState.READY) {
			swapTimeMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3);
			other.swapTimeMillis = swapTimeMillis;
		}

		if (s == TradeState.NOT_READY || other.getState() == TradeState.NOT_READY) {
			swapTimeMillis = null;
			other.swapTimeMillis = null;
		}
	}

	@Override
	public ItemStack transferSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index >= 27 && index <= 35) {
				if (!insertItem(itemstack1, 0, 27, false))
					return ItemStack.EMPTY;
			} else if (index <= 35) {
				if (!insertItem(itemstack1, 36, 54, false))
					return ItemStack.EMPTY;
			} else if (index >= 36 && index < 54) {
				if (!insertItem(itemstack1, 0, 36, false))
					return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty())
				slot.setStack(ItemStack.EMPTY);

			else
				slot.markDirty();

			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;

			slot.onTakeItem(playerIn, itemstack1);
		}

		return itemstack;
	}

	@Override
	public boolean canUse(PlayerEntity playerIn) {
		return true;
	}

}
