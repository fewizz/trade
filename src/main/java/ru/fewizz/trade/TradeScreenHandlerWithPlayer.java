package ru.fewizz.trade;

import java.util.function.Function;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

import static net.minecraft.item.ItemStack.EMPTY;
import static ru.fewizz.trade.TradeScreen.*;

public class TradeScreenHandlerWithPlayer<
	T extends TradeScreenHandlerWithPlayer<T, O, P>,
	O extends AbstractTradeScreenHandler<O, T>,
	P extends PlayerEntity
>
	extends AbstractTradeScreenHandler<T, O>
{
	static final int SLOT_SIZE = 18;
	public final P player;

	public TradeScreenHandlerWithPlayer(ScreenHandlerType<?> type, int syncID, P player, Function<T, O> other) {
		super(type, syncID, other);
		this.player = player;
		
		for (int h = 0; h < 3; h++) {
			for (int w = 0; w < 9; w++) {
				addSlot(new Slot(player.inventory, h * 9 + w + 9, 8 + w * SLOT_SIZE, BASE_Y + 8 + h * SLOT_SIZE));
			}
		} // 0 - 26

		for (int x = 0; x < 9; x++) {
			addSlot(new Slot(player.inventory, x, x * 18 + 8, H - 6 - SLOT_SIZE));
		} // 27 - 35

		for (int x = 0; x < 3; x++) { // 36 - 53
			for (int y = 0; y < 3; y++) {
				addSlot(new Slot(tradeInventory, x * 3 + y, x * 18 + SLOTS_X + 1, SLOTS_Y + 1 + y * 18) {
					@Override
					public void onStackChanged(ItemStack originalItem, ItemStack itemStack) {
						if (getState().isReady())
							setState(TradeState.NOT_READY);
						super.onStackChanged(originalItem, itemStack);
					}
				});
				addSlot(new Slot(this.other.tradeInventory, x * 3 + y, x * 18 + W - SLOTS_X - SLOTS_SIZE + 1, SLOTS_Y + 1 + y * 18) {
					@Override
					public boolean canTakeItems(PlayerEntity player) {
						return false;
					}

					@Override
					public boolean canInsert(ItemStack stack) {
						return false;
					}
				});
			}
		}
	}
	
	@Override
	public ItemStack transferSlot(PlayerEntity player, int index) {
		ItemStack itemstack = EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index >= 27 && index <= 35) {
				if (!insertItem(itemstack1, 0, 27, false))
					return EMPTY;
			} else if (index <= 35) {
				if (!insertItem(itemstack1, 36, 54, false))
					return EMPTY;
			} else if (index >= 36 && index < 54) {
				if (!insertItem(itemstack1, 0, 36, false))
					return EMPTY;
			}

			if (itemstack1.isEmpty())
				slot.setStack(EMPTY);

			else
				slot.markDirty();

			if (itemstack1.getCount() == itemstack.getCount())
				return EMPTY;

			slot.onTakeItem(player, itemstack1);
		}

		return itemstack;
	}

}
