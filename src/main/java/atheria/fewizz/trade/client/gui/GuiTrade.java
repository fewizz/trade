package atheria.fewizz.trade.client.gui;

import org.lwjgl.opengl.GL11;

import atheria.fewizz.trade.Trade;
import atheria.fewizz.trade.Trade.TradeState;
import atheria.fewizz.trade.inventory.ContainerTradeAbstract;
import atheria.fewizz.trade.packet.MessageTradeState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class GuiTrade extends GuiContainer {
	static final ResourceLocation RL_TRADE_GUI_BASE = new ResourceLocation("trade:textures/gui/gui_trade_base.png");
	static final ResourceLocation RL_TRADE_GUI_CLOTH = new ResourceLocation("trade:textures/gui/gui_trade_cloth.png");
	static final ResourceLocation RL_TRADE_GUI_SLOTS = new ResourceLocation("trade:textures/gui/gui_trade_slots.png");
	static final int W = 344 / 2;
	static final int H = 335 / 2;
	final ContainerTradeAbstract containerTrade;

	public GuiTrade(ContainerTradeAbstract inventorySlotsIn) {
		super(inventorySlotsIn);
		this.containerTrade = inventorySlotsIn;
	}

	@Override
	public void initGui() {
		super.initGui();
		
		this.xSize = W;
		this.ySize = H;
		this.guiLeft = width / 2 - W / 2;
		this.guiTop = height / 2 - H / 2;
		
		addButton(new GuiButtonTrade(this, 0, W / 2 - 58, 73) {
			@Override
			public void mouseReleased(int mouseX, int mouseY) {
				containerTrade.setTradeState(containerTrade.getTradeState().opposite());
				Trade.NETWORK_WRAPPER.sendToServer(new MessageTradeState(containerTrade.getTradeState()));
			}
		});
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		float x = guiLeft;
		float y = guiTop + 0.5F;
		
		mc.getTextureManager().bindTexture(RL_TRADE_GUI_BASE);
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		
		vb.pos(x, y + H, 0).tex(0, 1).endVertex();
		vb.pos(x + W, y + H, 0).tex(1, 1).endVertex();
		vb.pos(x + W, y, 0).tex(1, 0).endVertex();
		vb.pos(x, y, 0).tex(0, 0).endVertex();
		
		tess.draw();
		
		
		mc.getTextureManager().bindTexture(RL_TRADE_GUI_CLOTH);
		vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		
		boolean ready = containerTrade.getTradeState() == TradeState.READY;
		
		vb.pos(x + 5, y + 5 + 78, 0).tex(0, 1).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).endVertex();
		vb.pos(x + 5 + 69, y + 5 + 78, 0).tex(1, 1).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).endVertex();
		vb.pos(x + 5 + 69, y + 5, 0).tex(1, 0).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).endVertex();
		vb.pos(x + 5, y + 5, 0).tex(0, 0).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).endVertex();
		
		tess.draw();
		
		vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		
		ready = containerTrade.otherContainer.getTradeState() == TradeState.READY;
		
		vb.pos(x + 98.5F, y + 5 + 78, 0).tex(0, 1).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).endVertex();
		vb.pos(x + 98.5F + 69, y + 5 + 78, 0).tex(1, 1).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).endVertex();
		vb.pos(x + 98.5F + 69, y + 5, 0).tex(1, 0).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).endVertex();
		vb.pos(x + 98.5F, y + 5, 0).tex(0, 0).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).endVertex();
		
		tess.draw();
		
		
		mc.getTextureManager().bindTexture(RL_TRADE_GUI_SLOTS);
		vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		
		y += 18F;
		
		vb.pos(x + 13.5F, y + 53, 0).tex(0, 1).endVertex();
		vb.pos(x + 13.5F + 53, y + 53, 0).tex(1, 1).endVertex();
		vb.pos(x + 13.5F + 53, y, 0).tex(1, 0).endVertex();
		vb.pos(x + 13.5F, y, 0).tex(0, 0).endVertex();
		
		tess.draw();
		
		vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		
		vb.pos(x + 106.5F, y + 53, 0).tex(0, 1).endVertex();
		vb.pos(x + 106.5F + 53, y + 53, 0).tex(1, 1).endVertex();
		vb.pos(x + 106.5F + 53, y, 0).tex(1, 0).endVertex();
		vb.pos(x + 106.5F, y, 0).tex(0, 0).endVertex();
		
		tess.draw();
		
		String str = containerTrade.playerName;
		fontRenderer.drawStringWithShadow(str, width / 2 - 47 - (fontRenderer.getStringWidth(str) / 2), height / 2 - 75, 0xFFFFFFFF);
		
		str = containerTrade.otherPlayerName;
		fontRenderer.drawStringWithShadow(str, width / 2 + 47 - (fontRenderer.getStringWidth(str) / 2), height / 2 - 75, 0xFFFFFFFF);
		
		Long st = containerTrade.swapTimeMillis;
		
		if(st != null) {
			str = Integer.toString((int) Math.ceil(((double)(st - System.currentTimeMillis()) / 1000D)));
			fontRenderer.drawStringWithShadow(str, width / 2 - (fontRenderer.getStringWidth(str) / 2), height / 2 - 15, 0xFFFFFFFF);
		}
	}
	
}
