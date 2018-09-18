/**
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Create images from files by converting bytes to RGB values.
 *
 * @author JakeJMattson
 */
final class FileToImage
{
	private static ByteArrayOutputStream stream = new ByteArrayOutputStream();
	private static BufferedImage image;
	private static int row;
	private static int col;

	private static final int CHANNEL_COUNT = 3;

	private FileToImage()
	{
		throw new IllegalStateException("Stateless class");
	}

	/**
	 * Static method to initiate the conversion.
	 *
	 * @param inputFiles
	 *            List of files to be converted
	 * @param outputFile
	 *            Image file to be output when conversion is complete
	 */
	static boolean convert(List<File> inputFiles, File outputFile)
	{
		int bytes = calculatePixelCount(inputFiles);
		int dims = (int) Math.ceil(Math.sqrt(bytes / CHANNEL_COUNT));

		//No data to write
		if (dims == 0)
			return false;

		//(Re)initialize fields
		image = new BufferedImage(dims, dims, BufferedImage.TYPE_INT_RGB);
		row = 0;
		col = 0;

		//Get file bytes
		for (File file : inputFiles)
			if (file.exists())
				if (file.isDirectory())
					directoryToBytes(file);
				else
					fileToBytes(file, file.getName());

		//Write remaining data onto the image
		finalizeStream();

		//Create image and return success status
		return createImage(outputFile);
	}

	private static int calculatePixelCount(List<File> inputFiles)
	{
		int byteCount = 0;

		for (File file : inputFiles)
			if (file.exists())
				if (file.isDirectory())
				{
					String parentDir = file.getName();
					List<Path> paths = walkDirectory(file);

					for (Path path : paths)
					{
						String fullPath = path.toString();
						String fileName = fullPath.substring(fullPath.indexOf(parentDir));

						byteCount += pixelSizeOfFile(path.toFile(), fileName);
					}
				}
				else
					byteCount += pixelSizeOfFile(file, file.getName());

		return byteCount;
	}

	private static int pixelSizeOfFile(File file, String fileName)
	{
		int byteCount = 0;

		byteCount += 1;
		byteCount += fileName.getBytes().length;
		byteCount += 4;
		byteCount += file.length();

		return byteCount;
	}

	/**
	 * Turn directory into bytes and preserve directory structure.
	 *
	 * @param dir
	 *            Directory to extract bytes from
	 */
	private static void directoryToBytes(File dir)
	{
		String parentDir = dir.getName();
		List<Path> paths = walkDirectory(dir);

		for (Path path : paths)
		{
			//Construct arguments
			String fullPath = path.toString();
			String fileName = fullPath.substring(fullPath.indexOf(parentDir));

			//Retrieve bytes from each file
			fileToBytes(path.toFile(), fileName);
		}
	}

	/**
	 * Turn file into bytes.
	 *
	 * @param file
	 *            File to extract bytes from
	 * @param fileName
	 *            Name of file (with folder structure if file was in directory)
	 */
	private static void fileToBytes(File file, String fileName)
	{
		try
		{
			//Acquire file information
			stream.write((byte) fileName.length());
			stream.write(fileName.getBytes());
			stream.write(ByteUtils.intToBytes((int) file.length(), 4));
			stream.write(Files.readAllBytes(file.toPath()));

			writeDataToImage();
		}
		catch (IOException e)
		{
			DialogDisplay.displayException(e, "Unable to read file: " + file.toString());
		}
	}

	private static List<Path> walkDirectory(File dir)
	{
		try (Stream<Path> files = Files.walk(dir.toPath()))
		{
			return files.filter(Files::isRegularFile).collect(Collectors.toList());
		}
		catch (IOException e)
		{
			DialogDisplay.displayException(e, "Unable to walk directory: " + dir.toString());
			return Collections.emptyList();
		}
	}

	private static void writeDataToImage()
	{
		byte[] fileBytes = stream.toByteArray();
		int i;

		for (i = 0; i < fileBytes.length - CHANNEL_COUNT; i += CHANNEL_COUNT)
		{
			byte[] pixelData = {fileBytes[i], fileBytes[i + 1], fileBytes[i + 2]};
			int pixel = ByteUtils.bytesToInt(pixelData);

			image.setRGB(row, col, pixel);

			row++;

			if (row == image.getWidth())
			{
				row = 0;
				col++;
			}
		}

		stream.reset();

		for ( ; i < fileBytes.length; i++)
		{
			stream.write(fileBytes[i]);
		}
	}

	private static void finalizeStream()
	{
		byte[] finalPixelData = new byte[CHANNEL_COUNT];
		byte[] partialPixel = stream.toByteArray();
		System.arraycopy(partialPixel, 0, finalPixelData, 0, partialPixel.length);
		int pixel = ByteUtils.bytesToInt(finalPixelData);
		image.setRGB(row, col, pixel);

		stream.reset();
	}

	/**
	 * Create an image from pixels and save to disk.
	 *
	 * @param output
	 *            The desired file location of the output image
	 * @return Whether or not the image was written to disk
	 */
	private static boolean createImage(File output)
	{
		boolean status = false;

		try
		{
			//Write image to file
			ImageIO.write(image, "png", output);
			status = true;
		}
		catch (IOException e)
		{
			DialogDisplay.displayException(e, "Error creating image!");
		}
		finally
		{
			image = null;
		}

		return status;
	}
}