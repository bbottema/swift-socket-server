package clockworldclient;

import org.codemonkey.swiftsocketclient.ClientType;
import org.codemonkey.swiftworldclient.WorldClient;
import org.codemonkey.swiftworldclient.WorldContext;

public class ClockWorldClient extends ClockWindow implements WorldContext {

	public static enum TimeFormat {
		timeAMPM, time24Hr;
	}

	private WorldClient<ClockWorldClient> client;
	private TimeFormat currentTimeFormat;

	public ClockWorldClient() {
		// setting up server client
		System.out.println("-----------------------");
		System.out.println("- Starting ChatClient -");
		System.out.println("-----------------------");
		client = new WorldClient<ClockWorldClient>("localhost", 4444, this, ClientType.UDP);
		client.registerClientMessageToServerType(1, ClientMessageToServerSwitchFormat.class);
		client.registerClientMessageToServerType(3, ClientMessageToServerSetFps.class);
		client.registerServerMessageToClientId(1, ServerMessageToClientTimeUpdate.class);
		client.start();
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		new ClockWorldClient();
	}

	@Override
	public void initWorld() {
		currentTimeFormat = TimeFormat.time24Hr;
	}

	private static final String[] tiks = { "--", "\\", "|", "/" };
	private int tik = 0;

	/**
	 * Called by {@link ServerMessageToClientTimeUpdate#execute(ClockWorldClient)}.
	 */
	public void updateTime(String currentTime) {
		frame.setTitle(tiks[tik = (tik + 1) % tiks.length]);
		time.setText(currentTime);
	}

	@Override
	protected void setFPS(double fps) {
		client.sendMessage(new ClientMessageToServerSetFps(fps));
	}

	@Override
	protected void switchTimeFormat() {
		if (currentTimeFormat == TimeFormat.time24Hr) {
			currentTimeFormat = TimeFormat.timeAMPM;
		} else {
			currentTimeFormat = TimeFormat.time24Hr;
		}
		client.sendMessage(new ClientMessageToServerSwitchFormat(currentTimeFormat));
	}
}