package asciiasimage;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * Create images from text files by converting bytes to RGB values
 *
 * @author mattson543
 */
public class TextToImage
{
	/**
	 * Static method to initiate the conversion.
	 *
	 * @param inputFiles
	 *            Array of text files to be converted
	 * @param outputFile
	 *            Image file to be output by conversion
	 * @param unitSeparator
	 *            Separates files and contents (name; text; name; text...)
	 */
	public static void convert(File[] inputFiles, File outputFile, byte unitSeparator)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		for (File file : inputFiles)
			try
			{
				//Write all necessary file information into the stream
				bytes.write(file.getName().getBytes());
				bytes.write(unitSeparator);
				bytes.write(Files.readAllBytes(file.toPath()));
				bytes.write(unitSeparator);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		//Create pixels from file information
		int[] pixels = bytesToPixels(bytes.toByteArray());

		//Create image from pixels
		createImage(pixels, outputFile);
	}

	/**
	 * Covert file bytes into an integer (pixel)
	 *
	 * @param bytes
	 *            Bytes that make up an file
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
			pixels[i / numOfChannels] = pixel[0] << 16 | pixel[1] << 8 | pixel[2];
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
	 */
	private static void createImage(int[] pixels, File output)
	{
		try
		{
			//Calculate image dimensions
			int dims = (int) Math.ceil(Math.sqrt(pixels.length));

			//Store pixel values in image
			BufferedImage image = new BufferedImage(dims, dims, BufferedImage.TYPE_INT_RGB);
			image.setRGB(0, 0, dims, dims, pixels, 0, dims);

			//Write image to file
			ImageIO.write(image, "png", output);

			//Display status
			JOptionPane.showMessageDialog(null, "Image created successfully!",
					"Creation Complete!", JOptionPane.INFORMATION_MESSAGE);
		}
		catch (IOException e)
		{
			//Display status
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Unable to write to file!", "Error!", JOptionPane.ERROR_MESSAGE);
		}
	}
}