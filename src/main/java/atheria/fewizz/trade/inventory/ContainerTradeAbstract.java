package atheria.fewizz.trade.inventory;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import atheria.fewizz.trade.Trade;
import atheria.fewizz.trade.Trade.TradeState;
import atheria.fewizz.trade.Trade.TradeState.State;
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
	final String otherPlayerName;
	final String playerName;
	protected final TradeState tradeState;
	ContainerTradeAbstract otherContainer;
	Long swapTimeMillis = null;

	public ContainerTradeAbstract(EntityPlayer player, String otherPlayerName) {
		this.inventoryTrade = new InventoryTrade();
		this.inventoryPlayer = player.inventory;
		this.otherPlayerName = otherPlayerName;
		this.playerName = player.getName();
		this.tradeState = new TradeState();
	}
	
	public void initSlots() {
		for (int h = 0; h < 3; h++) {
			for (int w = 0; w < 9; w++) {
				addSlotToContainer(new Slot(inventoryPlayer, h * 9 + w + 9, w * 18 + 6, 103 + h * 18));
			}
		}
		
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				addSlotToContainer(new Slot(inventoryTrade, x * 3 + y, x * 18 + 13, 29 + y * 18));
				addSlotToContainer(new Slot(otherContainer.inventoryTrade, x * 3 + y, x * 18 + 90, 29 + y * 18) {
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

	public void setTradeState(State state) {
		this.tradeState.state = state;

		if (tradeState.state == otherContainer.tradeState.state && tradeState.state == State.READY) {
			swapTimeMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3);
		}

		if (tradeState.state == State.NOT_READY || otherContainer.tradeState.state == State.NOT_READY) {
			swapTimeMillis = null;
		}
	}

	public void setOtherPlayerTradeState(State state) {
		this.otherContainer.tradeState.state = state;
	}

	public TradeState getTradeState() {
		return tradeState;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

}
