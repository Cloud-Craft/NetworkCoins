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

import java.nio.file.Path;

import org.bukkit.plugin.java.JavaPlugin;

class Wrapper {

	private final JavaPlugin plugin;
	private final Path folder;
	
	private volatile Core core;
	
	Wrapper(JavaPlugin plugin, Path folder) {
		this.plugin = plugin;
		this.folder = folder;

		plugin.getCommand("networkcoins").setExecutor(new BukkitCommands(this));
	}
	
	JavaPlugin getPlugin() {
		return plugin;
	}
	
	Core start() {
		Core core = Core.create(folder);
		this.core = core;
		return core;
	}
	
	Core getCore() {
		return core;
	}
	
	void close() {
		//HandlerList.unregisterAll(plugin);
		plugin.getCommand("networkcoins").setExecutor(plugin);
		core.close();
	}
	
}
