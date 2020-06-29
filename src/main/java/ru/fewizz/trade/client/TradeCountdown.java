package ru.fewizz.trade.client;

public class TradeCountdown {
	long endTimeMillis = Long.MIN_VALUE;
	
	public boolean isEnabled() {
		return endTimeMillis != Long.MIN_VALUE;
	}
	
	public void enableForSeconds(long forSecs) {
		endTimeMillis = System.currentTimeMillis() + forSecs*1000;
	}
	
	public void disable() {
		endTimeMillis = Long.MIN_VALUE;
	}
	
	long millisLeft() {
		long leftMillis = endTimeMillis - System.currentTimeMillis();
		return leftMillis < 0? 0 : leftMillis;
	}
	
	public long secondsLeft() {
		long leftMillis = millisLeft();
		long roundedSecs = leftMillis / 1000L;
		boolean isEnt = leftMillis - roundedSecs*1000==0;
		return isEnt ? roundedSecs : roundedSecs + 1; // ceil
	}
}
