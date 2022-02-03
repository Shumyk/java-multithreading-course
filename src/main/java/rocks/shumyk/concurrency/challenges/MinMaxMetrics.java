package rocks.shumyk.concurrency.challenges;

public class MinMaxMetrics {
	// Add all necessary member variables
	private volatile long min;
	private volatile long max;

	/**
	 * Initializes all member variables
	 */
	public MinMaxMetrics() {
		// Add code here
		min = Long.MAX_VALUE;
		max = Long.MIN_VALUE;
	}

	/**
	 * Adds a new sample to our metrics.
	 */
	public synchronized void addSample(long newSample) {
		// Add code here
		this.min = Math.min(newSample, this.min);
		this.max = Math.max(newSample, this.max);
	}

	/**
	 * Returns the smallest sample we've seen so far.
	 */
	public long getMin() {
		// Add code here
		return min;
	}

	/**
	 * Returns the biggest sample we've seen so far.
	 */
	public long getMax() {
		// Add code here
		return max;
	}
}
