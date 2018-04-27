package asciiasimage;

import java.awt.Color;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

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
			int[] pixels = extractPixels(file);

			byte[] allBytes = extractBytes(pixels);

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
		List<byte[]> allName = new ArrayList<>();
		List<byte[]> allData = new ArrayList<>();

		ByteArrayOutputStream name = new ByteArrayOutputStream();
		ByteArrayOutputStream data = new ByteArrayOutputStream();

		boolean isName = true;

		for (byte currentByte : bytes)
			if (currentByte != unitSeparator)
			{
				if (isName)
					name.write(currentByte);
				else
					data.write(currentByte);
			}
			else
			{
				if (isName)
				{
					allName.add(name.toByteArray());
					name = new ByteArrayOutputStream();
				}
				else
				{
					allData.add(data.toByteArray());
					data = new ByteArrayOutputStream();
				}

				isName = !isName;
			}

		for (int i = 0; i < allName.size(); i++)
			try
			{
				Path path = Paths.get(outputDir + "/" + new String(allName.get(i)));
				Files.write(path, allData.get(i));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
	}
}