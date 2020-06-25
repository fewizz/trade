package ru.fewizz.trade.inventory;

import net.minecraft.client.MinecraftClient;

public class ContainerTradeClientFake extends TradeScreenHandler {

	public ContainerTradeClientFake(TradeScreenHandler contaier) {
		super(MinecraftClient.getInstance().player, null, 0); // TODO
		this.otherContainer = contaier;
	}
}
