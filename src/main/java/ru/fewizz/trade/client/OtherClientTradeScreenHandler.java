package ru.fewizz.trade.client;

import java.util.UUID;
import java.util.function.Function;

import ru.fewizz.trade.TradeScreenHandler;
import ru.fewizz.trade.TradeState;

public class OtherClientTradeScreenHandler extends TradeScreenHandler<OtherClientTradeScreenHandler, ClientTradeScreenHandler> {
	public final UUID playerUUID;
	
	public OtherClientTradeScreenHandler(UUID uuid, Function<OtherClientTradeScreenHandler, ClientTradeScreenHandler> otherTSHFactory) {
		super(null, 0, otherTSHFactory);
		this.playerUUID = uuid;
	}
	
	@Override
	public void setState(TradeState ts) {
		super.setState(ts);
		if(ts.isNotReady())other.countdown.disable();
	}

}
