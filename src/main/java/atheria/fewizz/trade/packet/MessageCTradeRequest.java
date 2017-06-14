package atheria.fewizz.trade.packet;

import java.util.Objects;

import atheria.fewizz.trade.Trade;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageCTradeRequest implements IMessage {
	public String playerToRequest;
	
	public MessageCTradeRequest() {
	}
	
	public MessageCTradeRequest(String playerName) {
		this.playerToRequest = playerName;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		playerToRequest = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		Objects.requireNonNull(playerToRequest);
		ByteBufUtils.writeUTF8String(buf, playerToRequest);
	};
	
	public static class Handler implements IMessageHandler<MessageCTradeRequest, IMessage> {

		@Override
		public IMessage onMessage(MessageCTradeRequest message, MessageContext ctx) {
			FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
				Trade.onClientTradeRequest(ctx.getServerHandler().player, message.playerToRequest);
			});
			
			return null;
		}
	}
}
