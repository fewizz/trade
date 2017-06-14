package atheria.fewizz.trade.client.gui;

import org.lwjgl.opengl.GL11;

import atheria.fewizz.trade.Trade;
import atheria.fewizz.trade.inventory.ContainerTradeAbstract;
import atheria.fewizz.trade.packet.MessageTradeState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiTrade extends GuiContainer {
	static final ResourceLocation RL_TRADE_GUI = new ResourceLocation("trade:textures/gui/gui_trade.png");
	static final int TEXTURE_W = 308;
	static final int TEXTURE_H = 356;
	static final int W = TEXTURE_W / 2;
	static final int H = TEXTURE_H / 2;
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
		
		addButton(new GuiButtonTrade(this, 0, W / 2 - GuiButtonTrade.W / 2, 84) {
			@Override
			public void mouseReleased(int mouseX, int mouseY) {
				containerTrade.setTradeState(containerTrade.getTradeState().state.opposite());
				Trade.NETWORK_WRAPPER.sendToServer(new MessageTradeState(containerTrade.getTradeState().state));
			}
		});
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(RL_TRADE_GUI);
		
		Tessellator tess = Tessellator.getInstance();
		VertexBuffer vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		
		float x = guiLeft;
		float y = guiTop + 0.5F;
		
		vb.pos(x, y + H, 0).tex(0, 1).endVertex();
		vb.pos(x + W, y + H, 0).tex(1, 1).endVertex();
		vb.pos(x + W, y, 0).tex(1, 0).endVertex();
		vb.pos(x, y, 0).tex(0, 0).endVertex();
		
		tess.draw();
	}
	
}
