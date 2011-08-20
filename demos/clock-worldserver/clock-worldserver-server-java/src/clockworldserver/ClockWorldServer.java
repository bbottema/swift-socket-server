package clockworldserver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.codemonkey.swiftsocketserver.ServerType;
import org.codemonkey.swiftworldserver.WorldContext;
import org.codemonkey.swiftworldserver.WorldServer;

public class ClockWorldServer implements WorldContext {

	public static void main(final String[] args) {
		System.out.println("------------------------------");
		System.out.println("- Starting Clock WorldServer -");
		System.out.println("------------------------------");

		new ClockWorldServer();
	}

	public static enum TimeFormat {
		timeAMPM("hh:mm:ss:a"), time24Hr("HH:mm:ss");
		public final String FORMATTING;

		private TimeFormat(String formatting) {
			this.FORMATTING = formatting;
		}
	}

	private WorldServer<ClockWorldServer> server;

	private TimeFormat timeFormat = TimeFormat.time24Hr;

	private Date currentTime;

	public ClockWorldServer() {
		int INITIAL_FPS = 1;
		server = new WorldServer<ClockWorldServer>(4444, this, INITIAL_FPS, ServerType.UDP);
		server.registerServerMessageToClientId(1, ServerMessageToClientTimeUpdate.class);
		server.registerClientMessageToServerType(1, ClientMessageToServerSwitchFormat.class);
		server.registerClientMessageToServerType(2, ClientMessageToServerSetFps.class);
		server.setPingPongMode(true);
		server.start();
	}

	@Override
	public void initWorld() {
		currentTime = Calendar.getInstance().getTime();
		System.out.println("initializing world with time: " + currentTime);
	}

	@Override
	public void updateWorld(double secondsPerFrame) {
		// we want to add 1 second to the clock per real second
		int secondIncrementInMillis = (int) (1000d * secondsPerFrame);
		currentTime = new Date(currentTime.getTime() + secondIncrementInMillis);

		// send a new update to all clients?
		String timeRepresentation = generateTimeRepresentation();
		server.broadcastMessage(new ServerMessageToClientTimeUpdate(null, timeRepresentation));
	}

	private String generateTimeRepresentation() {
		return new SimpleDateFormat(timeFormat.FORMATTING).format(currentTime);
	}

	/**
	 * Called by {@link ClientMessageToServerSwitchFormat#execute(ClockWorldServer)}.
	 */
	public void setFormat(TimeFormat timeFormat) {
		this.timeFormat = timeFormat;
	}

	/**
	 * Called by {@link ClientMessageToServerSetFps#execute(ClockWorldServer)}.
	 */
	public void setFramesPerSecond(double framesPerSecond) {
		server.setFramesPerSecond(framesPerSecond);
	}
}