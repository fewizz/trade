package ru.fewizz.trade.inventory;

import net.minecraft.inventory.SimpleInventory;

public class TradeInventory extends SimpleInventory {
	public static final int SIZE = 3 * 3;

	public TradeInventory() {
		super(SIZE);
	}

}
