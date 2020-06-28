package ru.fewizz.trade;

import java.util.function.Function;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.timer.Timer;
import ru.fewizz.trade.client.ClientTradeScreenHandler;

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
	
	private void setState0(TradeState s) {
		TradeState old = getState();
		if(old == s)
			return;
		super.setState(s);
		
		String eventName = "trade:trade\\"+player.getUuidAsString()+"\\"+other.player.getUuidAsString();
		
		if (s.isReady() && other.getState().isReady()) {
			/*player
				.server
				.getSaveProperties()
				.getMainWorldProperties()
				.getScheduledEvents()
				.setEvent(eventName, Trade.swap_time, (server, timer, time) -> {
					for (int i = 0; i < TradeInventory.SIZE; i++) {
						ItemStack stack = tradeInventory.getStack(i);
						tradeInventory.setStack(i, other.tradeInventory.getStack(i));
						other.tradeInventory.setStack(i, stack);
					}
					setState(TradeState.NOT_READY);
					other.setState(TradeState.NOT_READY);
				});*/
			countdown.enableForSeconds(Trade.swap_time);
			PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
			packet.writeInt(syncId);
			packet.writeInt(Trade.swap_time);
			ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Trade.TRADE_START, packet);
			ServerSidePacketRegistry.INSTANCE.sendToPlayer(other.player, Trade.TRADE_START, packet);
		} else if(old.isReady() && other.getState().isReady()) {
			
		}
	}
	
	@Override
	public void setState(TradeState s) {
		setState0(s);
		sendStateUpdate(Trader.MAIN);
		sendStateUpdate(Trader.OTHER);
	}
	
	public void setState(TradeState s, Trader tr) {
		setState0(s);
		sendStateUpdate(tr);
	}
	
	private void sendStateUpdate(Trader tr) {
		ServerTradeScreenHandler sh = tr == Trader.MAIN ? this : other;
		
		PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
		packet.writeInt(sh.syncId);
		packet.writeInt(tr.ordinal());
		packet.writeInt(getState().ordinal());
		ServerSidePacketRegistry.INSTANCE.sendToPlayer(sh.player, Trade.TRADE_STATE_S2C, packet);
	}
	
	@Override
	public void sendContentUpdates() {
		if (countdown.isEnabled() && countdown.secondsLeft() == 0) {
			for (int i = 0; i < TradeInventory.SIZE; i++) {
				ItemStack stack = tradeInventory.getStack(i);
				tradeInventory.setStack(i, other.tradeInventory.getStack(i));
				other.tradeInventory.setStack(i, stack);
			}
			setState(TradeState.NOT_READY);
			other.setState(TradeState.NOT_READY);
			countdown.disable();
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
