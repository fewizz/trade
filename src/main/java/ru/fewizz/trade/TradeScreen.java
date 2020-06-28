package ru.fewizz.trade;

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TradeScreen extends HandledScreen<ClientTradeScreenHandler> {
	static final Identifier
		BASE = new Identifier("trade:textures/base.png"),
		CLOTH = new Identifier("trade:textures/cloth.png"),
		SLOTS = new Identifier("trade:textures/slots.png");
	static final int
		BASE_W = 176,
		BASE_H = 90,
		CLOTH_W = 176,
		CLOTH_H = 156,
		CLOTH_X = (BASE_W - CLOTH_W) / 2,
		CLOTH_Y = 0,
		BASE_X = 0,
		SLOTS_SIZE = 108 / 2,
		SLOTS_X = 14,
		SLOTS_Y = 19,
		BUTTON_Y = SLOTS_Y + SLOTS_SIZE + 2,
		BASE_Y = BUTTON_Y + TradeButtonWidget.H + 2,
		W = BASE_X + BASE_W,
		H = BASE_Y + BASE_H;

	public TradeScreen(ClientTradeScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundWidth = BASE_W;
		this.backgroundHeight = BASE_Y + BASE_H - CLOTH_Y;
	}
	
	public static final ScreenRegistry.Factory<ClientTradeScreenHandler, TradeScreen> FACTORY =
			(handler, inventory, title) -> {
		return new TradeScreen(handler, inventory, title);
	};

	@Override
	public void init() {
		super.init();
		
		addButton(new TradeButtonWidget(handler, x + SLOTS_X + SLOTS_SIZE/2 - TradeButtonWidget.W/2, y + BUTTON_Y, button ->
			handler.setState(handler.getState().opposite())
		));
		ButtonWidget other = new TradeButtonWidget(
				handler.other,
				x + W - SLOTS_X - SLOTS_SIZE/2 - TradeButtonWidget.W/2,
				y + BUTTON_Y,
				button -> {}
		);
		other.active = false;
		addButton(other);
	}
	
	@Override
	protected void drawBackground(MatrixStack matrices, float partialTicks, int mouseX, int mouseY) {
		int
			left = this.x,
			top = this.y,
			right = this.x + this.backgroundWidth;
		
		client.getTextureManager().bindTexture(CLOTH);
		drawTexture(matrices, left + CLOTH_X, top + CLOTH_Y, CLOTH_W, CLOTH_H);
		
		client.getTextureManager().bindTexture(BASE);
		drawTexture(matrices, left + BASE_X, top + BASE_Y, BASE_W, BASE_H);
		
		client.getTextureManager().bindTexture(SLOTS);
		drawTexture(
				matrices,
				left + SLOTS_X,
				top + SLOTS_Y,
				SLOTS_SIZE,
				SLOTS_SIZE
		);
		drawTexture(
				matrices,
				right - SLOTS_X - SLOTS_SIZE,
				top + SLOTS_Y,
				SLOTS_SIZE,
				SLOTS_SIZE
		);
		
		String str = client.player.getEntityName();
		textRenderer.drawWithShadow(
				matrices,
				str,
				x + SLOTS_X + SLOTS_SIZE/2 - (textRenderer.getWidth(str) / 2),
				y + SLOTS_Y - 11,
				0xFFFFFFFF
		);
		
		str =
			client.
			getNetworkHandler().getPlayerListEntry(
				handler.other.
				playerUUID
			).getProfile()
			.getName();
		textRenderer.drawWithShadow(
				matrices,
				str,
				right - SLOTS_X - SLOTS_SIZE/2 - (textRenderer.getWidth(str) / 2),
				y + SLOTS_Y - 11,
				0xFFFFFFFF
		);
		
		Long st = handler.swapTimeMillis;
		
		if(st != null) {
			double value = Math.ceil((double)(st - System.currentTimeMillis()) / 1000D);
			
			str = Integer.toString((int) value);
			
			textRenderer.drawWithShadow(
					matrices,
					str,
					x + W / 2 - (textRenderer.getWidth(str) / 2),
					y + SLOTS_Y + SLOTS_SIZE/2,
					0xFFFFFFFF
			);
		}
	}
	
	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
	}
	
	static void drawTexture(MatrixStack mats, int x, int y, int w, int h) {
		drawTexture(mats, x, y, 0, 0, w, h, w, h);
	}
	
}
