package atheria.fewizz.trade.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

import atheria.fewizz.trade.Trade;
import atheria.fewizz.trade.Trade.TradeState;
import atheria.fewizz.trade.inventory.ContainerTradeAbstract;
import atheria.fewizz.trade.packet.MessageTradeState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiTrade extends GuiContainer {
	private static final ResourceLocation RL_TRADE_GUI = new ResourceLocation("trade:textures/gui/trade.png");

	final ContainerTradeAbstract containerTrade;

	private GuiButton btnTradeOwn;
	private GuiButton btnTradeOther;

	public GuiTrade(ContainerTradeAbstract inventorySlotsIn) {
		super(inventorySlotsIn);
		this.containerTrade = inventorySlotsIn;

		this.xSize = 176;
		this.ySize = 188;
	}

	@Override
	public void initGui() {
		super.initGui();

		btnTradeOwn = new GuiButton(1, guiLeft + 16, guiTop + 71, 54, 20, "gui.trade.waiting");
		btnTradeOther = new GuiButton(2, guiLeft + 106, guiTop + 71, 54, 20, "gui.trade.waiting");
		btnTradeOther.enabled = false;

		addButton(btnTradeOwn);
		addButton(btnTradeOther);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 1) {
			containerTrade.setTradeState(containerTrade.getTradeState().opposite());
			Trade.NETWORK_WRAPPER.sendToServer(new MessageTradeState(containerTrade.getTradeState()));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = containerTrade.playerName;
		fontRenderer.drawString(name, 43 - fontRenderer.getStringWidth(name) / 2, 6, 4210752);

		name = containerTrade.otherPlayerName;
		fontRenderer.drawString(name, 133 - fontRenderer.getStringWidth(name) / 2, 6, 4210752);

		fontRenderer.drawString(containerTrade.inventoryPlayer.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);

		Long swapTime = containerTrade.swapTimeMillis;
		if(swapTime != null) {
			String str = Integer.toString((int) Math.ceil((double) (swapTime - System.currentTimeMillis()) / 1000D));
			fontRenderer.drawString(str, 88 - (fontRenderer.getStringWidth(str) / 2), 77, 0x007f00);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		btnTradeOwn.displayString = I18n.format(containerTrade.getTradeState() == TradeState.READY ? "gui.trade.ready" : "gui.trade.notready");
		btnTradeOther.displayString = I18n.format(containerTrade.otherContainer.getTradeState() == TradeState.READY ? "gui.trade.ready" : "gui.trade.notready");

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(RL_TRADE_GUI);

		float x = guiLeft;
		float y = guiTop;

		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}
	
}
