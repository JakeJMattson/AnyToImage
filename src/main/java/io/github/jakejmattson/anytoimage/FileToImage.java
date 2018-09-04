package io.github.jakejmattson.anytoimage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.*;

/**
 * Create images from files by converting bytes to RGB values.
 *
 * @author JakeJMattson
 */
public class FileToImage
{
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
	public static boolean convert(List<File> inputFiles, File outputFile)
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		//Get file bytes
		for (File file : inputFiles)
			if (file.exists())
				if (file.isDirectory())
					directoryToBytes(stream, file);
				else if (file.isFile())
					fileToBytes(stream, file, file.getName());

		//Create pixels from file information
		int[] pixels = bytesToPixels(stream.toByteArray());

		//Create image and return success status
		return createImage(pixels, outputFile);
	}

	/**
	 * Turn directory into bytes and preserve directory structure.
	 *
	 * @param stream
	 *            Byte stream currently open
	 * @param file
	 *            Directory to extract bytes from
	 */
	private static void directoryToBytes(ByteArrayOutputStream stream, File file)
	{
		//Get selected directory
		String parentDir = file.getName();

		try (Stream<Path> files = Files.walk(file.toPath()))
		{
			//Get all files from the directory and its sub-directories
			List<Path> paths = files.filter(Files::isRegularFile).collect(Collectors.toList());

			for (Path path : paths)
			{
				//Construct arguments
				String fullPath = path.toString();
				String fileName = fullPath.substring(fullPath.indexOf(parentDir));

				//Retrieve bytes from each file
				fileToBytes(stream, path.toFile(), fileName);
			}
		}
		catch (IOException e)
		{
			ExceptionDisplay.display(e, "Unable to walk directory: " + file.toString());
		}
	}

	/**
	 * Turn file into bytes.
	 *
	 * @param stream
	 *            Byte stream currently open
	 * @param file
	 *            File to extract bytes from
	 * @param fileName
	 *            Name (with / without folder structure)
	 */
	private static void fileToBytes(ByteArrayOutputStream stream, File file, String fileName)
	{
		byte[] name, data, nameLength, dataLength;
		name = data = nameLength = dataLength = null;

		try
		{
			//Acquire file information
			name = fileName.getBytes();
			data = Files.readAllBytes(file.toPath());
			nameLength = new byte[] {(byte) name.length};
			dataLength = ByteUtils.intToBytes(data.length, 4);
		}
		catch (IOException e)
		{
			ExceptionDisplay.display(e, "Unable to read file: " + file.toString());
		}

		try
		{
			if (data != null) //If file successfully read
			{
				//Write information into the stream
				stream.write(nameLength);
				stream.write(name);
				stream.write(dataLength);
				stream.write(data);
			}
		}
		catch (IOException e)
		{
			ExceptionDisplay.display(e, "Error writing array to stream!");
		}
	}

	/**
	 * Covert file bytes into an integer (pixel).
	 *
	 * @param bytes
	 *            All bytes to be converted
	 * @return All pixels
	 */
	private static int[] bytesToPixels(byte[] bytes)
	{
		//Number of image channels (RGB)
		int numOfChannels = 3;

		//Total number of characters to convert
		int byteCount = bytes.length;

		//Determine total number of pixels
		int pixelCount = (int) Math.pow(Math.ceil(Math.sqrt(byteCount / numOfChannels)), 2);
		int[] pixels = new int[pixelCount];

		//Read text in groups of [channel count]
		for (int i = 0; i < byteCount; i += numOfChannels)
		{
			//Array of current pixel info
			byte[] pixel = new byte[numOfChannels];

			//Read info from group into each channel
			for (int j = 0; j < numOfChannels; j++)
				if (i + j < byteCount)
					pixel[j] = bytes[i + j];
				else
					break;

			//Store current pixel into pixel array
			pixels[i / numOfChannels] = ByteUtils.bytesToInt(pixel);
		}

		return pixels;
	}

	/**
	 * Create an image from pixels and save to disk.
	 *
	 * @param pixels
	 *            The pixel data to be written to the image
	 * @param output
	 *            The desired file location of the output image
	 * @return Whether or not the image was written to disk
	 */
	private static boolean createImage(int[] pixels, File output)
	{
		//Calculate image dimensions
		int dims = (int) Math.ceil(Math.sqrt(pixels.length));

		if (dims == 0)
			return false;

		//Store pixel values in image
		BufferedImage image = new BufferedImage(dims, dims, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, dims, dims, pixels, 0, dims);

		try
		{
			//Write image to file
			ImageIO.write(image, "png", output);
			return true;
		}
		catch (IOException e)
		{
			ExceptionDisplay.display(e, "Error creating image!");
			return false;
		}
	}
}