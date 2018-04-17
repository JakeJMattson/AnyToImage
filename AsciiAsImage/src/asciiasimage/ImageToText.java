package asciiasimage;

import java.awt.Color;
import java.awt.image.*;
import java.io.*;

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
	public static void convert(File[] inputFiles, File outputDir, char unitSeparator)
	{
		for (File inputFile : inputFiles)
		{
			//Get pixels from file
			BufferedImage image = readImage(inputFile);
			int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

			//Build string from pixel info
			String rawText = extractText(pixels);

			//Split string by unit separator
			String[] fileInfo = rawText.split("" + unitSeparator);

			for (int i = 0; i < fileInfo.length - 1; i += 2)
			{
				//Construct arguments
				String fileName = outputDir + "/" + fileInfo[i];
				String fileText = fileInfo[i + 1];

				createFile(fileName, fileText);
			}
		}

		JOptionPane.showMessageDialog(null, "All valid files have been extracted from the image.",
				"Extraction Complete!", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Read an image from a file.
	 *
	 * @param file
	 *            File containing the image to be read
	 * @return Image from file
	 */
	private static BufferedImage readImage(File file)
	{
		BufferedImage image = null;

		try
		{
			//Read image from file
			BufferedImage fileImage = ImageIO.read(file);

			//Create a buffered image with the desired type
			image = new BufferedImage(fileImage.getWidth(), fileImage.getHeight(),
					BufferedImage.TYPE_INT_RGB);

			//Draw the image from the file into the buffer
			image.getGraphics().drawImage(fileImage, 0, 0, null);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return image;
	}

	/**
	 * Convert pixel information into text.
	 *
	 * @param pixels
	 *            Int array containing all pixels from the image
	 * @return String representation
	 */
	private static String extractText(int[] pixels)
	{
		StringBuffer buffer = new StringBuffer();

		for (int pixel : pixels)
		{
			//Create color object from pixel info
			Color color = new Color(pixel);

			//Read channels from color object
			buffer.append((char) color.getRed());
			buffer.append((char) color.getGreen());
			buffer.append((char) color.getBlue());
		}

		return buffer.toString();
	}

	/**
	 * Create a new file and write text to it.
	 *
	 * @param fileName
	 *            Name of text file to be created
	 * @param fileText
	 *            Text to be written to file
	 */
	private static void createFile(String fileName, String fileText)
	{
		try
		{
			//Write text to file
			Writer writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(fileText);
			writer.close();
		}
		catch (IOException e)
		{
			//Display status
			String errorMessage = "Unable to write to file! (" + fileName + ")";
			JOptionPane.showMessageDialog(null, errorMessage, "Error!", JOptionPane.ERROR_MESSAGE);
		}
	}
}