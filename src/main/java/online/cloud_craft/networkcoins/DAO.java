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

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

class DAO implements NetworkCoins {

	private final Core core;
	
	DAO(Core core) {
		this.core = core;
	}
	
	@Override
	public CompletableFuture<Boolean> withdrawCoins(UUID uuid, int amount) {
		Objects.requireNonNull(uuid, "uuid");
		if (amount == 0) {
			return CompletableFuture.completedFuture(true);
		}
		if (amount < 0) {
			throw new IllegalArgumentException("Amount " + amount + " must be non-negative");
		}
		Database database = core.getDatabase();
		return database.selectAsync(() -> {
			byte[] uuidBytes = UUIDUtil.toByteArray(uuid);
			Boolean boxedResult = database.jdbCaesar().transaction().body((querySource, controller) -> {
				querySource.query(
						"INSERT IGNORE INTO `networkcoins_coins` (`uuid`, `balance`) VALUES (?, ?)")
						.params(uuidBytes, core.getDefaultBalance())
						.voidResult().execute();
				return querySource.query(
						"UPDATE `networkcoins_coins` SET `balance` = `balance` - ? WHERE `uuid` = ? AND `balance` > ?")
						.params(amount, uuidBytes, amount)
						.updateCount((updateCount) -> updateCount == 1)
						.execute();
			}).onError(() -> false).execute();
			boolean result = boxedResult;
			return result;
		});
	}

	@Override
	public CompletableFuture<?> depositCoins(UUID uuid, int amount) {
		Objects.requireNonNull(uuid, "uuid");
		if (amount == 0) {
			return CompletableFuture.completedFuture(null);
		}
		if (amount < 0) {
			throw new IllegalArgumentException("Amount " + amount + " must be non-negative");
		}
		Database database = core.getDatabase();
		return database.executeAsync(() -> {
			database.jdbCaesar().query(
					"INSERT INTO `networkcoins_coins` (`uuid`, `balance`) VALUES (?, ?) "
					+ "ON DUPLICATE KEY UPDATE `balance` = `balance` + ?")
					.params(uuid, amount, amount)
					.voidResult().execute();
		});
	}
	
}
