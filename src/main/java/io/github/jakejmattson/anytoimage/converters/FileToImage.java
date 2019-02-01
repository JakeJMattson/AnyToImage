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
package io.github.jakejmattson.anytoimage.converters;

import io.github.jakejmattson.anytoimage.utils.*;

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
public final class FileToImage
{
	private static ByteArrayOutputStream stream = new ByteArrayOutputStream();
	private static BufferedImage image;
	private static int row;
	private static int col;

	private static final int CHANNEL_COUNT = 3;

	private FileToImage(){}

	/**
	 * Static method to initiate the conversion.
	 *
	 * @param inputFiles
	 * 		List of files to be converted
	 * @param outputFile
	 * 		Image file to be output when conversion is complete
	 */
	public static boolean convert(List<File> inputFiles, File outputFile)
	{
		inputFiles = inputFiles.stream().filter(File::exists).collect(Collectors.toList());

		int bytes = calculateBytesRequired(inputFiles);
		int dims = (int) Math.ceil(Math.sqrt(bytes / CHANNEL_COUNT));

		if (dims == 0)
			return false;

		//(Re)initialize fields
		image = new BufferedImage(dims, dims, BufferedImage.TYPE_INT_RGB);
		row = 0;
		col = 0;

		for (File file : inputFiles)
			if (file.isDirectory())
				directoryToBytes(file);
			else
				fileToBytes(file, file.getName());

		finalizeStream();
		return saveImage(outputFile);
	}

	/**
	 * Calculate the number of bytes needed to store all files in the list.
	 *
	 * @param inputFiles
	 * 		List of files to be converted
	 *
	 * @return Number of bytes required
	 */
	private static int calculateBytesRequired(List<File> inputFiles)
	{
		int byteCount = 0;

		for (File file : inputFiles)
			if (file.isDirectory())
			{
				String parentDir = file.getName();
				List<File> directoryFiles = FileUtils.walkDirectory(file);

				for (File directoryFile : directoryFiles)
				{
					String fullPath = directoryFile.toPath().toString();
					String fileName = fullPath.substring(fullPath.indexOf(parentDir));

					byteCount += calculateFileSize(directoryFile, fileName);
				}
			}
			else
				byteCount += calculateFileSize(file, file.getName());

		return byteCount;
	}

	/**
	 * Calculate the number of bytes needed to store and recreate a file.
	 *
	 * @param file
	 * 		File to be sized
	 * @param fileName
	 * 		Name of file to be sized
	 *
	 * @return Number of bytes
	 */
	private static int calculateFileSize(File file, String fileName)
	{
		final int SIZE_BYTES = 5;
		return (int) (fileName.getBytes().length + file.length() + SIZE_BYTES);
	}

	/**
	 * Collect all files from a directory (and sub-directories) and convert each to bytes.
	 *
	 * @param dir
	 * 		Directory to extract bytes from
	 */
	private static void directoryToBytes(File dir)
	{
		String parentDir = dir.getName();
		List<File> files = FileUtils.walkDirectory(dir);

		for (File file : files)
		{
			String fullPath = file.toPath().toString();
			String fileName = fullPath.substring(fullPath.indexOf(parentDir));

			fileToBytes(file, fileName);
		}
	}

	/**
	 * Collect all necessary file information as bytes and write it into the stream.
	 *
	 * @param file
	 * 		File to extract bytes from
	 * @param fileName
	 * 		Name of file (with folder structure if file was in directory)
	 */
	private static void fileToBytes(File file, String fileName)
	{
		try
		{
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

	/**
	 * Create pixels from stream data and write them onto the image; remove data from stream.
	 */
	private static void writeDataToImage()
	{
		byte[] fileBytes = stream.toByteArray();
		int index;

		for (index = 0; index < fileBytes.length - CHANNEL_COUNT; index += CHANNEL_COUNT)
		{
			byte[] pixelData = {fileBytes[index], fileBytes[index + 1], fileBytes[index + 2]};
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

		for (; index < fileBytes.length; index++)
			stream.write(fileBytes[index]);
	}

	/**
	 * Write remaining stream data onto the image and reset the stream.
	 */
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
	 * Write image to disk.
	 *
	 * @param output
	 * 		The desired location of the output image
	 *
	 * @return Whether or not the image was written to disk
	 */
	private static boolean saveImage(File output)
	{
		boolean status = false;

		try
		{
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