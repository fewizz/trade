package atheria.fewizz.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import atheria.fewizz.trade.inventory.ContainerTrade;
import atheria.fewizz.trade.inventory.InventoryTrade;
import atheria.fewizz.trade.packet.MessageShowGuiContainer;
import atheria.fewizz.trade.packet.MessageTradeRequest;
import atheria.fewizz.trade.packet.MessageTradeState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(name = Trade.NAME, modid = Trade.MODID, version = Trade.VERSION)
public class Trade {
	public static final Trade INSTANCE = new Trade();
	public static final String NAME = "Trade";
	public static final String MODID = "trade";
	public static final String VERSION = "1.0.0";
	public static final SimpleNetworkWrapper NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

	// For server world, key = player, value = requested players
	static final Map<String, List<String>> REQUESTS = new HashMap<>();
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
		keyTrade = new KeyBinding("Trade key", Keyboard.KEY_V, "key.categories.gameplay");
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
			requestSender.sendMessage(new TextComponentString("Player " + requestRecieverName + " is not found."));
			return;
		}

		List<String> recieverRequests = REQUESTS.get(requestRecieverName);

		if (recieverRequests != null && recieverRequests.contains(requestSender.getName())) { // sender accepted reciever's request
			acceptTrade(requestSender, requestReciever);
		} else {
			List<String> senderRequests = REQUESTS.computeIfAbsent(requestSender.getName(), key -> new ArrayList<>());
			Trade.NETWORK_WRAPPER.sendTo(new MessageTradeRequest(requestSender.getName()), requestReciever);

			if (!senderRequests.contains(requestRecieverName)) {
				senderRequests.add(requestRecieverName);
			}

			requestSender.sendMessage(new TextComponentString("Request sent."));
		}
	}

	// On server world
	public static void acceptTrade(EntityPlayerMP acceptedPlayer, EntityPlayerMP requestedPlayer) {
		String requestedPlayerName = requestedPlayer.getName();
		String acceptedPlayerName = acceptedPlayer.getName();
		System.out.println(requestedPlayerName + " " + acceptedPlayerName);

		InventoryTrade invR = new InventoryTrade();
		InventoryTrade invA = new InventoryTrade();

		TradeState rts = new TradeState();
		TradeState ats = new TradeState();
		
		ContainerTrade conR = new ContainerTrade(requestedPlayer, acceptedPlayerName, rts, ats, invR, invA);
		ContainerTrade conA = new ContainerTrade(acceptedPlayer, requestedPlayerName, ats, rts, invA, invR);

		showGuiContainerForPlayers(requestedPlayer, acceptedPlayer, conR);
		showGuiContainerForPlayers(acceptedPlayer, requestedPlayer, conA);

		REQUESTS.get(requestedPlayerName).remove(acceptedPlayerName);
	}

	// On server world
	private static void showGuiContainerForPlayers(EntityPlayerMP player, EntityPlayerMP otherPlayer, ContainerTrade container) {
		player.getNextWindowId();
		player.closeContainer();
		int windowId = player.currentWindowId;

		Trade.NETWORK_WRAPPER.sendTo(new MessageShowGuiContainer(otherPlayer.getName(), windowId), player);

		player.openContainer = container;
		player.openContainer.windowId = windowId;
		player.openContainer.addListener(player);
		MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.openContainer));
	}

	public static class TradeState {
		public State state = State.NOT_READY;

		public enum State {
			READY, NOT_READY;

			public State opposite() {
				return this == READY ? State.NOT_READY : READY;
			}
		}
	}
}
