package rocks.shumyk.fundamentals.thread.creation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadCreationPt1 {
	public static void main(String[] args) throws InterruptedException {
		// creating thread
		Thread thread = new Thread(() -> {
			// code that will run in a new thread
			log.info("We are in a new thread: [{}]", Thread.currentThread().getName());
			log.info("Current thread priority: {}", Thread.currentThread().getPriority());
			throw new RuntimeException("Intentional Exception");
		});
		thread.setName("New Worker Thread");
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.setUncaughtExceptionHandler((t, e) -> {
			log.error("A critical error happened in thread " + t.getName());
			log.error("The error is " + e.getMessage());
		});

		// executing
		log.info("We are in thread: [{}], before starting a new thread", Thread.currentThread().getName());
		thread.start();
		log.info("We are in thread: [{}], after starting a new thread", Thread.currentThread().getName());

		Thread.sleep(5_000);
	}
}
