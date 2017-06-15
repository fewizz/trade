package atheria.fewizz.trade.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;

public class InventoryTrade extends InventoryBasic {
	public static final int SIZE = 3 * 3;

	public InventoryTrade() {
		super("trade", false, SIZE);
	}

}
