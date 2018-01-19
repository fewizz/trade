package atheria.fewizz.trade.inventory;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import atheria.fewizz.trade.Trade;
import atheria.fewizz.trade.Trade.TradeState;
import atheria.fewizz.trade.packet.MessageTradeState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		}

		for (int x = 0; x < 9; x++) {
			addSlotToContainer(new Slot(inventoryPlayer, x, x * 18 + 6, 150));
		}

		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				addSlotToContainer(new Slot(inventoryTrade, x * 3 + y, x * 18 + 14, 19 + y * 18) {
					@Override
					public void onSlotChanged() {
						if (getTradeState() == TradeState.READY)
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

		if (slot == null || stack == null || (index & 0x1) == 1) {
			return null;
		}

		if (index >= 36 && index < 54) {
			if (!mergeItemStack(stack, 0, 35, false)) {
				return null;
			}
		}

		return stack;
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
