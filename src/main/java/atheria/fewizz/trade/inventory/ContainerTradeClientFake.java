package atheria.fewizz.trade.inventory;

import net.minecraft.client.Minecraft;

public class ContainerTradeClientFake extends ContainerTradeAbstract {

	public ContainerTradeClientFake(ContainerTradeAbstract contaier) {
		super(Minecraft.getMinecraft().player, null);
		this.otherContainer = contaier;
	}
}
