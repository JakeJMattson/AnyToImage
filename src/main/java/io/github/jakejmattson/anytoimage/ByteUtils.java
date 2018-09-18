/*
 * The MIT License
 * Copyright © 2018 Jake Mattson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.jakejmattson.anytoimage;

/**
 * Limited utility class for handling bytes and conversions.
 *
 * @author JakeJMattson
 */
final class ByteUtils
{
	private ByteUtils(){}

	/**
	 * Split integer into bytes.
	 *
	 * @param pixel
	 * 		Integer to be split
	 * @param count
	 * 		Number of bytes desired
	 *
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
	 * 		Values to be shifted
	 *
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