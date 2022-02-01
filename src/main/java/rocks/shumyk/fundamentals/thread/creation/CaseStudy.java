package rocks.shumyk.fundamentals.thread.creation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

import static java.util.Arrays.asList;

@Slf4j
public class CaseStudy {

	public static final int MAX_PASSWORD = 9999;

	public static void main(String[] args) {
		final Random random = new Random();

		final int password = random.nextInt(MAX_PASSWORD);
		log.info("Random password: {}", password);
		final Vault vault = new Vault(password);

		asList(new AscendingHackerThread(vault), new DescendingHackerThread(vault), new PoliceThread())
			.forEach(Thread::start);
	}

	@Data
	@AllArgsConstructor
	private static class Vault {
		private int password;

		public boolean isCorrectPassword(int guess) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				log.error("Exception occurred during sleep: {}", e.getMessage(), e);
			}
			return this.password == guess;
		}
	}

	private abstract static class HackerThread extends Thread {
		protected Vault vault;

		public HackerThread(Vault vault) {
			this.vault = vault;
			this.setName(this.getClass().getSimpleName());
			this.setPriority(Thread.MAX_PRIORITY);
		}

		@Override
		public synchronized void start() {
			log.info("Starting thread {}", this.getName());
			super.start();
		}

		protected void passwordCracked(int guess) {
			log.info("{} guessed the password {}", this.getName(), guess);
			System.exit(0);
		}
	}

	private static class AscendingHackerThread extends HackerThread {
		public AscendingHackerThread(Vault vault) {
			super(vault);
		}

		@Override
		public void run() {
			for (int guess = 0; guess < MAX_PASSWORD; guess++) {
				if (vault.isCorrectPassword(guess)) {
					this.passwordCracked(guess);
				}
			}
		}
	}

	private static class DescendingHackerThread extends HackerThread {
		public DescendingHackerThread(Vault vault) {
			super(vault);
		}

		@Override
		public void run() {
			for (int guess = MAX_PASSWORD; guess >= 0; guess--) {
				if (vault.isCorrectPassword(guess)) {
					this.passwordCracked(guess);
				}
			}
		}
	}

	private static class PoliceThread extends Thread {
		@Override
		public synchronized void start() {
			log.info("Starting thread {}", this.getName());
			super.start();
		}

		@Override
		public void run() {
			for (int i = 20; i > 0; i--) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.error("Exception occurred during sleep: {}", e.getMessage(), e);
				}
				log.info("Police iterate over {}", i);
			}
			log.info("Game over for you hackers");
			System.exit(0);
		}
	}
}
