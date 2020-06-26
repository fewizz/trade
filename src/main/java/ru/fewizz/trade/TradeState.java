package ru.fewizz.trade;

public enum TradeState {
	READY, NOT_READY;
	
	public TradeState opposite() {
		return this == READY ? TradeState.NOT_READY : READY;
	}
	
	static final TradeState[] VALUES = values();
	public static TradeState fromOrdinal(int ord) {
		return VALUES[ord];
	}
}