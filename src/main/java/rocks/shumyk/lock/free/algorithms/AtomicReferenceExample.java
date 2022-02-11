package rocks.shumyk.lock.free.algorithms;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class AtomicReferenceExample {

	public static void main(String[] args) {
		String oldName = "old name";
		String newName = "new name";
		AtomicReference<String> atomicReference = new AtomicReference<>(oldName);

		if (atomicReference.compareAndSet(oldName, newName)) {
			log.info("new value is: {}", atomicReference.get());
		} else {
			log.info("nothing changed");
		}
	}
}
