package clockworldserver;

import org.codemonkey.swiftsocketserver.ClientContext;
import org.codemonkey.swiftsocketserver.ClientMessageToServer;

/**
 * Message that tells the World Server to adopt a new fps.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
public class ClientMessageToServerSetFps extends ClientMessageToServer<ClockWorldServer> {

	public ClientMessageToServerSetFps(ClientContext clientContext) {
		super(clientContext);
	}

	private double framesPerSecond;

	@Override
	protected void decode(String datagramMessage) {
		this.framesPerSecond = Double.parseDouble(datagramMessage);
	}

	/**
	 * Calls {@link WorldServer#setFramesPerSecond(double)}.
	 */
	@Override
	public void execute(ClockWorldServer clockWorldServer) {
		clockWorldServer.setFramesPerSecond(framesPerSecond);
	}
}