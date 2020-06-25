package ru.fewizz.trade.inventory;

import ru.fewizz.trade.Trade;
import ru.fewizz.trade.Trade.TradeState;
import ru.fewizz.trade.packet.MessageTradeState;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerListener;

public class TradeServerScreenHandler extends TradeScreenHandler {

	public TradeServerScreenHandler(int syncId, PlayerEntity player, String otherPlayerName) {
		super(syncId, player, otherPlayerName);
	}
	
	@Override
	public void addListener(ScreenHandlerListener listener) {
		super.addListener(listener);
		PlayerEntity otherPlayer = getOtherPlayer();
		
		if (otherPlayer.currentScreenHandler instanceof TradeScreenHandler) {
			((TradeScreenHandler) otherPlayer.currentScreenHandler).otherContainer = this;
			this.otherContainer = (TradeScreenHandler) otherPlayer.currentScreenHandler;
			this.initSlots();
			this.otherContainer.initSlots();
			sendContentUpdates();
		}
	}

	@Override
	public void sendContentUpdates() {
		super.sendContentUpdates();
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
		for (int i = 0; i < TradeInventory.SIZE; i++) {
			ItemStack stack = tradeInventory.getStack(i);
			tradeInventory.setInventorySlotContents(i, otherContainer.tradeInventory.getStack(i));
			otherContainer.tradeInventory.setInventorySlotContents(i, stack);
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
	public void onContainerClosed(PlayerEntity player) {
		if (getOtherPlayer().openContainer == otherContainer && otherContainer.otherContainer == this) {
			// Means that trade is over, prevent infinite loop when container closing =3
			this.otherContainer = null;

			getOtherPlayer().closeScreen();
		}

		for (int i = 0; i < TradeInventory.SIZE; i++) {
			if (!player.inventory.addItemStackToInventory(tradeInventory.getStackInSlot(i))) {
				player.dropItem(tradeInventory.getStackInSlot(i), false);
			}
		}
	}
}
