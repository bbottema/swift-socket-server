package clockworldclient;

import org.codemonkey.swiftsocketclient.ClientMessageToServer;

import clockworldclient.ClockWorldClient.TimeFormat;

public class ClientMessageToServerSwitchFormat extends ClientMessageToServer {
	
	private TimeFormat timeFormat;

	public ClientMessageToServerSwitchFormat(TimeFormat timeFormat) {
		this.timeFormat = timeFormat;
	}

	@Override
	public String encode() {
		if (timeFormat == TimeFormat.timeAMPM) {
			return "12";
		} else {
			return "24";
		}
	}
}