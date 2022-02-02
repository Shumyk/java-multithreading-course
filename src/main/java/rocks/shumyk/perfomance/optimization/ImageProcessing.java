package rocks.shumyk.perfomance.optimization;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ImageProcessing {

	public static final String SOURCE_FILE = "./resources/many-flowers.jpg";
	public static final String DESTINATION_FILE = "./out/many-flowers.jpg";

	public static void main(String[] args) throws IOException {
		BufferedImage originalImage = ImageIO.read(new File(SOURCE_FILE));
		BufferedImage resultImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

		processImageRecoloringSingleThreaded(originalImage, resultImage);
		processImageRecoloringMultiThreaded(originalImage, resultImage, 6);
	}

	private static void processImageRecoloringMultiThreaded(BufferedImage originalImage, BufferedImage resultImage, int threadsNumber) throws IOException {
		long startTime = System.currentTimeMillis();
		recolorMultiThreaded(originalImage, resultImage, threadsNumber);
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;

		File outputFile = new File(DESTINATION_FILE);
		ImageIO.write(resultImage, "jpg", outputFile);

		log.info("Duration of multi thread solution: {} ms", duration);
	}

	private static void processImageRecoloringSingleThreaded(BufferedImage originalImage, BufferedImage resultImage) throws IOException {
		long startTime = System.currentTimeMillis();
		recolorSingleThread(originalImage, resultImage);
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;

		File outputFile = new File(DESTINATION_FILE);
		ImageIO.write(resultImage, "jpg", outputFile);

		log.info("Duration of single thread solution: {} ms", duration);
	}

	public static void recolorMultiThreaded(BufferedImage original, BufferedImage result, int numberOfThreads) {
		int width = original.getWidth();
		int height = original.getHeight() / numberOfThreads;

		final List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < numberOfThreads; i++) {
			final var threadMultiplier = i;

			final var thread = new Thread(() -> {
				int leftCorner = 0;
				int topCorner = height * threadMultiplier;

				recolorImage(original, result, leftCorner, topCorner, width, height);
			});
			threads.add(thread);
		}
		threads.forEach(Thread::start);
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {}
		}
	}

	public static void recolorSingleThread(BufferedImage original, BufferedImage result) {
		recolorImage(original, result, 0, 0, original.getWidth(), original.getHeight());
	}

	public static void recolorImage(BufferedImage original, BufferedImage result, int leftCorner, int topCorner, int width, int height) {
		for (int x = leftCorner; x < leftCorner + width && x < original.getWidth(); x++) {
			for (int y = topCorner; y < topCorner + height && y < original.getHeight(); y++) {
				recolorPixel(original, result, x, y);
			}
		}
	}

	public static void recolorPixel(BufferedImage original, BufferedImage result,  int x, int y) {
		int rgb = original.getRGB(x, y);

		int red = getRed(rgb);
		int green = getGreen(rgb);
		int blue = getBlue(rgb);

		int newRed;
		int newGreen;
		int newBlue;

		if (isShadeOfGray(red, green, blue)) {
			newRed = Math.min(255, red + 10);
			newGreen = Math.max(0, green - 80);
			newBlue = Math.max(0, blue - 20);
		} else {
			newRed = red;
			newGreen = green;
			newBlue = blue;
		}

		int newRgb = createRgbFromColors(newRed, newGreen, newBlue);
		setRgb(result, x, y, newRgb);
	}

	private static void setRgb(BufferedImage image, int x, int y, int rgb) {
		image.getRaster().setDataElements(x, y, image.getColorModel().getDataElements(rgb, null));
	}

	public static boolean isShadeOfGray(int red, int green, int blue) {
		return Math.abs(red - green) < 30 &&
			Math.abs(red - blue) < 30 &&
			Math.abs(green - blue) < 30;
	}

	public static int createRgbFromColors(int red, int green, int blue) {
		int rgb = 0;

		rgb |= blue;
		rgb |= green << 8;
		rgb |= red << 16;

		rgb |= 0xFF000000;

		return rgb;
	}

	public static int getBlue(int rgb) {
		return rgb & 0x000000FF;
	}

	public static int getGreen(int rgb) {
		return (rgb & 0x0000FF00) >> 8;
	}

	public static int getRed(int rgb) {
		return (rgb & 0x00FF0000) >> 16;
	}
}
