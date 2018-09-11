package io.github.jakejmattson.anytoimage;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Extract files from images created by the 'FileToImage' class.
 *
 * @author JakeJMattson
 */
final class ImageToFile
{
	private ImageToFile()
	{
		throw new IllegalStateException("Stateless class");
	}

	/**
	 * Static method to initiate the conversion.
	 *
	 * @param inputFiles
	 *            List of image files to be converted
	 * @param outputDir
	 *            Directory to store all output files in
	 */
	static boolean convert(List<File> inputFiles, File outputDir)
	{
		boolean wasSuccessful = false;

		for (File file : inputFiles)
		{
			//Extract individual pixels from an image
			int[] pixels = extractPixels(file);

			if (pixels == null)
				continue;

			//Separate pixels into bytes
			byte[] allBytes = extractBytes(pixels);

			//Create files from bytes
			if (createFiles(allBytes, outputDir))
				wasSuccessful = true;
		}

		return wasSuccessful;
	}

	/**
	 * Read an image from a file and extract pixels.
	 *
	 * @param file
	 *            File containing the image to be read
	 * @return Pixels from image
	 */
	private static int[] extractPixels(File file)
	{
		int[] pixels = null;

		try
		{
			//Read image from file
			BufferedImage fileImage = ImageIO.read(file);

			//Create a buffered image with the desired type
			BufferedImage image = new BufferedImage(fileImage.getWidth(), fileImage.getHeight(),
					BufferedImage.TYPE_INT_RGB);

			//Draw the image from the file into the buffer
			image.getGraphics().drawImage(fileImage, 0, 0, null);

			//Read all pixels from image
			pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		}
		catch (IOException e)
		{
			DialogDisplay.displayException(e, "Failed to read image: " + file.toString());
		}

		return pixels;
	}

	/**
	 * Extract bytes from each pixel.
	 *
	 * @param pixels
	 *            Int array containing all pixels from the image
	 * @return Bytes
	 */
	private static byte[] extractBytes(int[] pixels)
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		//Read channels from pixel
		for (int pixel : pixels)
			try
			{
				stream.write(ByteUtils.intToBytes(pixel, 3));
			}
			catch (IOException e)
			{
				DialogDisplay.displayException(e, "Error writing array to stream!");
			}

		return stream.toByteArray();
	}

	/**
	 * Create all files contained within the image.
	 *
	 * @param bytes
	 *            File names and data as a byte array
	 * @param outputDir
	 *            Directory to store all output files in
	 */
	private static boolean createFiles(byte[] bytes, File outputDir)
	{
		boolean filesExtracted = false;
		int index = 0;
		File newFile = null;
		List<File> allNewFiles = new ArrayList<>();

		try
		{
			while (index != bytes.length)
			{
				ByteArrayOutputStream name = new ByteArrayOutputStream();
				ByteArrayOutputStream data = new ByteArrayOutputStream();

				//Calculate the number of bytes in each cluster (name/data)
				byte[] sizeBytes = new byte[] {0, 0, bytes[index++]};
				int clusterLength = ByteUtils.bytesToInt(sizeBytes);

				//EOF
				if (clusterLength == 0)
					break;

				for (int i = 0; i < clusterLength; i++)
					name.write(bytes[index++]);

				sizeBytes = new byte[] {bytes[index++], bytes[index++], bytes[index++], bytes[index++]};
				clusterLength = ByteUtils.bytesToInt(sizeBytes);

				for (int i = 0; i < clusterLength; i++)
					data.write(bytes[index++]);

				//Create file
				newFile = new File(outputDir + File.separator + new String(name.toByteArray()));
				File parentDir = newFile.getParentFile();
				allNewFiles.add(newFile);

				if (!parentDir.exists())
					parentDir.mkdirs();

				Files.write(newFile.toPath(), data.toByteArray());
				filesExtracted = true;

				name.reset();
				data.reset();
			}
		}
		catch (InvalidPathException | ArrayIndexOutOfBoundsException e)
		{
			filesExtracted = false;

			for (File file : allNewFiles)
				file.delete();

			DialogDisplay.displayException(e, "Incorrectly encoded input image!");
		}
		catch (IOException e)
		{
			//General case
			DialogDisplay.displayException(e, "Failed to create file: " + newFile.toString());
		}

		return filesExtracted;
	}
}