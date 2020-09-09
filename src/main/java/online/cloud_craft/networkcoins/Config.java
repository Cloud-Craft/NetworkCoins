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
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.Yaml;

class Config {

	private final Map<String, Object> configMap;
	
	Config(Map<String, Object> configMap) {
		this.configMap = configMap;
	}
	
	static Config loadConfig(Path path) {
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

			return new Yaml().load(reader);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
	
	<T> T getFromMap(String key, Class<T> clazz) {
		Objects.requireNonNull(key, "key");
		Objects.requireNonNull(clazz, "clazz");

		Object result = getNested(key);
		if (!clazz.isInstance(result)) {
			throw new IllegalArgumentException("Config key " + key + " is not of type " + clazz.getName());
		}
		return clazz.cast(result);
	}
	
	@SuppressWarnings("unchecked")
	private Object getNested(String key) {
		Map<String, Object> currentMap = configMap;
		String[] keyParts = Pattern.compile(".", Pattern.LITERAL).split(key);
		for (int n = 0; n < keyParts.length; n++) {
			String keyPart = keyParts[n];
			if (n == keyParts.length - 1) {
				return currentMap.get(keyPart);
			}
			currentMap = (Map<String, Object>) currentMap.get(keyPart);
			if (currentMap == null) {
				return null;
			}
		}
		return null;
	}
	
}
