package atheria.fewizz.trade.inventory;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import atheria.fewizz.trade.Trade;
import atheria.fewizz.trade.Trade.TradeState;
import atheria.fewizz.trade.packet.MessageTradeState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerTradeServer extends ContainerTradeAbstract {

	public ContainerTradeServer(EntityPlayer player, String otherPlayerName) {
		super(player, otherPlayerName);
	}
	
	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		EntityPlayerMP otherPlayer = getOtherPlayer();
		
		if (otherPlayer.openContainer instanceof ContainerTradeAbstract) {
			((ContainerTradeAbstract) otherPlayer.openContainer).otherContainer = this;
			this.otherContainer = (ContainerTradeAbstract) otherPlayer.openContainer;
			this.initSlots();
			this.otherContainer.initSlots();
			detectAndSendChanges();
		}
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		onTick();
	}

	void onTick() {
		if (otherContainer != null && swapTimeMillis != null && System.currentTimeMillis() >= swapTimeMillis) {
			swapItems();
			setTradeState(TradeState.NOT_READY);
			otherContainer.setTradeState(TradeState.NOT_READY);
			swapTimeMillis = null;
			otherContainer.swapTimeMillis = null;
			
			updateTradeStateOfClients();
		}
	}
	
	public void swapItems() {
		for (int i = 0; i < InventoryTrade.SIZE; i++) {
			ItemStack stack = inventoryTrade.getStackInSlot(i);
			inventoryTrade.setInventorySlotContents(i, otherContainer.inventoryTrade.getStackInSlot(i));
			otherContainer.inventoryTrade.setInventorySlotContents(i, stack);
		}
	}
	
	@Override
	public void setTradeState(TradeState state) {
		super.setTradeState(state);
		updateTradeStateOfClients();
	}
	
	void updateTradeStateOfClients() {
		EntityPlayerMP otherPlayer = getOtherPlayer();

		if (otherPlayer == null) {
			EntityPlayerMP player = getPlayer();
			player.closeScreen();
			player.sendMessage(new TextComponentString("Player '" + otherPlayerName + "' is offline now"));
		}

		Trade.NETWORK_WRAPPER.sendTo(new MessageTradeState(tradeState, otherContainer.tradeState), getPlayer());
		Trade.NETWORK_WRAPPER.sendTo(new MessageTradeState(otherContainer.tradeState, tradeState), otherPlayer);
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		if (getOtherPlayer().openContainer == otherContainer && otherContainer.otherContainer == this) {
			// Means that trade is over, prevent infinite loop when container closing =3
			this.otherContainer = null;

			getOtherPlayer().closeScreen();
		}

		for (int i = 0; i < InventoryTrade.SIZE; i++) {
			if (!player.inventory.addItemStackToInventory(inventoryTrade.getStackInSlot(i))) {
				player.dropItem(inventoryTrade.getStackInSlot(i), false);
			}
		}
	}

	// Because player instance can be recreated
	EntityPlayerMP getOtherPlayer() {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(otherPlayerName);
	}

	EntityPlayerMP getPlayer() {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerName);
	}
}
