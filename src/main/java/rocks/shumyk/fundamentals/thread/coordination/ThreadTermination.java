package rocks.shumyk.fundamentals.thread.coordination;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadTermination {
	public static void main(String[] args) {
		final Thread thread = new Thread(new BlockingTask());

		thread.start();
		thread.interrupt();
	}

	private static class BlockingTask implements Runnable {
		@Override
		public void run() {
			// do things
			try {
				Thread.sleep(500000);
			} catch (InterruptedException e) {
				log.error("Exiting blocking thread");
			}
		}
	}
}
