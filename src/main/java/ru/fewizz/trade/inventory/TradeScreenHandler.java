package ru.fewizz.trade.inventory;

import java.util.concurrent.TimeUnit;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import ru.fewizz.trade.Trade;
import ru.fewizz.trade.Trade.TradeState;

public abstract class TradeScreenHandler extends ScreenHandler {
	final TradeInventory tradeInventory;
	final PlayerEntity player;
	final TradeScreenHandler other;
	public Long swapTimeMillis = null;
	TradeState tradeState = TradeState.NOT_READY;
	
	/*public static final ExtendedScreenHandlerType<TradeScreenHandler> TYPE =
			(ExtendedScreenHandlerType<TradeScreenHandler>)
			ScreenHandlerRegistry.registerExtended(Trade.TRADE_ID, TradeScreenHandler::new);

	TradeScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf bb) {
		this(syncId, inventory.player, bb.readString());
	}*/
	
	TradeScreenHandler(int syncID, PlayerEntity player, PlayerEntity otherPlayer) {
		super(TYPE, syncID);
		TradeScreenHandler other = new TradeScreenHandler(, player, otherPlayer)
	}
	
	TradeScreenHandler(int syncID, PlayerEntity player, TradeScreenHandler other) {
		super(TYPE, syncID);
		this.tradeInventory = new TradeInventory();
		this.player = player;
		this.other = other;
		PlayerEntity otherPlayer =
				player.world.getServer().getPlayerManager().getPlayer(otherPlayerName);
	}

	public void initSlots() {
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
						if (getTradeState() == TradeState.READY)
							setTradeState(TradeState.NOT_READY);
						super.onStackChanged(originalItem, itemStack);
					}
				});
				addSlot(new Slot(otherContainer.tradeInventory, x * 3 + y, x * 18 + 107, 19 + y * 18) {
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

	public void setTradeState(TradeState state) {
		this.tradeState = state;

		if (tradeState == otherContainer.tradeState && tradeState == TradeState.READY) {
			swapTimeMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3);
			otherContainer.swapTimeMillis = swapTimeMillis;
		}

		if (tradeState == TradeState.NOT_READY || otherContainer.tradeState == TradeState.NOT_READY) {
			swapTimeMillis = null;
			otherContainer.swapTimeMillis = null;
		}
	}

	public TradeState getTradeState() {
		return tradeState;
	}

	@Override
	public boolean canUse(PlayerEntity playerIn) {
		return true;
	}

}
