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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Entry point for Network Coins
 * 
 * @author A248
 *
 */
public interface NetworkCoins {

	/**
	 * Attempts to withdraw the specified amount of coins from the player's account.
	 * If {@code amount} is 0, this is a no op and the future yields {@code true}
	 * 
	 * @param uuid the UUID of the player whose coins to withdraw from
	 * @param amount the amount to withdraw
	 * @return a future which yields true if withdrawn successfully, false otherwise
	 * @throws IllegalArgumentException if {@code amount} is less than 0
	 */
	CompletableFuture<Boolean> withdrawCoins(UUID uuid, int amount);
	
	/**
	 * Deposits the specified amount of coins in the player's account. If {@code amount}
	 * is 0, this is a no op.
	 * 
	 * @param uuid the UUID of the player whose coins to add to
	 * @param amount the amount to deposit
	 * @return a future which completes when the coins have been deposited
	 */
	CompletableFuture<?> depositCoins(UUID uuid, int amount);
	
}
