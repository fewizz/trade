package atheria.fewizz.trade.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;

public class InventoryTrade extends InventoryBasic {

	public InventoryTrade() {
		super("trade", false, 9);
	}

	/*public void swapIemsToInwentory() {
		for (int i = 0; i < getSizeInventory(); i++) {
			player.inventory.addItemStackToInventory(getStackInSlot(i));
		}
	}*/

}
