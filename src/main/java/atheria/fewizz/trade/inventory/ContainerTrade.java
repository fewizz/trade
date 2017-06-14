package atheria.fewizz.trade.inventory;

import javax.annotation.Nullable;

import atheria.fewizz.trade.Trade;
import atheria.fewizz.trade.Trade.TradeState;
import atheria.fewizz.trade.Trade.TradeState.State;
import atheria.fewizz.trade.packet.MessageTradeState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerTrade extends Container {
	final InventoryTrade invR;
	final InventoryTrade invL;
	final EntityPlayer player;
	final String otherPlayerName;
	final private TradeState tradeState;
	final private TradeState otherTradeState;
	final boolean worldIsRemote;

	// Only on server world
	@Nullable
	EntityPlayerMP otherPlayer;
	@Nullable
	ContainerTrade otherContainer;

	public ContainerTrade(EntityPlayer player, String otherPlayerName, TradeState tradeState, TradeState otherTradeState, InventoryTrade invR, InventoryTrade invL) {
		this.invR = invR;
		this.invL = invL;
		this.player = player;
		this.otherPlayerName = otherPlayerName;
		this.tradeState = tradeState;
		this.otherTradeState = otherTradeState;
		this.worldIsRemote = player.world.isRemote;

		if (!worldIsRemote) {
			PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
			otherPlayer = playerList.getPlayerByUsername(otherPlayerName);
			
			if(otherPlayer.openContainer instanceof ContainerTrade) {
				((ContainerTrade)otherPlayer.openContainer).otherContainer = this;
				this.otherContainer = (ContainerTrade) otherPlayer.openContainer;
			}
		}

		for (int h = 0; h < 3; h++) {
			for (int w = 0; w < 9; w++) {
				addSlotToContainer(new Slot(player.inventory, h * 9 + w + 9, w * 18 + 6, 103 + h * 18));
			}
		}

		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				addSlotToContainer(new Slot(invR, x * 3 + y, x * 18 + 13, 29 + y * 18));
				addSlotToContainer(new Slot(invL, x * 3 + y, x * 18 + 90, 29 + y * 18) {
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
	public void onContainerClosed(EntityPlayer playerIn) {
		if (!worldIsRemote && otherPlayer.openContainer == otherContainer && otherContainer.otherContainer == this) {
			// Means, that trade is over, prevent infinite loop when container closing =3
			this.otherContainer = null;
			
			otherPlayer.closeScreen();
		}

		for (int i = 0; i < 9; i++) {
			if (!player.inventory.addItemStackToInventory(invL.getStackInSlot(i))) {
				player.dropItem(invL.getStackInSlot(i), false);
			}
		}
	}

	public void setTradeState(TradeState.State state) {
		this.tradeState.state = state;

		if (!worldIsRemote) {
			Trade.NETWORK_WRAPPER.sendTo(new MessageTradeState(this.tradeState.state), otherPlayer);
		}

		if (tradeState.state == otherTradeState.state && tradeState.state == State.READY) {
			for (int i = 0; i < 9; i++) {
				ItemStack stackL = invL.getStackInSlot(i);
				invL.setInventorySlotContents(i, invR.getStackInSlot(i));
				invR.setInventorySlotContents(i, stackL);
			}

			setTradeState(State.NOT_READY);
			otherContainer.setTradeState(State.NOT_READY);
		}
	}

	@SideOnly(Side.CLIENT)
	public void setOtherPlayerTradeState(TradeState.State state) {
		this.otherTradeState.state = state;
	}

	public TradeState getTradeState() {
		return tradeState;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

}
