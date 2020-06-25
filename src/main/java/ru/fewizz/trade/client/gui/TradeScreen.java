package ru.fewizz.trade.client.gui;

import org.lwjgl.opengl.GL11;

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.fewizz.trade.Trade.TradeState;
import ru.fewizz.trade.inventory.TradeScreenHandler;

public class TradeScreen extends HandledScreen<TradeScreenHandler> {
	static final Identifier BASE = new Identifier("trade:textures/gui/gui_trade_base.png");
	static final Identifier CLOTH = new Identifier("trade:textures/gui/gui_trade_cloth.png");
	static final Identifier SLOTS = new Identifier("trade:textures/gui/gui_trade_slots.png");
	static final int W = 344 / 2;
	static final int H = 335 / 2;

	public TradeScreen(TradeScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundWidth = W;
		this.backgroundHeight = H;
	}
	
	public static final ScreenRegistry.Factory<TradeScreenHandler, TradeScreen> FACTORY =
			(handler, inventory, title) -> {
		return new TradeScreen(handler, inventory, title);
	};

	@Override
	public void init() {
		super.init();
		
		addButton(new TradeButtonWidget(this, 0, x + W / 2 - 58, y + 73) {
			@Override
			public boolean mouseReleased(double mouseX, double mouseY, int button) {
				handler.setTradeState(handler.getTradeState().opposite());
				//TODO
				return false;
				//Trade.NETWORK_WRAPPER.sendToServer(new MessageTradeState(containerTrade.getTradeState()));
			}
		});
	}
	
	@Override
	protected void drawBackground(MatrixStack matrices, float partialTicks, int mouseX, int mouseY) {
		float x = this.x;
		float y = this.y + 0.5F;
		float z = getZOffset();
		client.getTextureManager().bindTexture(BASE);
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
		
		vb.vertex(x, y + H, z).texture(0, 1).next();
		vb.vertex(x + W, y + H, z).texture(1, 1).next();
		vb.vertex(x + W, y, z).texture(1, 0).next();
		vb.vertex(x, y, z).texture(0, 0).next();
		
		tess.draw();
		
		
		client.getTextureManager().bindTexture(CLOTH);
		vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		
		boolean ready = handler.getTradeState() == TradeState.READY;
		
		vb.vertex(x + 5, y + 5 + 78, z).texture(0, 1).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).next();
		vb.vertex(x + 5 + 69, y + 5 + 78, z).texture(1, 1).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).next();
		vb.vertex(x + 5 + 69, y + 5, z).texture(1, 0).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).next();
		vb.vertex(x + 5, y + 5, z).texture(0, 0).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).next();
		
		tess.draw();
		
		vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		
		ready = false;//TODO handler.otherContainer.getTradeState() == TradeState.READY;
		
		vb.vertex(x + 98.5F, y + 5 + 78, z).texture(0, 1).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).next();
		vb.vertex(x + 98.5F + 69, y + 5 + 78, z).texture(1, 1).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).next();
		vb.vertex(x + 98.5F + 69, y + 5, z).texture(1, 0).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).next();
		vb.vertex(x + 98.5F, y + 5, z).texture(0, 0).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).next();
		
		tess.draw();
		
		
		client.getTextureManager().bindTexture(SLOTS);
		vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
		
		y += 18F;
		
		vb.vertex(x + 13.5F, y + 53, z).texture(0, 1).next();
		vb.vertex(x + 13.5F + 53, y + 53, z).texture(1, 1).next();
		vb.vertex(x + 13.5F + 53, y, z).texture(1, 0).next();
		vb.vertex(x + 13.5F, y, z).texture(0, 0).next();
		
		tess.draw();
		
		vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
		
		vb.vertex(x + 106.5F, y + 53, z).texture(0, 1).next();
		vb.vertex(x + 106.5F + 53, y + 53, z).texture(1, 1).next();
		vb.vertex(x + 106.5F + 53, y, z).texture(1, 0).next();
		vb.vertex(x + 106.5F, y, z).texture(0, 0).next();
		
		tess.draw();
		
		String str = handler.playerName;
		textRenderer.drawWithShadow(matrices, str, width / 2 - 47 - (textRenderer.getWidth(str) / 2), height / 2 - 75, 0xFFFFFFFF);
		
		str = handler.otherPlayerName;
		textRenderer.drawWithShadow(matrices, str, width / 2 + 47 - (textRenderer.getWidth(str) / 2), height / 2 - 75, 0xFFFFFFFF);
		
		Long st = handler.swapTimeMillis;
		
		if(st != null) {
			str = Integer.toString((int) Math.ceil(((double)(st - System.currentTimeMillis()) / 1000D)));
			textRenderer.drawWithShadow(matrices, str, width / 2 - (textRenderer.getWidth(str) / 2), height / 2 - 15, 0xFFFFFFFF);
		}
	}
	
}
