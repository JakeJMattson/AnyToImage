import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.List;

import javax.imageio.ImageIO;

public class TextToImage
{
	private static final String DEFAULT_INPUT = "src/TextToImage.java";
	private static final String DEFAULT_OUTPUT = "./Hello World.png";
	private static String inputFilePath;
	private static String outputFilePath;

	public static void main(String[] args)
	{
		//Allow command line args
		if (args.length >= 1)
			inputFilePath = args[0];
		else
			inputFilePath = DEFAULT_INPUT;

		if (args.length >= 2)
			outputFilePath = args[1];
		else
			outputFilePath = DEFAULT_OUTPUT;

		//Start program
		TextToImage driver = new TextToImage();
		driver.start();
	}

	private void start()
	{
		//Text group separation character
		char unitSeparator = (char) 31;

		//File to read text from
		File input = new File(inputFilePath);

		//File to save final image
		File output = new File(outputFilePath);

		//Get text from file
		String fileName = input.getName();
		String fileText = readFile(input.getAbsolutePath());

		//Combine text and separators
		String text = "";
		text += fileName + unitSeparator;
		text += fileText + unitSeparator;

		//Assign text to pixel values
		int[] pixels = createPixels(text);

		//Calculate image size (Quick and dirty pack - leaves unused pixels)
		int dims = (int) Math.ceil(Math.sqrt(pixels.length));

		//Store pixel values in image
		BufferedImage image = createImage(dims, dims, pixels);

		//Save image to output file
		saveImage(image, output);
	}

	private String readFile(String fileName)
	{
		//Read file data
		List<String> lines = null;
		try
		{
			lines = Files.readAllLines(Paths.get(fileName));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

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
		outerloop:
		for (int i = 0; i < height; i++)
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
			ImageIO.write(image, "png", output);
			System.out.println(">>> File Created Successfully <<<");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}