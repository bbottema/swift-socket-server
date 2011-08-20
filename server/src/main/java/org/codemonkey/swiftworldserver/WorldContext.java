package org.codemonkey.swiftworldserver;

/**
 * Client messages are executed against a <code>WorldContext</code> implementation allowing those messages to invoke world specific methods
 * (ie. <code>mySpaceGame.buyShip(id)</code>).
 * <p>
 * In addition some hooks are available to the {@link WorldServer}, to interact with the current <code>WorldContext</code> implementation
 * (such as {@link #initWorld()}).
 * <p>
 * Perhaps the most important is the {@link #updateWorld(double)} method which is called based on a frame per second setting, to progress
 * the world simulation (see {@link WorldServer}).
 * 
 * @author Benny Bottema
 * @since 1.0
 */
public interface WorldContext {

	/**
	 * Called when the server is being started.
	 * 
	 * @see WorldServer#start()
	 */
	void initWorld();

	/**
	 * Called when an FPS cycle has been completed.
	 * 
	 * @param secondsPerFrame The scale factor that determines the size of the change (see {@link WorldServer}).
	 */
	void updateWorld(double secondsPerFrame);
}