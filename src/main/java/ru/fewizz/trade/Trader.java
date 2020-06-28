package ru.fewizz.trade;

public enum Trader {
	MAIN, OTHER;
	
	public static Trader fromOrdinal(int ord) {
		return VALUES[ord];
	}
	
	static final Trader[] VALUES = values();
}
