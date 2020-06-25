package ru.fewizz.trade.inventory;

import net.minecraft.client.MinecraftClient;

public class TradeClientScreenHandler extends TradeScreenHandler {

	public TradeClientScreenHandler(String otherPlayerName) {
		super(MinecraftClient.getInstance().player, otherPlayerName, 0); // TODO
		otherContainer = new ContainerTradeClientFake(this);
		initSlots();
	}
}
