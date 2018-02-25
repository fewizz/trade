package atheria.fewizz.trade.packet;

import java.util.Objects;

import atheria.fewizz.trade.client.gui.GuiTrade;
import atheria.fewizz.trade.inventory.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.*;

public class MessageShowGuiContainer implements IMessage {
	public String otherPlayerName;
	public int windowID;

	public MessageShowGuiContainer() {
	}
	
	public MessageShowGuiContainer(String playerName, int windowID) {
		this.otherPlayerName = playerName;
		this.windowID = windowID;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.otherPlayerName = ByteBufUtils.readUTF8String(buf);
		this.windowID = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		Objects.requireNonNull(otherPlayerName);
		ByteBufUtils.writeUTF8String(buf, otherPlayerName);
		buf.writeInt(windowID);
	};
	
	public static class HandlerClient implements IMessageHandler<MessageShowGuiContainer, IMessage> {

		@Override
		public IMessage onMessage(MessageShowGuiContainer message, MessageContext ctx) {
			onMessageClient(message);
			
			return null;
		}
		
		@SideOnly(Side.CLIENT)
		public void onMessageClient(MessageShowGuiContainer message) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				ContainerTradeAbstract container = new ContainerTradeClient(message.otherPlayerName);
				FMLCommonHandler.instance().showGuiScreen(new GuiTrade(container));
				Minecraft.getMinecraft().player.openContainer.windowId = message.windowID;
			});
		}
	}

}
