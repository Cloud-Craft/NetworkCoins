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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class NetworkCoinsPlugin extends JavaPlugin {

	private Wrapper wrapper;
	
	private static final Logger logger = LoggerFactory.getLogger(NetworkCoinsPlugin.class);
	
	@Override
	public synchronized void onEnable() {
		if (wrapper != null) {
			logger.warn("Someone attempted to enable NetworkCoins twice");
			return;
		}
		Wrapper wrapper = new Wrapper(this, getDataFolder().toPath());
		Core core = wrapper.start();
		getServer().getServicesManager().register(NetworkCoins.class, core.getDAO(), this, ServicePriority.Low);
	}
	
	@Override
	public synchronized void onDisable() {
		if (wrapper == null) {
			logger.warn("NetworkCoins not initialised, disabling is a no-op");
			return;
		}
		wrapper.close();
		wrapper = null;
	}
	
}
