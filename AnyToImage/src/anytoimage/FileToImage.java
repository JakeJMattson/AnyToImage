package anytoimage;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * Create images from files by converting bytes to RGB values.
 *
 * @author mattson543
 */
public class FileToImage
{
	/**
	 * Static method to initiate the conversion.
	 *
	 * @param inputFiles
	 *            Array of files to be converted
	 * @param outputFile
	 *            Image file to be output when conversion is complete
	 */
	public static void convert(File[] inputFiles, File outputFile)
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		for (File file : inputFiles)
			try
			{
				if (file.exists())
					if (file.isDirectory())
						directoryToBytes(stream, file);
					else if (file.isFile())
						fileToBytes(stream, file, file.getName());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		//Create pixels from file information
		int[] pixels = bytesToPixels(stream.toByteArray());

		//Create image from pixels
		createImage(pixels, outputFile);
	}

	/**
	 * Turn directory into bytes.
	 *
	 * @param stream
	 *            Byte stream currently open
	 * @param file
	 *            Directory to extract bytes from
	 * @throws IOException
	 *             Failed to traverse directory structure
	 */
	private static void directoryToBytes(ByteArrayOutputStream stream, File file) throws IOException
	{
		//Get selected directory
		String parentDir = file.getName();

		//Get all files from the directory and sub-directories
		List<Path> paths = Files.walk(file.toPath())
				.filter(Files::isRegularFile)
				.collect(Collectors.toList());

		for (Path path : paths)
		{
			//Construct
			File fileFromDir = path.toFile();
			String fullPath = fileFromDir.toString();
			String fileName = fullPath.substring(fullPath.indexOf(parentDir));

			//Retrieve bytes
			fileToBytes(stream, fileFromDir, fileName);
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
	 * @throws IOException
	 *             Failed to write byte array to stream
	 */
	private static void fileToBytes(ByteArrayOutputStream stream, File file, String fileName) throws IOException
	{
		//Acquire file information
		byte[] name = fileName.getBytes();
		byte[] nameLength = {(byte) name.length};
		byte[] data = Files.readAllBytes(file.toPath());
		byte[] dataLength = ByteUtils.intToBytes(data.length, 4);

		//Write information into the stream
		stream.write(nameLength);
		stream.write(name);
		stream.write(dataLength);
		stream.write(data);
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