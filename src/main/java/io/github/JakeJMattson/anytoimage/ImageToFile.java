package io.github.jakejmattson.anytoimage;

import java.awt.image.*;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * Extract files from images created by the 'FileToImage' class.
 *
 * @author JakeJMattson
 */
public class ImageToFile
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
	 * @param displayMode
	 *            How to display information to a user: GUI = true; CLI = false
	 */
	public static void convert(List<File> inputFiles, File outputDir, boolean displayMode)
	{
		for (File file : inputFiles)
		{
			//Extract individual pixels from an image
			int[] pixels = extractPixels(file);

			//Separate pixels into bytes
			byte[] allBytes = extractBytes(pixels);

			//Create files from bytes
			createFiles(allBytes, outputDir);
		}

		if (displayMode)
			JOptionPane.showMessageDialog(null, "All valid files have been extracted!", "Extraction Complete!",
					JOptionPane.INFORMATION_MESSAGE);
		else
			System.out.println("Extraction Complete!");
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
			System.out.println("Failed to read image: " + file.toString());
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
				//Mandatory catch when writing array to stream
				//Failure expanding stream when heap is full
				System.out.println("Error writing array to stream!");
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
	private static void createFiles(byte[] bytes, File outputDir)
	{
		//Create stream to store file info
		ByteArrayOutputStream name = new ByteArrayOutputStream();
		ByteArrayOutputStream data = new ByteArrayOutputStream();

		boolean isName = true;
		int index = 0;

		while (index != bytes.length)
		{
			//Calculate the number of bytes in each cluster (name/data)
			byte[] sizeBytes;

			if (isName)
				sizeBytes = new byte[] {0, 0, bytes[index++]};
			else
				sizeBytes = new byte[] {bytes[index++], bytes[index++], bytes[index++], bytes[index++]};

			int clusterLength = ByteUtils.bytesToInt(sizeBytes);

			//EOF
			if (clusterLength == 0)
				break;

			for (int i = 0; i < clusterLength; i++)
				if (isName)
					name.write(bytes[index++]);
				else
					data.write(bytes[index++]);

			if (!isName)
			{
				//Create file
				File newFile = new File(outputDir + File.separator + new String(name.toByteArray()));
				File parentDir = newFile.getParentFile();

				if (!parentDir.exists())
					parentDir.mkdirs();

				try
				{
					Files.write(newFile.toPath(), data.toByteArray());
				}
				catch (IOException e)
				{
					System.out.println("Failed to create file: " + newFile.toString());
				}

				//Clear streams
				name.reset();
				data.reset();
			}

			isName = !isName;
		}
	}
}