package io.github.jakejmattson.anytoimage;

/**
 * Limited utility class for handling bytes and conversions.
 *
 * @author JakeJMattson
 */
final class ByteUtils
{
	private ByteUtils()
	{
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Split integer into bytes.
	 *
	 * @param pixel
	 *            Integer to be split
	 * @param count
	 *            Number of bytes desired
	 * @return Array of bytes
	 */
	static byte[] intToBytes(int pixel, int count)
	{
		byte[] bytes = new byte[count];

		for (int i = 0; i < count; i++)
			bytes[i] = (byte) (pixel >> (count - i - 1) * 8 & 0x0ff);

		return bytes;
	}

	/**
	 * Combine values into an integer.
	 *
	 * @param bytes
	 *            Values to be shifted
	 * @return Resulting integer
	 */
	static int bytesToInt(byte[] bytes)
	{
		//Un-sign bytes
		int[] unsigned = new int[bytes.length];

		for (int i = 0; i < bytes.length; i++)
			unsigned[i] = bytes[i] & 0xFF;

		if (unsigned.length == 3)
			return unsigned[0] << 16 | unsigned[1] << 8 | unsigned[2];
		else if (unsigned.length == 4)
			return unsigned[0] << 24 | unsigned[1] << 16 | unsigned[2] << 8 | unsigned[3];
		else
			return -1;
	}
}