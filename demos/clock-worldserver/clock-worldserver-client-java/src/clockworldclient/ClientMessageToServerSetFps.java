package clockworldclient;

import org.codemonkey.swiftsocketclient.ClientMessageToServer;

/**
 * Message that tells the World Server to adopt a new fps.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
class ClientMessageToServerSetFps extends ClientMessageToServer {

	private double framesPerSecond;

	public ClientMessageToServerSetFps(double fps) {
		this.framesPerSecond = fps;
	}

	@Override
	public String encode() {
		return String.valueOf(framesPerSecond);
	}
}