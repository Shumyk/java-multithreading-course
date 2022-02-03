package rocks.shumyk.concurrency.challenges;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class AtomicOperations {

	public static void main(String[] args) {
		final Metrics metrics = new Metrics();

		final BusinessLogic businessLogicThread1 = new BusinessLogic(metrics);
		final BusinessLogic businessLogicThread2 = new BusinessLogic(metrics);
		final MetricsPrinter metricsPrinter = new MetricsPrinter(metrics);

		businessLogicThread1.start();
		businessLogicThread2.start();
		metricsPrinter.start();
	}

	@RequiredArgsConstructor
	public static class MetricsPrinter extends Thread {
		private final Metrics metrics;

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					log.error("Exception occurred during sleep: {}", e.getMessage(), e);
				}
				final double currentAverage = metrics.getAverage();
				log.info("Current Average is {}", currentAverage);
			}
		}
	}

	@RequiredArgsConstructor
	public static class BusinessLogic extends Thread {
		private final Metrics metrics;
		private final Random random = new Random();

		@Override
		public void run() {
			while (true) {
				final long start = System.currentTimeMillis();
				try {
					Thread.sleep(random.nextInt(10));
				} catch (InterruptedException e) {
					log.error("Unexpected error occurred during sleep: {}", e.getMessage(), e);
				}
				final long end = System.currentTimeMillis();
				metrics.addSample(end - start);
			}
		}
	}

	public static class Metrics {
		private long count = 0;
		private volatile double average = 0.0;

		public synchronized void addSample(long sample) {
			double currentSum = average * count;
			count++;
			average = (currentSum + sample) / count;
		}

		public double getAverage() {
			return average;
		}
	}
}
