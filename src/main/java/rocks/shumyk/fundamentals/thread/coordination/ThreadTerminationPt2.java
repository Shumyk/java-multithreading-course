package rocks.shumyk.fundamentals.thread.coordination;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;

@Slf4j
public class ThreadTerminationPt2 {
	public static void main(String[] args) {
		final Thread thread = new Thread(new LongComputation(BigInteger.valueOf(20000L), BigInteger.valueOf(1000000L)));

		thread.start();
		thread.interrupt();
	}

	private static class LongComputation implements Runnable {

		private final BigInteger base;
		private final BigInteger power;

		private LongComputation(BigInteger base, BigInteger power) {
			this.base = base;
			this.power = power;
		}

		@Override
		public void run() {
			log.info("{}^{}={}", base, power, pow(base, power));
		}

		private BigInteger pow(BigInteger base, BigInteger power) {
			BigInteger result = BigInteger.ONE;
			for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {
				if (Thread.currentThread().isInterrupted()) {
					log.info("Prematurely interrupted computation");
					return BigInteger.ZERO;
				}
				result = result.multiply(base);
			}
			return result;
		}
	}
}
