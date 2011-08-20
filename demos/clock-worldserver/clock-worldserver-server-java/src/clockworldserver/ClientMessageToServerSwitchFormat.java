package clockworldserver;

import org.codemonkey.swiftsocketserver.ClientContext;
import org.codemonkey.swiftsocketserver.ClientMessageToServer;
import org.codemonkey.swiftsocketserver.UnknownMessageException;
import static clockworldserver.ClockWorldServer.TimeFormat;

public class ClientMessageToServerSwitchFormat extends ClientMessageToServer<ClockWorldServer>{

	public TimeFormat timeFormat;

	public ClientMessageToServerSwitchFormat(ClientContext clientContext) {
		super(clientContext);
	}
	
	@Override
	protected void decode(String messageContent) {
		if (messageContent.equals("12")) {
			timeFormat = TimeFormat.timeAMPM;
		} else if (messageContent.equals("24")) {
			timeFormat = TimeFormat.time24Hr;
		} else {
			throw new RuntimeException("invalid time format message, reason: didn't reconignize format " + messageContent);
		}
	}
	
	@Override
	public void execute(ClockWorldServer clockWorldServer)
			throws UnknownMessageException {
		clockWorldServer.setFormat(timeFormat);
	}
}