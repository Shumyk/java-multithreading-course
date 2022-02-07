package rocks.shumyk.inter.thread.communication;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.StringJoiner;

public class MatricesGenerator {

	private static final String OUTPUT_FILE = "./out/matrices";
	private static final int N = 10;
	private static final int NUMBER_OF_MATRIX_PAIRS = 100_000;

	public static void main(String[] args) throws IOException {
		final var file = new File(OUTPUT_FILE);
		final var fileWriter = new FileWriter(file);

		createMatrices(fileWriter);

		fileWriter.flush();
		fileWriter.close();
	}

	private static void createMatrices(FileWriter fileWriter) throws IOException {
		final var random = new Random();
		for (int i = 0; i < NUMBER_OF_MATRIX_PAIRS * 2; i++) {
			final float[][] matrix = createMatrix(random);
			saveMatrixToFile(fileWriter, matrix);
		}
	}

	private static float[][] createMatrix(Random random) {
		final float[][] matrix = new float[N][N];
		for (int i = 0; i < N; i++) {
			matrix[i] = createRow(random);
		}
		return matrix;
	}

	private static float[] createRow(Random random) {
		final float[] row = new float[N];
		for (int i = 0; i < N; i++) {
			row[i] = random.nextFloat() * random.nextInt(100);
		}
		return row;
	}

	private static void saveMatrixToFile(FileWriter writer, float[][] matrix) throws IOException {
		for (int r = 0; r < N; r++) {
			final var stringJoiner = new StringJoiner(", ");
			for (int c = 0; c < N; c++) {
				stringJoiner.add(String.format("%.2f", matrix[r][c]));
			}
			writer.write(stringJoiner.toString());
			writer.write('\n');
		}
		writer.write('\n');
	}
}
