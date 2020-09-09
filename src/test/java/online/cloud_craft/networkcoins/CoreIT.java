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

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;

public class CoreIT {

	private final UUID uuid = UUID.randomUUID();
	
	@TempDir
	public Path folder;
	
	private Core core;
	
	private static AuthDetails authDetails;
	
	@BeforeAll
	public static void setup() {
		DB db;
		try {
			db = DB.newEmbeddedDB(0);
			db.start();
		} catch (ManagedProcessException ex) {
			fail(ex);
			return;
		}
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				db.stop();
			} catch (ManagedProcessException ex) {
				ex.printStackTrace();
			}
		}));
		authDetails = new AuthDetails("localhost", db.getConfiguration().getPort(), "test", "root", "");
	}
	
	@BeforeEach
	public void setupEach() {
		core = Core.create(authDetails, 1, 200);
	}
	
	@Test
	public void testDAO() {
		DAO dao = core.getDAO();

		// Test withdrawal of amount beyond starting balance
		int amountOver200 = ThreadLocalRandom.current().nextInt(201, Integer.MAX_VALUE);
		assertFalse(dao.withdrawCoins(uuid, amountOver200).join());

		// Test withdrawal of amount below starting balance
		int amountBetween100And200 = ThreadLocalRandom.current().nextInt(101, 200);
		assertTrue(dao.withdrawCoins(uuid, amountBetween100And200).join());

		// Test user no longer has enough coins
		assertFalse(dao.withdrawCoins(uuid, amountBetween100And200).join());

		// Test that deposit re-enables withdrawal
		dao.depositCoins(uuid, amountBetween100And200);
		assertTrue(dao.withdrawCoins(uuid, amountBetween100And200).join());

		CompletableFuture<?> randomDeposit = dao.depositCoins(uuid, ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
		randomDeposit.join();
		assertFalse(randomDeposit.isCompletedExceptionally());

		// Test withdrawal when balance is zero
		reduceBalanceToZero(dao);
		assertFalse(dao.withdrawCoins(uuid, ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE)).join());
	}
	
	private void reduceBalanceToZero(DAO dao) {
		int biteSize = Integer.MAX_VALUE >> 4;
		while (true) {
			boolean withdraw = dao.withdrawCoins(uuid, biteSize).join();
			if (!withdraw) {
				if (biteSize == 1) {
					break;
				}
				if (biteSize > 16) {
					biteSize >>= 4;
				} else if (biteSize > 4) {
					biteSize >>= 2;
				} else {
					biteSize = 1;
				}
			}
		}
	}
	
}
