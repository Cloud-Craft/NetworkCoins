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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import space.arim.jdbcaesar.ConnectionSource;
import space.arim.jdbcaesar.JdbCaesar;
import space.arim.jdbcaesar.adapter.DataTypeAdapter;
import space.arim.jdbcaesar.builder.JdbCaesarBuilder;

class Database {

	private final HikariDataSource dataSource;
	private final ExecutorService threadPool;
	private final JdbCaesar jdbCaesar;
	
	private static final Logger logger = LoggerFactory.getLogger(Database.class);
	
	private Database(HikariConfig hikariConf) {
		dataSource = new HikariDataSource(hikariConf);
		threadPool = Executors.newFixedThreadPool(hikariConf.getMaximumPoolSize());

		jdbCaesar = new JdbCaesarBuilder()
				.connectionSource(new ConnectionSourceImpl())
				.exceptionHandler((ex) -> logger.error("Error while executing a database query", ex))
				.addAdapter(new UUIDAdapter())
				.rewrapExceptions(true)
				.build();

		boolean success = jdbCaesar.query(
				"CREATE TABLE IF NOT EXISTS `networkcoins_coins` ("
				+ "`uuid` BINARY(16) PRIMARY KEY,"
				+ "`balance` INT NOT NULL)")
				.updateCount((updateCount) -> true)
				.onError(() -> false).execute();
		if (!success) {
			throw new IllegalStateException("Failed to create table 'networkcoins_coins'");
		}
	}
	
	<T> CompletableFuture<T> selectAsync(Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier, threadPool);
	}
	
	CompletableFuture<?> executeAsync(Runnable command) {
		return CompletableFuture.runAsync(command, threadPool);
	}
	
	JdbCaesar jdbCaesar() {
		return jdbCaesar;
	}
	
	static Database create(AuthDetails authDetails, int poolSize) {
		HikariConfig hikariConf = new HikariConfig();

		hikariConf.setJdbcUrl("jdbc:mysql://" + authDetails.host + ':' + authDetails.port + '/' + authDetails.database);
		hikariConf.setUsername(authDetails.username);
		hikariConf.setPassword(authDetails.password);

		hikariConf.setMinimumIdle(poolSize);
		hikariConf.setMaximumPoolSize(poolSize);
		hikariConf.setAutoCommit(false);

		return new Database(hikariConf);
	}
	
	void close() {
		dataSource.close();
		threadPool.shutdown();
	}
	
	private class ConnectionSourceImpl implements ConnectionSource {
		@Override
		public Connection getConnection() throws SQLException {
			return dataSource.getConnection();
		}

		@Override
		public void close() throws SQLException {}
	}
	
	private static class UUIDAdapter implements DataTypeAdapter {

		@Override
		public Object adaptObject(Object parameter) {
			if (parameter instanceof UUID) {
				return UUIDUtil.toByteArray((UUID) parameter);
			}
			return parameter;
		}
		
	}
	
}
