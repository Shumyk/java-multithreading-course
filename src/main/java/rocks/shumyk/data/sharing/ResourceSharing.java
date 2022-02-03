package rocks.shumyk.data.sharing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResourceSharing {

	public static void main(String[] args) throws InterruptedException {
		final InventoryCounter counter = new InventoryCounter();
		final IncrementingThread incrementingThread = new IncrementingThread(counter);
		final DecrementingThread decrementingThread = new DecrementingThread(counter);

		incrementingThread.start();
		decrementingThread.start();

		incrementingThread.join();
		decrementingThread.join();

		log.info("We currently have {} items", counter.getItems());
	}

	@RequiredArgsConstructor
	public static class DecrementingThread extends Thread {
		private final InventoryCounter counter;

		@Override
		public void run() {
			for (int i = 0; i < 10000; i++) {
				counter.decrement();
			}
		}
	}

	@RequiredArgsConstructor
	public static class IncrementingThread extends Thread {
		private final InventoryCounter counter;

		@Override
		public void run() {
			for (int i = 0; i < 10000; i++) {
				counter.increment();
			}
		}
	}

	public static class InventoryCounter {
		private int items = 0;

		private final Object lock = new Object();

		public void increment() {
			synchronized (this.lock) {
				items++;
			}
		}

		public void decrement() {
			synchronized (this.lock) {
				items--;
			}
		}

		public int getItems() {
			synchronized (this.lock) {
				return items;
			}
		}
	}
}
