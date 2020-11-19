package ru.fewizz.trade.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import ru.fewizz.trade.TradeScreenHandler;

@Environment(EnvType.CLIENT)
public class TradeButtonWidget extends ButtonWidget {
	public static final int
		W = TradeScreen.SLOTS_SIZE + 10,
		H = 20;
	final TradeScreenHandler<?, ?> screen;
	
	public TradeButtonWidget(TradeScreenHandler<?, ?> handler, int x, int y, PressAction action) {
		super(x, y, W, H, new LiteralText(""), action);
		this.screen = handler;
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		setMessage(
			new TranslatableText(
				screen.getState().isReady() ? "trade.gui.button.ready" : "trade.gui.button.not_ready"
			)
		);
		super.renderButton(matrices, mouseX, mouseY, delta);
	}

}
