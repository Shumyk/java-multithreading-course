package rocks.shumyk.fundamentals.thread.coordination;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;


@Slf4j
public class ComplexCalculationExercise {

	public BigInteger calculateResult(BigInteger base1, BigInteger power1, BigInteger base2, BigInteger power2) throws InterruptedException {
		BigInteger result;
        /*
            Calculate result = ( base1 ^ power1 ) + (base2 ^ power2).
            Where each calculation in (..) is calculated on a different thread
        */
		final var threads = Arrays.asList(
			new PowerCalculatingThread(base1, power1),
			new PowerCalculatingThread(base2, power2)
		);
		threads.forEach(Thread::start);
		for (var t : threads) {
			t.join(2000);
		}
		final Optional<BigInteger> optionalResult = threads.stream()
			.map(PowerCalculatingThread::getResult)
			.reduce(BigInteger::add);
		if (optionalResult.isPresent()) {
			result = optionalResult.get();
		} else {
			log.error("Failed to get result for computations of two powers");
			result = BigInteger.ZERO;
		}

		return result;
	}

	private static class PowerCalculatingThread extends Thread {
		private BigInteger result = BigInteger.ONE;
		private final BigInteger base;
		private final BigInteger power;

		public PowerCalculatingThread(BigInteger base, BigInteger power) {
			this.base = base;
			this.power = power;
		}

		@Override
		public void run() {
			for (BigInteger i = BigInteger.ZERO; i.compareTo(this.power) != 0; i = i.add(BigInteger.ONE)) {
				result = result.multiply(base);
			}
		}

		public BigInteger getResult() { return result; }
	}
}
