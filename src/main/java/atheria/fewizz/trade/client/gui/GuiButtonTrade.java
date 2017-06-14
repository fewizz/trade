package atheria.fewizz.trade.client.gui;

import org.lwjgl.opengl.GL11;

import atheria.fewizz.trade.Trade.TradeState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class GuiButtonTrade extends GuiButton {
	static final ResourceLocation RL_BUTTON_TRADE = new ResourceLocation("trade:textures/gui/button_trade.png");
	public static final int TEX_SIZE = 51;
	public static final int W = TEX_SIZE / 2;
	public static final int H = TEX_SIZE / 2 / 3;
	final GuiTrade guiTrade;
	
	public GuiButtonTrade(GuiTrade gui, int id, int x, int y) {
		super(id, x + gui.getGuiLeft(), y + gui.getGuiTop(), W, H, "");
		this.guiTrade = gui;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(RL_BUTTON_TRADE);
		
		Tessellator tess = Tessellator.getInstance();
		VertexBuffer vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		
		float tyOffset = guiTrade.containerTrade.getTradeState().state == TradeState.State.READY ? 1F / 3F : 2F / 3F;
		
		vb.pos(x, y + height, 0).tex(0, 1 / 3F + tyOffset).endVertex();
		vb.pos(x + width, y + height, 0).tex(1, 1 / 3F + tyOffset).endVertex();
		vb.pos(x + width, y, 0).tex(1, tyOffset).endVertex();
		vb.pos(x, y, 0).tex(0, tyOffset).endVertex();
		
		tess.draw();
	}

}
