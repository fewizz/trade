package atheria.fewizz.trade.packet;

import java.util.Objects;

import atheria.fewizz.trade.Trade;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.*;

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
				Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation("message.trade.requestIn", message.playerName, Trade.keyTrade.getDisplayName()));
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