package asciiasimage;

import java.awt.Color;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * Extract files from images created by the 'TextToImage' class
 *
 * @author mattson543
 */
public class ImageToText
{
	/**
	 * Static method to initiate the conversion.
	 *
	 * @param inputFiles
	 *            Array of image files to be converted
	 * @param outputDir
	 *            Directory to store all output files in
	 * @param unitSeparator
	 *            Separates files and contents (name; text; name; text...)
	 */
	public static void convert(File[] inputFiles, File outputDir, byte unitSeparator)
	{
		for (File file : inputFiles)
		{
			//Extract individual pixels from an image
			int[] pixels = extractPixels(file);

			//Separate pixels into bytes
			byte[] allBytes = extractBytes(pixels);

			//Create files from bytes
			createFiles(allBytes, unitSeparator, outputDir);
		}

		JOptionPane.showMessageDialog(null, "All valid files have been extracted from the input.",
				"Extraction Complete!", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Read an image from a file.
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
			e.printStackTrace();
		}

		return pixels;
	}

	/**
	 * Extract bytes from each pixel
	 *
	 * @param pixels
	 *            Int array containing all pixels from the image
	 * @return Bytes
	 */
	private static byte[] extractBytes(int[] pixels)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		for (int pixel : pixels)
		{
			//Create color object from pixel info
			Color color = new Color(pixel);

			//Read channels from color object
			bytes.write((byte) color.getRed());
			bytes.write((byte) color.getGreen());
			bytes.write((byte) color.getBlue());
		}

		return bytes.toByteArray();
	}

	/**
	 * Create all files contained within the image
	 *
	 * @param bytes
	 *            File names and data as a byte array
	 * @param unitSeparator
	 *            Separates files and contents
	 * @param outputDir
	 *            Directory to store all output files in
	 */
	private static void createFiles(byte[] bytes, byte unitSeparator, File outputDir)
	{
		//File name and contents
		ByteArrayOutputStream name = new ByteArrayOutputStream();
		ByteArrayOutputStream data = new ByteArrayOutputStream();

		//Control flow
		boolean isName = true;

		for (byte currentByte : bytes)
			if (currentByte != unitSeparator)
			{
				//Store byte
				if (isName)
					name.write(currentByte);
				else
					data.write(currentByte);
			}
			else
			{
				if (!isName)
				{
					try
					{
						//Create file
						Path path = Paths.get(outputDir + "/" + new String(name.toByteArray()));
						Files.write(path, data.toByteArray());
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}

					//Clear stream once file is created
					name.reset();
					data.reset();
				}

				isName = !isName;
			}
	}
}