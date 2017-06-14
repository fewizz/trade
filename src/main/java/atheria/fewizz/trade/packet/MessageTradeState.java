package atheria.fewizz.trade.packet;

import atheria.fewizz.trade.Trade.TradeState;
import atheria.fewizz.trade.Trade.TradeState.State;
import atheria.fewizz.trade.inventory.ContainerTradeAbstract;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageTradeState implements IMessage {
	TradeState.State state;
	TradeState.State otherState = State.NOT_READY;

	public MessageTradeState() {
	}
	
	public MessageTradeState(TradeState.State tradeState) {
		this.state = tradeState;
	}
	
	public MessageTradeState(TradeState.State tradeState, TradeState.State otherState) {
		this.state = tradeState;
		this.otherState = otherState;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		state = TradeState.State.values()[buf.readByte()];
		otherState = TradeState.State.values()[buf.readByte()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(state.ordinal());
		buf.writeByte(otherState.ordinal());
	}
	
	// Sets other player's trade state
	public static class HandlerClient implements IMessageHandler<MessageTradeState, IMessage> {

		@Override
		public IMessage onMessage(MessageTradeState message, MessageContext ctx) {
			onClientMessage(message);
			
			return null;
		}
		
		@SideOnly(Side.CLIENT)
		public void onClientMessage(MessageTradeState message) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				((ContainerTradeAbstract)Minecraft.getMinecraft().player.openContainer).setTradeState(message.state);
				((ContainerTradeAbstract)Minecraft.getMinecraft().player.openContainer).setOtherPlayerTradeState(message.otherState);
			});
		}
		
	}
	
	public static class HandlerServer implements IMessageHandler<MessageTradeState, IMessage> {

		@Override
		public IMessage onMessage(MessageTradeState message, MessageContext ctx) {
			FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
				((ContainerTradeAbstract)ctx.getServerHandler().player.openContainer).setTradeState(message.state);
			});
			
			return null;
		}
		
	}

}
