package ru.fewizz.trade.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import ru.fewizz.trade.Trade.TradeState;

public class TradeButtonWidget extends ButtonWidget {
	static final Identifier TRADE_BUTTON_TEXTURE = new Identifier("trade:textures/gui/button_trade.png");
	public static final int W = 51 / 2;
	public static final int H = 51 / 2 / 3;
	final TradeScreen screen;
	
	public TradeButtonWidget(TradeScreen screen, int id, int x, int y) {
		super(x, y, W, H, null, null);
		this.screen = screen;
	}
	
	
	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MinecraftClient.getInstance().getTextureManager().bindTexture(TRADE_BUTTON_TEXTURE);
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
		
		float tyOffset = screen.getScreenHandler().getTradeState() != TradeState.READY ? 1F / 3F : 2F / 3F;
		
		vb.vertex(x, y + height, getZOffset()).texture(0, 1 / 3F + tyOffset).next();
		vb.vertex(x + width, y + height, getZOffset()).texture(1, 1 / 3F + tyOffset).next();
		vb.vertex(x + width, y, getZOffset()).texture(1, tyOffset).next();
		vb.vertex(x, y, getZOffset()).texture(0, tyOffset).next();
		//RenderSystem.enableAlphaTest();
		tess.draw();
	}

}
