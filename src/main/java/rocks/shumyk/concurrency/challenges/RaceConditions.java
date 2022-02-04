package rocks.shumyk.concurrency.challenges;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RaceConditions {

	public static void main(String[] args) {
		final var shared = new SharedClass();

		final var incrementThread = new Thread(() -> {
			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				shared.increment();
			}
		});
		final var checkThread = new Thread(() -> {
			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				shared.checkForDataRace();
			}
		});

		incrementThread.start();
		checkThread.start();
	}

	public static class SharedClass {
		private volatile int x = 0;
		private volatile int y = 0;

		public void increment() {
			x++;
			y++;
		}

		public void checkForDataRace() {
			if (y > x) {
				log.error("y [{}] > x [{}] - Data Race is detected", y, x);
			}
		}
	}
}
