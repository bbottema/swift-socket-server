package org.codemonkey.swiftsocketclient;

import org.apache.log4j.Logger;

/**
 * Helper class mainly to hide some boilerplate code concerning {@link Thread#sleep(long)}.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
public final class ServerUtil {
	private static final Logger LOGGER = Logger.getRootLogger();

	private ServerUtil() {
	}

	/**
	 * Makes the current Thread sleep any number of milliseconds.
	 * 
	 * @param milliseconds The number of milliseconds to make the thread sleep for.
	 * @see Thread#sleep(long)
	 */
	public static void sleep(final int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (final InterruptedException e) {
			assert false;
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Performs a {@link Thread#sleep(long)} of 10 milliseconds. Used to avoid using 100% of cpu.
	 */
	public static void defaultSleep() {
		sleep(10);
	}
}