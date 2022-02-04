package rocks.shumyk.concurrency.challenges;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class Deadlocks {

	public static void main(String[] args) {
		final var intersection = new Intersection();
		final var trainA = new Thread(new TrainA(intersection));
		final var trainB = new Thread(new TrainB(intersection));

		trainA.start();
		trainB.start();
	}

	@RequiredArgsConstructor
	public static class TrainA implements Runnable {
		private final Intersection intersection;
		private final Random random = new Random();

		@Override
		public void run() {
			while (true) {
				final int sleepingTime = random.nextInt(5);
				try {
					Thread.sleep(sleepingTime);
				} catch (InterruptedException e) {
					log.info("Unexpected exception during sleep {}", e.getMessage(), e);
				}
				intersection.takeRoadA();
			}
		}
	}

	@RequiredArgsConstructor
	public static class TrainB implements Runnable {
		private final Intersection intersection;
		private final Random random = new Random();

		@Override
		public void run() {
			while (true) {
				final int sleepingTime = random.nextInt(5);
				try {
					Thread.sleep(sleepingTime);
				} catch (InterruptedException e) {
					log.info("Unexpected exception during sleep {}", e.getMessage(), e);
				}
				intersection.takeRoadB();
			}
		}
	}

	public static class Intersection {
		private final Object roadA = new Object();
		private final Object roadB  = new Object();

		public void takeRoadA() {
			synchronized (roadA) {
				log.info("Road A is locked by thread [{}]", Thread.currentThread().getName());
				synchronized (roadB) {
					log.info("Train is passing through road A");
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						log.info("Unexpected exception during sleep {}", e.getMessage(), e);
					}
				}
			}
		}

		public void takeRoadB() {
			synchronized (roadA) {
				log.info("Road B is locked by thread {}", Thread.currentThread().getName());

				synchronized (roadB) {
					log.info("Train is passing through road B");
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						log.error("Unexpected exception during sleep {}", e.getMessage(), e);
					}
				}
			}
		}
	}
}
