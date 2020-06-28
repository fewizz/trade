package ru.fewizz.trade;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class TradeButtonWidget extends ButtonWidget {
	public static int
		W = TradeScreen.SLOTS_SIZE + 10,
		H = 20;
	final AbstractTradeScreenHandler<?, ?> screen;
	
	public TradeButtonWidget(AbstractTradeScreenHandler<?, ?> handler, int x, int y, PressAction action) {
		super(x, y, W, H, null, action);
		this.screen = handler;
	}
	
	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		setMessage(new LiteralText(screen.getState().isReady() ? "Ready" : "Not ready"));
		super.renderButton(matrices, mouseX, mouseY, delta);
	}

}
