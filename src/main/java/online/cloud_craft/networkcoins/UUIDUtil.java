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

final class UUIDUtil {

	private UUIDUtil() {}
	
	/**
	 * Creates a byte array and writes a UUID to it. This would be the inverse operation of
	 * {@link #fromByteArray(byte[])}
	 * 
	 * @param uuid the UUID
	 * @return the byte array, will always be length 16
	 */
	static byte[] toByteArray(UUID uuid) {
		byte[] result = new byte[16];
		toByteArray(uuid, result, 0);
		return result;
	}
	
	private static void toByteArray(UUID uuid, byte[] byteArray, int offset) {
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();

		for (int i = 7; i >= 0; i--) {
			byteArray[offset + i] = (byte) (msb & 0xffL);
			msb >>= 8;
		}
		for (int i = 15; i >= 8; i--) {
			byteArray[offset + i] = (byte) (lsb & 0xffL);
			lsb >>= 8;
		}
	}
	
	/**
	 * Reads a UUID from a byte array. This is the inverse operation of {@link #toByteArray(UUID)}.
	 * 
	 * @param byteArray the byte array to read from, must be at least of length 16
	 * @return the UUID
	 */
	static UUID fromByteArray(byte[] byteArray) {
		return new UUID(
				longFromBytes(byteArray[0], byteArray[1], byteArray[2], byteArray[3], byteArray[4], byteArray[5], byteArray[6], byteArray[7]),
				longFromBytes(byteArray[8], byteArray[9], byteArray[10], byteArray[11], byteArray[12], byteArray[13], byteArray[14], byteArray[15]));
	}
	
	private static long longFromBytes(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
		return (b1 & 0xffL) << 56
				| (b2 & 0xffL) << 48
				| (b3 & 0xffL) << 40
				| (b4 & 0xffL) << 32
				| (b5 & 0xffL) << 24
				| (b6 & 0xffL) << 16
				| (b7 & 0xffL) << 8
				| (b8 & 0xffL);
	}
	
}
