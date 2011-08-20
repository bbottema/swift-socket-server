package org.codemonkey.swiftworldclient;

/**
 * Client messages are executed against a <code>WorldContext</code> implementation allowing those messages to invoke world specific methods
 * (ie. <code>mySpaceGame.buyShip(id)</code>).
 * <p>
 * In addition some hooks are available to the {@link WorldClient}, to interact with the current <code>WorldContext</code> implementation
 * (such as {@link #initWorld()}).
 * 
 * @author Benny Bottema
 * @since 1.0
 */
public interface WorldContext {

	/**
	 * Called when the server is being started.
	 * 
	 * @see WorldClient#start()
	 */
	void initWorld();
}