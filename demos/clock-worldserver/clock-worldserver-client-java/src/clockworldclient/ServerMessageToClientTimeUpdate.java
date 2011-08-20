package clockworldclient;

import org.codemonkey.swiftsocketclient.ServerMessageToClient;

public class ServerMessageToClientTimeUpdate implements ServerMessageToClient<ClockWorldClient> {

	private String currentTime;

	@Override
	public void decode(String messageContent) {
		this.currentTime = messageContent;
	}

	@Override
	public void execute(ClockWorldClient clockWorldClient) {
		clockWorldClient.updateTime(currentTime);
	}
}