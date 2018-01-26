import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class TextToImage
{
	private File[] inputFiles;
	private File outputFile;

	public TextToImage(File[] inputFiles, File outputFile)
	{
		this.inputFiles = inputFiles;
		this.outputFile = outputFile;
	}

	public void start()
	{
		//Text group separation character
		char unitSeparator = (char) 31;

		//Holds all required text
		String allText = "";

		for (int i = 0; i < inputFiles.length; i++)
		{
			//Get text from file
			String fileName = inputFiles[i].getName();
			List<String> lines = readFile(inputFiles[i].getAbsolutePath());
			if (lines != null)
			{
				String fileText = getFileText(lines);
	
				//Combine text and separators
				allText += fileName + unitSeparator;
				allText += fileText + unitSeparator;
			}
		}

		if (!allText.equals(""))
		{
			//Assign text to pixel values
			int[] pixels = createPixels(allText);
	
			//Calculate image size (Quick and dirty pack - leaves unused pixels)
			int dims = (int) Math.ceil(Math.sqrt(pixels.length));
	
			//Store pixel values in image
			BufferedImage image = createImage(dims, dims, pixels);
	
			//Save image to output file
			saveImage(image, outputFile);
		}
		else
			JOptionPane.showMessageDialog(null, "No valid files input", "Image not created", JOptionPane.ERROR_MESSAGE);
	}

	private List<String> readFile(String fileName)
	{
		//Read file data
		List<String> lines = null;
		try
		{
			lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			//Display status
			String errorMessage = "Unable to read file: " + fileName + "\n"
					+ "It will be omitted from the final result";
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, errorMessage, "Error!", JOptionPane.ERROR_MESSAGE);
		}

		return lines;
	}

	private String getFileText(List<String> lines)
	{
		//Add data to buffer
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < lines.size(); i++)
		{
			//Add line text to buffer
			buffer.append(lines.get(i));

			//Add newline character to buffer (except last line)
			if (i != lines.size() - 1)
				buffer.append(System.getProperty("line.separator"));
		}

		return buffer.toString();
	}

	private int[] createPixels(String text)
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
				if (i + j < charCount)
					pixel[j] = (int) text.charAt(i + j);
				else
					pixel[j] = 0;

			//Store current pixel into pixel array
			pixels[i / numOfChannels] = (pixel[0] << 16) | (pixel[1] << 8) | pixel[2];
		}

		return pixels;
	}

	private BufferedImage createImage(int width, int height, int[] pixels)
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

	private void saveImage(BufferedImage image, File output)
	{
		try
		{
			//Write image to file
			ImageIO.write(image, "png", output);

			//Display status
			String successMessage = "Image created successfully!";
			System.out.println(successMessage);
			JOptionPane.showMessageDialog(null, successMessage, "Success!", JOptionPane.INFORMATION_MESSAGE);
		}
		catch (IOException e)
		{
			//Display status
			String errorMessage = "Unable to write to file!";
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, errorMessage, "Error!", JOptionPane.ERROR_MESSAGE);
		}
	}
}