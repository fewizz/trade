package atheria.fewizz.trade.inventory;

import java.util.concurrent.TimeUnit;

import atheria.fewizz.trade.Trade.TradeState;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

public abstract class ContainerTradeAbstract extends Container {
	final InventoryTrade inventoryTrade;
	final InventoryPlayer inventoryPlayer;
	public final String otherPlayerName;
	public final String playerName;
	TradeState tradeState = TradeState.NOT_READY;
	public ContainerTradeAbstract otherContainer;
	public Long swapTimeMillis = null;

	public ContainerTradeAbstract(EntityPlayer player, String otherPlayerName) {
		this.inventoryTrade = new InventoryTrade();
		this.inventoryPlayer = player.inventory;
		this.otherPlayerName = otherPlayerName;
		this.playerName = player.getName();
	}
	
	public void initSlots() {
		for (int h = 0; h < 3; h++) {
			for (int w = 0; w < 9; w++) {
				addSlotToContainer(new Slot(inventoryPlayer, h * 9 + w + 9, w * 18 + 6, 92 + h * 18));
			}
		} // 0 - 26
		
		for (int x = 0; x < 9; x++) {
			addSlotToContainer(new Slot(inventoryPlayer, x, x * 18 + 6, 150));
		} // 27 - 35
		
		for (int x = 0; x < 3; x++) { // 36 - 53
			for (int y = 0; y < 3; y++) {
				addSlotToContainer(new Slot(inventoryTrade, x * 3 + y, x * 18 + 14, 19 + y * 18) {
					@Override
					public void onSlotChanged() {
						if(getTradeState() == TradeState.READY)
							setTradeState(TradeState.NOT_READY);
						super.onSlotChanged();
					}
				});
				addSlotToContainer(new Slot(otherContainer.inventoryTrade, x * 3 + y, x * 18 + 107, 19 + y * 18) {
					@Override
					public boolean canTakeStack(EntityPlayer playerIn) {
						return false;
					}

					@Override
					public boolean isItemValid(ItemStack stack) {
						return false;
					}
				});
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slot = this.inventorySlots.get(index);
        ItemStack stack = slot.getStack();

        //if (slot == null || stack == ItemStack.EMPTY || (index & 0x1) == 1){
        //	return ItemStack.EMPTY;
        //}
        
        if(index >= 27 && index <= 35) {
        	mergeItemStack(stack, 0, 27, false);
        }
        else if(index <= 35) {
        	mergeItemStack(stack, 36, 54, false);
        }
        else if(index >= 36 && index < 54) {
        	mergeItemStack(stack, 0, 36, false);
        }
        
        if(slot != null)
        	slot.onSlotChanged();
        
        return ItemStack.EMPTY;
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
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

}
