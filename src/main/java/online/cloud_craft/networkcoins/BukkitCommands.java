/* 
 * CloudCraft-NetworkCoins
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * CloudCraft-NetworkCoins is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * CloudCraft-NetworkCoins is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with CloudCraft-NetworkCoins. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Lesser General Public License.
 */
package online.cloud_craft.networkcoins;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.LoggerFactory;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

class BukkitCommands implements CommandExecutor {

	private final Wrapper wrapper;
	
	BukkitCommands(Wrapper wrapper) {
		this.wrapper = wrapper;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (sender.hasPermission("cloudcraft.networkcoins.use")) {
			sendMessage(sender, wrapper.getCore().getConfig().getFromMap("messages.no-permission", String.class));
			return true;
		}
		if (args.length < 4) {
			sendUsage(sender);
			return true;
		}

		final String cmd = args[0].toLowerCase(Locale.ENGLISH);
		boolean withdraw = false;
		switch (cmd) {
		case "withdraw":
			withdraw = true;
		case "deposit":
			depositOrWithdraw(sender, args, withdraw, cmd);
			return true;
		default:
			break;
		}
		sendUsage(sender);
		return true;
	}
	
	private void depositOrWithdraw(CommandSender sender, String[] args, boolean withdraw, String cmd) {
		final String playerName = args[1];
		Player target = wrapper.getPlugin().getServer().getPlayer(playerName);
		if (target == null) {
			sendMessage(sender, "&cPlayer &e" + playerName + "&c does not exist");
			return;
		}
		int amount;
		try {
			amount = Integer.parseInt(args[2]);
		} catch (NumberFormatException ex) {
			sendMessage(sender, "&e" + args[2] + "&c is not a number");
			return;
		}
		if (amount < 0) {
			sendMessage(sender, "&cAmount must not be negative.");
			return;
		}
		UUID uuid = target.getUniqueId();
		DAO dao = wrapper.getCore().getDAO();
		CompletableFuture<Boolean> future;
		if (withdraw) {
			future = dao.withdrawCoins(uuid, amount);
		} else {
			future = dao.depositCoins(uuid, amount).thenApply((ignore) -> true);
		}
		String commandLine = concatRemaining(args, 3);
		future.thenAccept((proceed) -> {
			if (!proceed) {
				sendMessage(sender, "&cTransaction failed. Player &e" + playerName + "&c does not have enough coins");
				return;
			}
			String cmdToRun = commandLine.replace("%PLAYER%", playerName);
			JavaPlugin plugin = wrapper.getPlugin();
			Server server = plugin.getServer();
			server.getScheduler().runTask(plugin, () -> {
				server.dispatchCommand(server.getConsoleSender(), cmdToRun);
			});
		}).whenComplete((ignore, ex) -> {
			if (ex != null) {
				LoggerFactory.getLogger(BukkitCommands.class).error("Encountered error while processing command {}", cmd, ex);
			}
		});
	}
	
	private void sendUsage(CommandSender sender) {
		sendMessage(sender, "&cUsage: /networkcoins <withdraw|deposit> <player> <amount> <command_callback>");
	}
	
	private void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
	
	/** Visible for testing */
	static String concatRemaining(String[] args, int startIndex) {
		StringBuilder builder = new StringBuilder();
		for (int n = startIndex; n < args.length; n++) {
			builder.append(args[n]);
			if (n != args.length - 1) {
				builder.append(' ');
			}
		}
		return builder.toString();
	}

}
