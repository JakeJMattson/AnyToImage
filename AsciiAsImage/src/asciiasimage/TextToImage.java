package asciiasimage;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * Create images from text files by converting ASCII values to RGB values
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
	public static void convert(File[] inputFiles, File outputFile, char unitSeparator)
	{
		//Holds all required text
		StringBuffer text = new StringBuffer();

		for (File inputFile : inputFiles)
		{
			//Build text from file
			text.append(inputFile.getName() + unitSeparator);
			text.append(readFile(inputFile) + unitSeparator);
		}

		//Assign text to pixel values
		int[] pixels = createPixels(text.toString());

		//Calculate image size (Quick and dirty pack - leaves "empty" pixels)
		int dims = (int) Math.ceil(Math.sqrt(pixels.length));

		//Store pixel values in image
		BufferedImage image = createImage(dims, dims, pixels);

		//Save image to output file
		saveImage(image, outputFile);
	}

	/**
	 * Read all text from the file.
	 *
	 * @param inputFile
	 *            File to the read text from
	 * @return File text
	 */
	private static String readFile(File inputFile)
	{
		String newline = System.getProperty("line.separator");
		String fileText = "";

		try
		{
			//Read all lines from the file
			List<String> lines = Files.readAllLines(Paths.get(inputFile.getAbsolutePath()), StandardCharsets.UTF_8);

			StringBuffer buffer = new StringBuffer();

			for (String line : lines)
				buffer.append(line + newline);

			//Remove final newline from buffer
			fileText = buffer.substring(0, buffer.length() - newline.length());
		}
		catch (IOException e)
		{
			//Error text
			fileText = "Could not read data from the original file!";

			//Display status
			String errorMessage = "Unable to read file: " + inputFile + newline
					+ "It will be omitted from the final result";
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, errorMessage, "Error!", JOptionPane.ERROR_MESSAGE);
		}

		return fileText;
	}

	/**
	 * Create the pixels that will make up the image.
	 *
	 * @param text
	 *            Text from the file to be converted
	 * @return Array of pixel data
	 */
	private static int[] createPixels(String text)
	{
		//Number of image channels (RGB assumed)
		int numOfChannels = 3;

		//Total number of characters to convert
		int charCount = text.length();

		//Determine if all pixels will be fully populated
		boolean doesOverflow = charCount % numOfChannels != 0;

		//Determine total number of pixels
		int[] pixels = new int[charCount / numOfChannels + (doesOverflow ? 1 : 0)];

		//Read text in groups of [channel count]
		for (int i = 0; i < charCount; i += numOfChannels)
		{
			//Array of current pixel info
			int[] pixel = new int[numOfChannels];

			//Read info from group into each channel
			for (int j = 0; j < numOfChannels; j++)
				pixel[j] = i + j < charCount ? text.charAt(i + j) : 0;

			//Store current pixel into pixel array
			pixels[i / numOfChannels] = pixel[0] << 16 | pixel[1] << 8 | pixel[2];
		}

		return pixels;
	}

	/**
	 * Create the output image with the pixel data.
	 *
	 * @param width
	 *            Width of the output image
	 * @param height
	 *            Height of the output image
	 * @param pixels
	 *            Array of pixel data
	 * @return The created image
	 */
	private static BufferedImage createImage(int width, int height, int[] pixels)
	{
		//Create empty image
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		//Create counter to navigate array
		int pixelIndex = 0;

		//Populate image with pixel info from array
		outerloop: for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				if (pixelIndex < pixels.length)
					image.setRGB(j, i, pixels[pixelIndex++]);
				else
					break outerloop;

		return image;
	}

	/**
	 * Save the image to the disk in the output location.
	 *
	 * @param image
	 *            The output image to be saved
	 * @param output
	 *            The desired file location of the output image
	 */
	private static void saveImage(BufferedImage image, File output)
	{
		try
		{
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