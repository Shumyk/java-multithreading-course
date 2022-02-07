package rocks.shumyk.advanced.locking;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;

@Slf4j
public class ReentrantReadWriteLockInventoryDatabase {

	public static final int HIGHEST_PRICE = 1000;

	public static void main(String[] args) throws InterruptedException {
		final InventoryDatabase inventoryDatabase = new InventoryDatabase();

		final Random random = new Random();
		IntStream.range(0, 100000)
			.forEach(i -> inventoryDatabase.addItem(random.nextInt(HIGHEST_PRICE)));

		final Thread writer = new Thread(() -> {
			while (true) {
				inventoryDatabase.addItem(random.nextInt(HIGHEST_PRICE));
				inventoryDatabase.removeItem(random.nextInt(HIGHEST_PRICE));

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
		});
		writer.setDaemon(true);
		writer.start();

		final int numberOfReaderThreads = 7;
		final List<Thread> readers = new ArrayList<>();
		for (int readerIndex = 0; readerIndex < numberOfReaderThreads; readerIndex++) {
			final Thread reader = new Thread(() -> {
				IntStream.range(0, 100000)
					.forEach(i -> {
						final int upperBound = random.nextInt(HIGHEST_PRICE);
						final int lowerBound = upperBound > 0 ? random.nextInt(upperBound) : 0;
						inventoryDatabase.getNumberOfItemsInPriceRange(lowerBound, upperBound);
					});
			});
			reader.setDaemon(true);
			readers.add(reader);
		}

		final long startReadingTime = System.currentTimeMillis();

		readers.forEach(Thread::start);
		for (Thread reader : readers) {
			reader.join();
		}

		final long endReadingTime = System.currentTimeMillis();
		log.info("Reading took {} ms", endReadingTime - startReadingTime);
	}

	public static class InventoryDatabase {
		private final TreeMap<Integer, Integer> priceToCountMap = new TreeMap<>();
		private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

		public int getNumberOfItemsInPriceRange(int lowerBound, int upperBound) {
			lock.readLock().lock();
			try {
				final Integer fromKey = priceToCountMap.ceilingKey(lowerBound);
				final Integer toKey = priceToCountMap.floorKey(upperBound);

				if (isNull(fromKey) || isNull(toKey)) {
					return 0;
				}

				return priceToCountMap.subMap(fromKey, true, toKey, true)
					.values()
					.stream()
					.mapToInt(Integer::intValue)
					.sum();
			} finally {
				lock.readLock().unlock();
			}
		}

		public void addItem(int price) {
			lock.writeLock().lock();
			try {
				priceToCountMap.merge(price, 1, Integer::sum);
			} finally {
				lock.writeLock().unlock();
			}
		}

		public void removeItem(int price) {
			lock.writeLock().lock();
			try {
				final Integer numberOfItemsForPrice = priceToCountMap.get(price);
				if (isNull(numberOfItemsForPrice) || numberOfItemsForPrice == 1) {
					priceToCountMap.remove(price);
				} else {
					priceToCountMap.put(price, numberOfItemsForPrice - 1);
				}
			} finally {
				lock.writeLock().unlock();
			}
		}
	}
}
