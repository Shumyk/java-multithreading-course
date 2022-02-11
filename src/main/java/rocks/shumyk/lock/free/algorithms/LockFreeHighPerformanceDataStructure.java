package rocks.shumyk.lock.free.algorithms;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class LockFreeHighPerformanceDataStructure {

	public static void main(String[] args) throws InterruptedException {
		final Random random = new Random();
		measureStackPerformance(new StandardStack<>(), random);
		measureStackPerformance(new LockFreeStack<>(), random);
	}

	private static void measureStackPerformance(Stack<Integer> stack, Random random) throws InterruptedException {
		for (int i = 0; i < 100_000; i++) {
			stack.push(random.nextInt());
		}

		final List<Thread> threads = new ArrayList<>();

		int pushingThreads = 2;
		int poppingThreads = 2;

		for (int i = 0; i < pushingThreads; i++) {
			final Thread thread = new Thread(() -> {
				while(true) {
					stack.push(random.nextInt());
				}
			});
			thread.setDaemon(true);
			threads.add(thread);
		}

		for (int i = 0; i < poppingThreads; i++) {
			final Thread thread = new Thread(() -> {
				while(true) {
					stack.pop();
				}
			});
			thread.setDaemon(true);
			threads.add(thread);
		}

		threads.forEach(Thread::start);
		Thread.sleep(10_000);
		log.info("{} operations were performed in 10 seconds", String.format("%,d", stack.getCounter()));
	}

	public interface Stack<T> {
		void push(T value);
		T pop();
		int getCounter();
	}

	public static class LockFreeStack<T> implements Stack<T> {
		private final AtomicReference<StackNode<T>> head = new AtomicReference<>();
		private final AtomicInteger counter = new AtomicInteger(0);

		public void push(T value) {
			StackNode<T> newHeadNode = new StackNode<>(value);

			while (true) {
				final StackNode<T> currentHeadNode = head.get();
				newHeadNode.next = currentHeadNode;
				if (head.compareAndSet(currentHeadNode, newHeadNode)) {
					break;
				} else {
					LockSupport.parkNanos(1);
				}
			}
			counter.incrementAndGet();
		}

		public T pop() {
			StackNode<T> currentHeadNode = head.get();
			StackNode<T> newHeadNode;

			while (nonNull(currentHeadNode)) {
				newHeadNode = currentHeadNode.next;
				if (head.compareAndSet(currentHeadNode, newHeadNode)) {
					break;
				} else {
					LockSupport.parkNanos(1);
					currentHeadNode = head.get();
				}
			}
			counter.incrementAndGet();
			return isNull(currentHeadNode) ? null : currentHeadNode.value;
		}

		public int getCounter() {
			return counter.get();
		}
	}

	public static class StandardStack<T> implements Stack<T> {
		private StackNode<T> head;
		private int counter = 0;

		public synchronized void push(T value) {
			final StackNode<T> newHead = new StackNode<>(value);
			newHead.next = head;
			head = newHead;
			counter++;
		}

		public synchronized T pop() {
			if (isNull(head)) {
				counter++;
				return null;
			}

			T value = head.value;
			head = head.next;
			counter++;
			return value;
		}

		public int getCounter() {
			return counter;
		}
	}

	private static class StackNode<T> {
		public T value;
		public StackNode<T> next;

		public StackNode(T value) {
			this.value = value;
			this.next = next;
		}
	}
}
