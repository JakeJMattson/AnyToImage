package asciiasimage;

/**
 * Limited utility for handling bytes and conversions
 *
 * @author mattson543
 */
public class ByteUtils
{
	/**
	 * Split integer into 3 values
	 *
	 * @param pixel
	 *            Integer to be split
	 * @return Array of bytes
	 */
	public static byte[] intToBytes(int pixel)
	{
		byte[] bytes = new byte[3];

		bytes[0] = (byte) (pixel >> 16 & 0x0ff);
		bytes[1] = (byte) (pixel >> 8 & 0x0ff);
		bytes[2] = (byte) (pixel & 0x0ff);

		return bytes;
	}

	/**
	 * Combine 3 values into an integer
	 *
	 * @param bytes
	 *            Values to be shifted
	 * @return Resulting integer
	 */
	public static int bytesToInt(int[] bytes)
	{
		return bytes[0] << 16 | bytes[1] << 8 | bytes[2];
	}

	/**
	 * Convert signed bytes into positive integers
	 *
	 * @param bytes
	 *            Bytes to be unsigned
	 * @return Array of positive integers
	 */
	public static int[] unsignBytes(byte[] bytes)
	{
		int[] unsigned = new int[bytes.length];

		for (int i = 0; i < bytes.length; i++)
			unsigned[i] = bytes[i] & 0xFF;

		return unsigned;
	}
}