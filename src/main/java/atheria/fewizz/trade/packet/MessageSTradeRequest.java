package atheria.fewizz.trade.packet;

import java.util.Objects;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageSTradeRequest implements IMessage {
	public String playerThatRequested;
	
	public MessageSTradeRequest() {
	}
	
	public MessageSTradeRequest(String from) {
		this.playerThatRequested = from;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		playerThatRequested = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		Objects.requireNonNull(playerThatRequested);
		ByteBufUtils.writeUTF8String(buf, playerThatRequested);
	};
	
	public static class Handler implements IMessageHandler<MessageSTradeRequest, IMessage> {

		@Override
		public IMessage onMessage(MessageSTradeRequest message, MessageContext ctx) {
			onMessageClient(message);
			
			return null;
		}
		
		@SideOnly(Side.CLIENT)
		public void onMessageClient(MessageSTradeRequest message) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Trade request from player '" + message.playerThatRequested +"'"));
			});
		}
	}
}