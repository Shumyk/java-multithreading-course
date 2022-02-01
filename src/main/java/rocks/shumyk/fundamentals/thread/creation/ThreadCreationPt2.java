package rocks.shumyk.fundamentals.thread.creation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadCreationPt2 {
	public static void main(String[] args) {
		final Thread thread = new NewThread();
		thread.start();
	}

	private static class NewThread extends Thread {
		@Override
		public void run() {
			// code that executes on the new thread
			log.info("Hello from {}", this.getName());
		}
	}
}
