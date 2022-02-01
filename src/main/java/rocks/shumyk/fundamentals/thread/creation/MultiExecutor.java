package rocks.shumyk.fundamentals.thread.creation;

import java.util.List;

public class MultiExecutor {

	private final List<Runnable> tasks;

	/*
	 * @param tasks to executed concurrently
	 */
	public MultiExecutor(List<Runnable> tasks) {
		this.tasks = tasks;
	}

	/**
	 * Starts and executes all the tasks concurrently
	 */
	public void executeAll() {
		tasks.stream()
			.map(Thread::new)
			.forEach(Thread::start);
	}
}
