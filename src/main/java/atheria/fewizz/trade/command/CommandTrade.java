package atheria.fewizz.trade.command;

import atheria.fewizz.trade.Trade;
import atheria.fewizz.trade.Trade;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandTrade extends CommandBase {

	@Override
	public String getName() {
		return "trade";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/trade <player_name>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 1) {
			//TradeMod.NETWORK_WRAPPER.sendToServer(new MessageCTradeRequest(args[0]));
			Trade.onClientTradeRequest(getCommandSenderAsPlayer(sender), args[0]);
		}
	}

}
