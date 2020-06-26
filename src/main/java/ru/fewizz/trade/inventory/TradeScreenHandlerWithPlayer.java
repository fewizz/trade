package ru.fewizz.trade.inventory;

import java.util.function.Function;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import ru.fewizz.trade.TradeState;

public class TradeScreenHandlerWithPlayer<
	T extends TradeScreenHandlerWithPlayer<T, O, P>,
	O extends AbstractTradeScreenHandler<O, T>,
	P extends PlayerEntity
>
	extends AbstractTradeScreenHandler<T, O>
{
	public final P player;

	public TradeScreenHandlerWithPlayer(ScreenHandlerType<?> type, int syncID, P player, Function<T, O> other) {
		super(type, syncID, other);
		this.player = player;
		
		for (int h = 0; h < 3; h++) {
			for (int w = 0; w < 9; w++) {
				addSlot(new Slot(player.inventory, h * 9 + w + 9, w * 18 + 6, 92 + h * 18));
			}
		} // 0 - 26

		for (int x = 0; x < 9; x++) {
			addSlot(new Slot(player.inventory, x, x * 18 + 6, 150));
		} // 27 - 35

		for (int x = 0; x < 3; x++) { // 36 - 53
			for (int y = 0; y < 3; y++) {
				addSlot(new Slot(tradeInventory, x * 3 + y, x * 18 + 14, 19 + y * 18) {
					@Override
					public void onStackChanged(ItemStack originalItem, ItemStack itemStack) {
						if (getState() == TradeState.READY)
							setState(TradeState.NOT_READY);
						super.onStackChanged(originalItem, itemStack);
					}
				});
				addSlot(new Slot(this.other.tradeInventory, x * 3 + y, x * 18 + 107, 19 + y * 18) {
					@Override
					public boolean canTakeItems(PlayerEntity playerIn) {
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

}
