package rocks.shumyk.inter.thread.communication;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.StringJoiner;

import static java.util.Objects.isNull;

@Slf4j
public class ObjectsAsConditionVariables {

	private static final int N = 10;

	private static final String INPUT_FILE = "./out/matrices";
	private static final String OUTPUT_FILE = "./out/matrices_results.txt";

	public static void main(String[] args) throws IOException {
		final ThreadSafeQueue queue = new ThreadSafeQueue();
		File inputFile = new File(INPUT_FILE);
		File outputFile = new File(OUTPUT_FILE);

		final MatricesReaderProducer matricesReader = new MatricesReaderProducer(new FileReader(inputFile), queue);
		final MatricesMultiplierConsumer matricesConsumer = new MatricesMultiplierConsumer(queue, new FileWriter(outputFile));

		matricesConsumer.start();
		matricesReader.start();
	}

	@RequiredArgsConstructor
	private static class MatricesMultiplierConsumer extends Thread {
		private final ThreadSafeQueue queue;
		private final FileWriter fileWriter;

		@Override
		public void run() {
			while (true) {
				final MatricesPair pair = queue.remove();
				if (isNull(pair)) {
					log.info("No more matrices to read from the queue, consumer is terminating");
					break;
				}

				float[][] result = multiplyMatrices(pair.matrix1, pair.matrix2);
				try {
					saveResultToFile(fileWriter, result);
				} catch (IOException e) {
				}
			}

			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				log.error("Unexpected exception during file write: {}", e.getMessage(), e);
			}
		}

		private float[][] multiplyMatrices(float[][] m1, float[][] m2) {
			float[][] result = new float[N][N];
			for (int r = 0; r < N; r++) {
				for (int c = 0; c < N; c++) {
					for (int k = 0; k < N; k++) {
						result[r][c] += m1[r][k] * m2[k][c];
					}
				}
			}
			return result;
		}

		private void saveResultToFile(FileWriter fileWriter, float[][] matrix) throws IOException {
			for (int r = 0; r < N; r++) {
				final StringJoiner stringJoiner = new StringJoiner(", ");
				for (int c = 0; c < N; c++) {
					stringJoiner.add(String.format("%.2f", matrix[r][c]));
				}
				fileWriter.write(stringJoiner.toString());
				fileWriter.write('\n');
			}
			fileWriter.write('\n');
		}
	}

	private static class MatricesReaderProducer extends Thread {
		private final Scanner scanner;
		private final ThreadSafeQueue queue;

		public MatricesReaderProducer(FileReader reader, ThreadSafeQueue queue) {
			this.scanner = new Scanner(reader);
			this.queue = queue;
		}

		@Override
		public void run() {
			while (true) {
				float[][] matrix1 = readMatrix();
				float[][] matrix2 = readMatrix();

				if (isNull(matrix1) || isNull(matrix2)) {
					queue.terminate();
					log.info("No more matrices to read. Producer Thread is terminating");
					return;
				}

				queue.add(new MatricesPair(matrix1, matrix2));
			}
		}

		private float[][] readMatrix() {
			float[][] matrix = new float[N][N];
			for (int r = 0; r < N; r++) {
				if (!scanner.hasNext()) {
					return null;
				}
				final String[] line = scanner.nextLine().split(", ");
				for (int c = 0; c < N; c++) {
					matrix[r][c] = Float.parseFloat(line[c]);
				}
			}
			scanner.nextLine();
			return matrix;
		}
	}

	private static class ThreadSafeQueue {
		private final Queue<MatricesPair> queue = new LinkedList<>();
		private boolean isEmpty = true;
		private boolean isTerminate = false;
		private static final int CAPACITY = 1000;

		public synchronized void add(MatricesPair pair) {
			while (queue.size() == CAPACITY) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}
			queue.add(pair);
			isEmpty = false;
			notify();
		}

		public synchronized MatricesPair remove() {
			MatricesPair matricesPair = null;
			while (isEmpty && !isTerminate) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}

			if (queue.size() == 1) {
				isEmpty = true;
			}

			if (queue.isEmpty() && isTerminate) {
				return null;
			}

			log.info("queue size {}", queue.size());

			matricesPair = queue.remove();
			if (queue.size() == CAPACITY - 1) {
				notifyAll();
			}
			return matricesPair;
		}

		public synchronized void terminate() {
			isTerminate = true;
			notifyAll();
		}
	}

	@AllArgsConstructor
	private static class MatricesPair {
		public float[][] matrix1;
		public float[][] matrix2;
	}
}
