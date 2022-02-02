package rocks.shumyk.data.sharing;

import lombok.Data;
import lombok.Getter;
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

	@Data
	public static class DecrementingThread extends Thread {
		private final InventoryCounter counter;

		@Override
		public void run() {
			for (int i = 0; i < 10000; i++) {
				counter.decrement();
			}
		}
	}

	@Data
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
		@Getter private int items = 0;

		public void increment() {
			items++;
		}

		public void decrement() {
			items--;
		}
	}
}
