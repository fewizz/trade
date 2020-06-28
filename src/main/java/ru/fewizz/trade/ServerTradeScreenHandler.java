package ru.fewizz.trade;

import java.util.function.Function;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerTradeScreenHandler
extends TradeScreenHandlerWithPlayer<
	ServerTradeScreenHandler,
	ServerTradeScreenHandler,
	ServerPlayerEntity
> {
	
	public static void openFor(ServerPlayerEntity player0, ServerPlayerEntity player1) {
		// https://www.meme-arsenal.com/memes/a8bc14a4a57639a2807023fc6418edd3.jpg
		player0.openHandledScreen(new TradeScreenHandlerFactory(
			(syncId0, inv0, p0) -> {
				return new ServerTradeScreenHandler(syncId0, (ServerPlayerEntity)p0, tsc0 -> {
					player1.openHandledScreen(new TradeScreenHandlerFactory(
						(syncId1, inv1, p1) -> new ServerTradeScreenHandler(syncId1, (ServerPlayerEntity)p1, tsc1 -> tsc0),
						player0
					));
					return (ServerTradeScreenHandler) player1.currentScreenHandler;
				});
			},
			player1
		));
	}
	
	private ServerTradeScreenHandler(
			int syncID,
			ServerPlayerEntity player,
			Function<ServerTradeScreenHandler, ServerTradeScreenHandler> otherTSHFactory
		) {
		super(ClientTradeScreenHandler.TYPE, syncID, player, otherTSHFactory);
	}
	
	@Override
	public void setState(TradeState s) {
		sendStateUpdate(Trader.MAIN);
		sendStateUpdate(Trader.OTHER);
		super.setState(s);
	}
	
	public void setState(TradeState s, Trader tr) {
		sendStateUpdate(tr);
		super.setState(s);
	}
	
	private void sendStateUpdate(Trader tr) {
		ServerTradeScreenHandler sh = tr == Trader.MAIN ? this : other;
		
		PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
		packet.writeInt(sh.syncId);
		packet.writeInt(getState().ordinal());
		ServerSidePacketRegistry.INSTANCE.sendToPlayer(sh.player, Trade.TRADE_STATE, packet);
	}
	
	@Override
	public void sendContentUpdates() {
		if (swapTimeMillis != null && System.currentTimeMillis() >= swapTimeMillis) {
			for (int i = 0; i < TradeInventory.SIZE; i++) {
				ItemStack stack = tradeInventory.getStack(i);
				tradeInventory.setStack(i, other.tradeInventory.getStack(i));
				other.tradeInventory.setStack(i, stack);
			}
			setState(TradeState.NOT_READY);
			other.setState(TradeState.NOT_READY);
			swapTimeMillis = null;
			other.swapTimeMillis = null;
		}
		super.sendContentUpdates();
	}
	
	boolean closing = false;
	@Override
	public void close(PlayerEntity player) {
		closing = true;
		
		if(!other.closing)
			other.player.closeHandledScreen();
	}
}
