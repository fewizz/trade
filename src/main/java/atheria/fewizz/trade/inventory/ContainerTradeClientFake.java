package atheria.fewizz.trade.inventory;

import atheria.fewizz.trade.Trade.TradeState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerTradeClientFake extends ContainerTradeAbstract {

	public ContainerTradeClientFake(ContainerTradeAbstract contaier) {
		super(Minecraft.getMinecraft().player, null);
		this.otherContainer = contaier;
	}
}
