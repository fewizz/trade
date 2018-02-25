package atheria.fewizz.trade.inventory;

import net.minecraft.client.Minecraft;

public class ContainerTradeClient extends ContainerTradeAbstract {

	public ContainerTradeClient(String otherPlayerName) {
		super(Minecraft.getMinecraft().player, otherPlayerName);
		otherContainer = new ContainerTradeClientFake(this);
		initSlots();
	}
}
