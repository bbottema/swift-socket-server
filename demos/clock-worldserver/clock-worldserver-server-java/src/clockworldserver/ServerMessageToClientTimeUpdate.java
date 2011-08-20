package clockworldserver;

import org.codemonkey.swiftsocketserver.ClientContext;
import org.codemonkey.swiftsocketserver.ServerMessageToClient;

public class ServerMessageToClientTimeUpdate extends ServerMessageToClient {

	private String currentTime;

	public ServerMessageToClientTimeUpdate(ClientContext clientContext, String currentTime) {
		super(clientContext);
		this.currentTime = currentTime;
	}

	@Override
	public String encode() {
		return currentTime;
	}
}