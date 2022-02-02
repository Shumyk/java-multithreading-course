package rocks.shumyk.fundamentals.thread.coordination;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.List;

import static java.util.Arrays.asList;

@Slf4j
public class JoiningThreads {
	public static void main(String[] args) throws InterruptedException {
		final List<Long> inputNumbers = asList(10000000L, 3435L, 35435L, 2324L, 4656L, 23L, 5556L);

		final List<FactorialThread> threads = inputNumbers.stream()
			.map(FactorialThread::new)
			.toList();

		threads.forEach(t -> t.setDaemon(true));
		threads.forEach(Thread::start);

		for (FactorialThread thread : threads) {
			thread.join(2000);
		}

		for (int i = 0; i < inputNumbers.size(); i++) {
			final FactorialThread factorialThread = threads.get(i);
			if (factorialThread.isFinished) {
				log.info("Factorial of {} is {}", inputNumbers.get(i), factorialThread.getResult());
			} else {
				log.info("The calculation for {} is still in progress", inputNumbers.get(i));
			}
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class FactorialThread extends Thread {
		private final long inputNumber;
		private BigInteger result = BigInteger.ZERO;
		private boolean isFinished = false;

		@Override
		public void run() {
			this.result = factorial(inputNumber);
			this.isFinished = true;
		}

		private BigInteger factorial(long n) {
			BigInteger tempResult = BigInteger.ONE;
			for (long i = n; i > 0; i--) {
				tempResult = tempResult.multiply(BigInteger.valueOf(i));
			}
			return tempResult;
		}
	}
}
