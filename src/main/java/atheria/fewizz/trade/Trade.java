package atheria.fewizz.trade;

import java.util.*;

import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Keyboard;

import atheria.fewizz.trade.inventory.*;
import atheria.fewizz.trade.packet.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.*;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.*;

@Mod(name = Trade.NAME, modid = Trade.MODID, version = Trade.VERSION, acceptedMinecraftVersions = "[1.12.0,)")
public class Trade {
	public static final Trade INSTANCE = new Trade();
	public static final String NAME = "Trade";
	public static final String MODID = "trade";
	public static final String VERSION = "1.0.4";
	public static final SimpleNetworkWrapper NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

	// For server world, key = player, value = requested players
	static final Map<String, Set<String>> REQUESTS = new HashMap<>();
	@SideOnly(Side.CLIENT)
	public static KeyBinding keyTrade;

	@EventHandler
	public void onInit(FMLInitializationEvent event) {
		NETWORK_WRAPPER.registerMessage(MessageTradeRequest.HandlerClient.class, MessageTradeRequest.class, 0, Side.CLIENT);
		NETWORK_WRAPPER.registerMessage(MessageShowGuiContainer.HandlerClient.class, MessageShowGuiContainer.class, 1, Side.CLIENT);
		NETWORK_WRAPPER.registerMessage(MessageTradeState.HandlerClient.class, MessageTradeState.class, 2, Side.CLIENT);

		NETWORK_WRAPPER.registerMessage(MessageTradeRequest.HandlerServer.class, MessageTradeRequest.class, 3, Side.SERVER);
		NETWORK_WRAPPER.registerMessage(MessageTradeState.HandlerServer.class, MessageTradeState.class, 4, Side.SERVER);

		MinecraftForge.EVENT_BUS.register(INSTANCE);

		if (FMLCommonHandler.instance().getSide().isClient())
			onClientInit();
	}

	@SideOnly(Side.CLIENT)
	public void onClientInit() {
		keyTrade = new KeyBinding("Trade", Keyboard.KEY_H, "key.categories.multiplayer");
		ClientRegistry.registerKeyBinding(keyTrade);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent e) {
		Minecraft mc = Minecraft.getMinecraft();

		if (mc == null || mc.world == null || mc.pointedEntity == null || !(mc.pointedEntity instanceof EntityPlayer) || !keyTrade.isPressed()) {
			return;
		}

		EntityPlayer pointedPlayer = (EntityPlayer) mc.pointedEntity;
		NETWORK_WRAPPER.sendToServer(new MessageTradeRequest(pointedPlayer.getName()));
	}

	// On server world
	public static void onClientTradeRequest(EntityPlayerMP requestSender, String requestRecieverName) {
		PlayerList list = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
		EntityPlayerMP requestReciever = list.getPlayerByUsername(requestRecieverName);

		if (requestReciever == null) {
			requestSender.sendMessage(new TextComponentTranslation("message.trade.playerNotFound", requestRecieverName));
			return;
		}

		Set<String> recieverRequests = REQUESTS.get(requestRecieverName);

		if (recieverRequests != null && recieverRequests.contains(requestSender.getName())) { // sender accepted reciever's request
			acceptTrade(requestSender, requestReciever);
		} else {
			Set<String> senderRequests = REQUESTS.computeIfAbsent(requestSender.getName(), key -> new HashSet<>());
			Trade.NETWORK_WRAPPER.sendTo(new MessageTradeRequest(requestSender.getName()), requestReciever);

			senderRequests.add(requestRecieverName);

			requestSender.sendMessage(new TextComponentTranslation("message.trade.requestOut", requestRecieverName));
		}
	}

	// On server world
	public static void acceptTrade(EntityPlayerMP acceptedPlayer, EntityPlayerMP requestedPlayer) {
		String requestedPlayerName = requestedPlayer.getName();
		String acceptedPlayerName = acceptedPlayer.getName();
		//System.out.println(requestedPlayerName + " " + acceptedPlayerName);

		ContainerTradeServer conR = new ContainerTradeServer(requestedPlayer, acceptedPlayerName);
		ContainerTradeServer conA = new ContainerTradeServer(acceptedPlayer, requestedPlayerName);

		showGuiContainerForPlayers(requestedPlayer, acceptedPlayer, conR);
		showGuiContainerForPlayers(acceptedPlayer, requestedPlayer, conA);

		REQUESTS.get(requestedPlayerName).remove(acceptedPlayerName);
	}

	// On server world
	private static void showGuiContainerForPlayers(EntityPlayerMP player, EntityPlayerMP otherPlayer, ContainerTradeAbstract container) {
		player.getNextWindowId();
		player.closeContainer();
		int windowId = player.currentWindowId;

		Trade.NETWORK_WRAPPER.sendTo(new MessageShowGuiContainer(otherPlayer.getName(), windowId), player);

		player.openContainer = container;
		player.openContainer.windowId = windowId;
		player.openContainer.addListener(player);
		MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.openContainer));
	}

	public enum TradeState {
		READY, NOT_READY;

		public TradeState opposite() {
			return this == READY ? TradeState.NOT_READY : READY;
		}
	}

}
