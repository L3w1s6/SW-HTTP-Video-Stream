package capSW;

import java.util.concurrent.locks.ReentrantLock;

// Thread-safe management of given type
public class ResourceSync<T> {
	private ReentrantLock lock = new ReentrantLock();
	private T resource;
	
	public ResourceSync() {
		resource = null;
	}

	public ResourceSync(T initial) {
		resource = initial;
	}
	
	// thread-safe get
	public T get() {
		lock.lock();
		try {			
			return resource;
		} finally {
			lock.unlock();
		}
	}
	
	// thread-safe set
	public void set(T value) {
		lock.lock();
		try {			
			resource = value;
		} finally {
			lock.unlock();
		}
	}
}