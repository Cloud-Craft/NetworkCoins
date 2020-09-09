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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class Core implements AutoCloseable {

	private final DAO dao = new DAO(this);
	private final Database database;
	private final Config config;
	
	private final int defaultBalance;
	
	private Core(Database database, Config config, int defaultBalance) {
		this.database = database;
		this.config = config;
		this.defaultBalance = defaultBalance;
	}

	static Core create(Path folder) {
		if (!Files.isDirectory(folder)) {
			try {
				Files.createDirectories(folder);
			} catch (IOException ex) {
				throw new UncheckedIOException("Unable to create plugin directory", ex);
			}
		}
		Path configPath = folder.resolve("config.yml");
		if (!Files.exists(configPath)) {
			try (InputStream inputStream = Core.class.getResource("/config.yml").openStream();
					FileChannel fc = FileChannel.open(configPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
					ReadableByteChannel rbc = Channels.newChannel(inputStream)) {

				fc.transferFrom(rbc, 0, Long.MAX_VALUE);
			} catch (IOException ex) {
				throw new UncheckedIOException("Unable to create config.yml", ex);
			}
			throw new RuntimeException(
					"The config.yml has been created. Please configure authentication details and restart the server. "
					+ "The plugin will now disable itself.");
		}
		Config config = Config.loadConfig(configPath);
		AuthDetails authDetails = new AuthDetails(
				config.getFromMap("auth-details.host", String.class),
				config.getFromMap("auth-details.port", Integer.class),
				config.getFromMap("auth-details.database", String.class),
				config.getFromMap("auth-details.username", String.class),
				config.getFromMap("auth-details.password", String.class));
		Database database = Database.create(authDetails, config.getFromMap("connection-pool-size", Integer.class));
		return new Core(database, config, config.getFromMap("default-balance", Integer.class));
	}
	
	static Core create(AuthDetails authDetails, int poolSize, int defaultBalance) {
		Database database = Database.create(authDetails, poolSize);
		return new Core(database, null, defaultBalance);
	}
	
	@Override
	public void close() {
		getDatabase().close();
	}
	
	Database getDatabase() {
		return database;
	}

	DAO getDAO() {
		return dao;
	}
	
	Config getConfig() {
		return config;
	}

	int getDefaultBalance() {
		return defaultBalance;
	}
	
}
