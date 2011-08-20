package org.codemonkey.util;

import org.codemonkey.swiftsocketserver.ClientMessageToServer;

/**
 * Marks an object executable. Used on conjunction with {@link ClientMessageToServer} to handle messages in two phases:
 * <ol>
 * <li>decode raw message</li>
 * <li>execute decoded message</li>
 * </ol>
 * 
 * @author Benny Bottema
 * @param <Controller> The context object provided to the implementing class to delegate to.
 * @param <ExecutionException> <code>RuntimeException</code> type that can optionally be thrown by the implementing class.
 * @see ClientMessageToServer
 * @since 1.0
 */
public interface Executable<Controller, ExecutionException extends RuntimeException> {
	/**
	 * Executes the object like a 'command' object making it interact with the provided controller argument.
	 * 
	 * @param controller A specific class against which the executable implementation is executed against, allowing the object to interact
	 *            with a known model.
	 * @throws ExecutionException May be thrown by the implementing class for whatever purpose it deems necessary.
	 */
	void execute(Controller controller)
			throws ExecutionException;
}