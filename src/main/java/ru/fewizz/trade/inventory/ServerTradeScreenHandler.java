package ru.fewizz.trade.inventory;

import java.util.function.Function;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import ru.fewizz.trade.Trade;
import ru.fewizz.trade.TradeState;

public class ServerTradeScreenHandler
extends TradeScreenHandlerWithPlayer<
	ServerTradeScreenHandler,
	ServerTradeScreenHandler,
	ServerPlayerEntity
> {
	
	public static void openFor(ServerPlayerEntity player0, ServerPlayerEntity player1) {
		class TradeScreenHandlerFactory implements ExtendedScreenHandlerFactory {
			ScreenHandlerFactory factory;
			ServerPlayerEntity otherPlayer;
			
			public TradeScreenHandlerFactory(ScreenHandlerFactory factory, ServerPlayerEntity otherPlayer) {
				this.factory = factory;
				this.otherPlayer = otherPlayer;
			}
			
			@Override
			public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
				return factory.createMenu(syncId, inv, player);
			}
			
			public Text getDisplayName() {
				return new LiteralText("");
			}
			
			public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
				buf.writeUuid(otherPlayer.getUuid());
			}
			
		}
		
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
	
	private ServerTradeScreenHandler(int syncID, ServerPlayerEntity player, Function<ServerTradeScreenHandler, ServerTradeScreenHandler> other) {
		super(ClientTradeScreenHandler.TYPE, syncID, player, other);
	}
	
	@Override
	public void setState(TradeState s) {
		setState(s, true, true);
	}
	
	public void setState(TradeState s, boolean sendPlayer, boolean sendOtherPlayer) {
		boolean sendChanges = sendPlayer || sendOtherPlayer;
		if(sendChanges) {
			PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
			packet.writeInt(syncId);
			packet.writeInt(s.ordinal());
		
			if(sendPlayer)
				ServerSidePacketRegistry.INSTANCE.sendToPlayer(
					player,
					Trade.TRADE_STATE,
					packet
				);
			if(sendOtherPlayer)
				ServerSidePacketRegistry.INSTANCE.sendToPlayer(
					other.player,
					Trade.OTHER_TRADE_STATE,
					packet
				);
		}
		
		super.setState(s);
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
