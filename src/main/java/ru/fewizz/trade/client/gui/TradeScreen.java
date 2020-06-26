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
import ru.fewizz.trade.TradeState;
import ru.fewizz.trade.inventory.ClientTradeScreenHandler;

public class TradeScreen extends HandledScreen<ClientTradeScreenHandler> {
	static final Identifier BASE = new Identifier("trade:textures/gui/gui_trade_base.png");
	static final Identifier CLOTH = new Identifier("trade:textures/gui/gui_trade_cloth.png");
	static final Identifier SLOTS = new Identifier("trade:textures/gui/gui_trade_slots.png");
	static final int W = 344 / 2;
	static final int H = 335 / 2;

	public TradeScreen(ClientTradeScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundWidth = W;
		this.backgroundHeight = H;
	}
	
	public static final ScreenRegistry.Factory<ClientTradeScreenHandler, TradeScreen> FACTORY =
			(handler, inventory, title) -> {
		return new TradeScreen(handler, inventory, title);
	};

	@Override
	public void init() {
		super.init();
		
		addButton(new TradeButtonWidget(this, 0, x + W / 2 - 58, y + 73, button ->
			handler.setState(handler.getState().opposite())
		));
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
		
		boolean ready = handler.getState() == TradeState.READY;
		
		vb.vertex(x + 5, y + 5 + 78, z).texture(0, 1).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).next();
		vb.vertex(x + 5 + 69, y + 5 + 78, z).texture(1, 1).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).next();
		vb.vertex(x + 5 + 69, y + 5, z).texture(1, 0).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).next();
		vb.vertex(x + 5, y + 5, z).texture(0, 0).color(ready ? 0F : 1F, 1F, ready ? 0F : 1F, 1F).next();
		
		tess.draw();
		
		vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		
		ready = handler.other.getState() == TradeState.READY;
		
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
		
		String str = client.player.getEntityName();
		textRenderer.drawWithShadow(matrices, str, width / 2 - 47 - (textRenderer.getWidth(str) / 2), height / 2 - 75, 0xFFFFFFFF);
		
		str = client.
				getNetworkHandler().getPlayerListEntry(
					handler.other.
					playerUUID
				).getProfile()
				.getName();
		textRenderer.drawWithShadow(matrices, str, width / 2 + 47 - (textRenderer.getWidth(str) / 2), height / 2 - 75, 0xFFFFFFFF);
		
		Long st = handler.swapTimeMillis;
		
		if(st != null) {
			str = Integer.toString((int) Math.ceil(((double)(st - System.currentTimeMillis()) / 1000D)));
			textRenderer.drawWithShadow(matrices, str, width / 2 - (textRenderer.getWidth(str) / 2), height / 2 - 15, 0xFFFFFFFF);
		}
	}
	
	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
	}
	
}
