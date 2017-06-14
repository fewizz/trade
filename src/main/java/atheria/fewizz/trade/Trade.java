package atheria.fewizz.trade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import atheria.fewizz.trade.command.CommandTrade;
import atheria.fewizz.trade.command.CommandTradeAccept;
import atheria.fewizz.trade.inventory.ContainerTrade;
import atheria.fewizz.trade.inventory.InventoryTrade;
import atheria.fewizz.trade.packet.MessageCTradeRequest;
import atheria.fewizz.trade.packet.MessageSShowGuiContainer;
import atheria.fewizz.trade.packet.MessageSTradeRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
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

@Mod(modid = Trade.MODID, version = Trade.VERSION)
public class Trade {
	public static final Trade INSTANCE = new Trade();
	public static final String MODID = "trade";
	public static final String VERSION = "1.0.0";
	public static final SimpleNetworkWrapper NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

	// For server world, key = player, value = requested players
	static final Map<String, List<String>> REQUESTS = new HashMap<>();
	// For client world, contains player names
	@SideOnly(Side.CLIENT)
	static final List<String> CLIENT_REQUESTS = new ArrayList<>();
	@SideOnly(Side.CLIENT)
	public static KeyBinding keyTrade;

	@EventHandler
	public void onInit(FMLInitializationEvent event) {
		NETWORK_WRAPPER.registerMessage(MessageSTradeRequest.Handler.class, MessageSTradeRequest.class, 0, Side.CLIENT);
		NETWORK_WRAPPER.registerMessage(MessageSShowGuiContainer.Handler.class, MessageSShowGuiContainer.class, 1, Side.CLIENT);

		NETWORK_WRAPPER.registerMessage(MessageCTradeRequest.Handler.class, MessageCTradeRequest.class, 0, Side.SERVER);

		MinecraftForge.EVENT_BUS.register(INSTANCE);

		if (FMLCommonHandler.instance().getSide().isClient())
			onClientInit();
	}

	@SideOnly(Side.CLIENT)
	public void onClientInit() {
		keyTrade = new KeyBinding("Trade key", Keyboard.KEY_V, "key.categories.gameplay");
	}

	@EventHandler
	public void onServerInit(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandTrade());
		event.registerServerCommand(new CommandTradeAccept());
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent e) {
		Minecraft mc = Minecraft.getMinecraft();

		if (mc == null || mc.world == null || mc.pointedEntity == null || !(mc.pointedEntity instanceof EntityPlayer) || !keyTrade.isPressed()) {
			return;
		}

		EntityPlayer pointedPlayer = (EntityPlayer) mc.pointedEntity;
		NETWORK_WRAPPER.sendToServer(new MessageCTradeRequest(pointedPlayer.getName()));
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
		
		if(recieverRequests != null && recieverRequests.contains(requestSender.getName())) { // sender accepted reciever's request
			acceptTrade(requestSender, requestReciever);
		}
		else {
			List<String> senderRequests = REQUESTS.computeIfAbsent(requestSender.getName(), key -> new ArrayList<>());
			Trade.NETWORK_WRAPPER.sendTo(new MessageSTradeRequest(requestSender.getName()), requestReciever);

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

		InventoryTrade invR = new InventoryTrade();
		InventoryTrade invA = new InventoryTrade();

		ContainerTrade conR = new ContainerTrade(requestedPlayer, acceptedPlayerName, invR, invA);
		ContainerTrade conA = new ContainerTrade(acceptedPlayer, requestedPlayerName, invA, invR);

		showGuiContainerForPlayers(requestedPlayer, acceptedPlayer, conR);
		showGuiContainerForPlayers(acceptedPlayer, requestedPlayer, conA);
		
		REQUESTS.get(requestedPlayerName).remove(acceptedPlayerName);
	}

	// On server world
	private static void showGuiContainerForPlayers(EntityPlayerMP player, EntityPlayerMP otherPlayer, ContainerTrade container) {
		player.getNextWindowId();
		player.closeContainer();
		int windowId = player.currentWindowId;

		Trade.NETWORK_WRAPPER.sendTo(new MessageSShowGuiContainer(otherPlayer.getName(), windowId), player);

		player.openContainer = container;
		player.openContainer.windowId = windowId;
		player.openContainer.addListener(player);
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(player, player.openContainer));
	}

	// On server world
	public static boolean isPlayerRequestedByPlayer(String whomRequested, String whoRequested) {
		if (REQUESTS.computeIfAbsent(whoRequested, key -> new ArrayList<>()).contains(whomRequested))
			return true;

		return false;
	}

	public enum TradeState {
		READY, NOT_READY
	}
}
