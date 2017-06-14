package atheria.fewizz.trade.packet;

import java.util.Objects;

import atheria.fewizz.trade.Trade;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageTradeRequest implements IMessage {
	public String playerName;
	
	public MessageTradeRequest() {
	}
	
	public MessageTradeRequest(String from) {
		this.playerName = from;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		playerName = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		Objects.requireNonNull(playerName);
		ByteBufUtils.writeUTF8String(buf, playerName);
	};
	
	public static class HandlerClient implements IMessageHandler<MessageTradeRequest, IMessage> {

		@Override
		public IMessage onMessage(MessageTradeRequest message, MessageContext ctx) {
			onMessageClient(message);
			
			return null;
		}
		
		@SideOnly(Side.CLIENT)
		public void onMessageClient(MessageTradeRequest message) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Trade request from player '" + message.playerName +"'"));
			});
		}
	}
	
	public static class HandlerServer implements IMessageHandler<MessageTradeRequest, IMessage> {

		@Override
		public IMessage onMessage(MessageTradeRequest message, MessageContext ctx) {
			FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
				Trade.onClientTradeRequest(ctx.getServerHandler().player, message.playerName);
			});
			
			return null;
		}
	}
}