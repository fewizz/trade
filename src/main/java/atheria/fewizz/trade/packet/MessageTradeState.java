package atheria.fewizz.trade.packet;

import atheria.fewizz.trade.Trade.TradeState;
import atheria.fewizz.trade.inventory.ContainerTradeAbstract;
import atheria.fewizz.trade.inventory.ContainerTradeClient;
import atheria.fewizz.trade.inventory.ContainerTradeServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageTradeState implements IMessage {
	TradeState state;
	TradeState otherState = TradeState.NOT_READY;

	public MessageTradeState() {
	}

	public MessageTradeState(TradeState tradeState) {
		this.state = tradeState;
	}

	public MessageTradeState(TradeState tradeState, TradeState otherState) {
		this.state = tradeState;
		this.otherState = otherState;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		state = TradeState.values()[buf.readByte()];
		otherState = TradeState.values()[buf.readByte()];
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
				Container c = Minecraft.getMinecraft().player.openContainer;
				if (c == null || !(c instanceof ContainerTradeClient)) {
					return;
				}

				ContainerTradeClient ct = (ContainerTradeClient) c;
				ct.otherContainer.setTradeState(message.otherState);
				ct.setTradeState(message.state);
			});
		}

	}

	public static class HandlerServer implements IMessageHandler<MessageTradeState, IMessage> {

		@Override
		public IMessage onMessage(MessageTradeState message, MessageContext ctx) {
			FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
				Container c = (ContainerTradeAbstract) ctx.getServerHandler().player.openContainer;
				if (c == null || !(c instanceof ContainerTradeServer)) {
					return;
				}

				((ContainerTradeServer) c).setTradeState(message.state);
			});

			return null;
		}

	}

}
