package ru.fewizz.trade;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

class TradeScreenHandlerFactory implements ExtendedScreenHandlerFactory {
	public ScreenHandlerFactory factory;
	public ServerPlayerEntity otherPlayer;

	public TradeScreenHandlerFactory(ScreenHandlerFactory factory, ServerPlayerEntity otherPlayer) {
		this.factory = factory;
		this.otherPlayer = otherPlayer;
	}
	
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return factory.createMenu(syncId, inv, player);
	}
	
	public Text getDisplayName() {
		return LiteralText.EMPTY;
	}
	
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		buf.writeUuid(otherPlayer.getUuid());
	}
	
}