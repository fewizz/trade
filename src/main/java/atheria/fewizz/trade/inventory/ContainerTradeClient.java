package atheria.fewizz.trade.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerTradeClient extends ContainerTradeAbstract {

	public ContainerTradeClient(String otherPlayerName) {
		super(Minecraft.getMinecraft().player, otherPlayerName);
		otherContainer = new ContainerTradeClientFake(this);
		initSlots();
	}
}
