package de.tum.in.i22.uc.cm.distribution;

/**
 * There will always be only one {@link LocalLocation}. Singleton.
 *
 * @author Florian Kelbert
 *
 */
public class LocalLocation extends Location {

	private static final String local = "local";

	private static LocalLocation _instance;

	/**
	 * Use {@link LocalLocation#getInstance()} to
	 * get the only instance.
	 */
	private LocalLocation() {
		super(ELocation.LOCAL);
	}

	public static LocalLocation getInstance() {
		/*
		 * This implementation may seem odd, overengineered, redundant, or all of it.
		 * Yet, it is the best way to implement a thread-safe singleton, cf.
		 * http://www.journaldev.com/171/thread-safety-in-java-singleton-classes-with-example-code
		 * -FK-
		 */
		if (_instance == null) {
			synchronized (LocalLocation.class) {
				if (_instance == null) _instance = new LocalLocation();
			}
		}
		return _instance;
	}


	@Override
	public String toString() {
		return local;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof LocalLocation;
	}

	@Override
	public int hashCode() {
		return this.getClass().hashCode();
	}

	@Override
	public String getName() {
		return PREFIX_LOCATION + local;
	}

	@Override
	public String asString() {
		return local;
	}
}
