package ru.fewizz.trade.inventory;

import java.util.UUID;
import java.util.function.Function;

public class OtherClientTradeScreenHandler extends AbstractTradeScreenHandler<OtherClientTradeScreenHandler, ClientTradeScreenHandler> {
	public final UUID playerUUID;
	
	public OtherClientTradeScreenHandler(UUID uuid, Function<OtherClientTradeScreenHandler, ClientTradeScreenHandler> other) {
		super(null, 0, other);
		this.playerUUID = uuid;
	}

}
