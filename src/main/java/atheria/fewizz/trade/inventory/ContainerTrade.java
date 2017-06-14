package atheria.fewizz.trade.inventory;

import atheria.fewizz.trade.Trade.TradeState;
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
	TradeState state = TradeState.NOT_READY;
	
	public ContainerTrade(EntityPlayer player, String otherPlayerName, InventoryTrade invR, InventoryTrade invL) {
		this.invR = invR;
		this.invL = invL;
		this.player = player;
		this.otherPlayerName = otherPlayerName;
		
		for(int h = 0; h < 3; h++) {
			for(int w = 0; w < 9; w++) {
				addSlotToContainer(new Slot(player.inventory, h * 9 + w + 9, w * 18 + 6, 103 + h * 18));
			}
		}
		
		for(int x = 0; x < 3; x++) {
			for(int y = 0; y < 3; y++) {
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
		/*if(playerIn.world.isRemote) {
			return;
		}
		PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
		EntityPlayerMP otherPlayer = playerList.getPlayerByUsername(otherPlayerName);
		otherPlayer.closeScreen();*/
	}
	
	public void onPlayerPressedTradeButton() {
		state = state == TradeState.READY ? TradeState.NOT_READY : TradeState.READY;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

}
