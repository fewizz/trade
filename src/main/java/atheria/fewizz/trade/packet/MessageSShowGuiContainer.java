package atheria.fewizz.trade.packet;

import java.util.Objects;

import atheria.fewizz.trade.Trade;
import atheria.fewizz.trade.client.gui.GuiTrade;
import atheria.fewizz.trade.inventory.ContainerTrade;
import atheria.fewizz.trade.inventory.InventoryTrade;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageSShowGuiContainer implements IMessage {
	public String otherPlayerName;
	public int windowID;

	public MessageSShowGuiContainer() {
	}
	
	public MessageSShowGuiContainer(String playerName, int windowID) {
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
	
	public static class Handler implements IMessageHandler<MessageSShowGuiContainer, IMessage> {

		@Override
		public IMessage onMessage(MessageSShowGuiContainer message, MessageContext ctx) {
			onMessageClient(message);
			
			return null;
		}
		
		@SideOnly(Side.CLIENT)
		public void onMessageClient(MessageSShowGuiContainer message) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				ContainerTrade container = new ContainerTrade(Minecraft.getMinecraft().player, message.otherPlayerName, new InventoryTrade(), new InventoryTrade());
				FMLCommonHandler.instance().showGuiScreen(new GuiTrade(container));
				Minecraft.getMinecraft().player.openContainer.windowId = message.windowID;
			});
		}
	}

}
