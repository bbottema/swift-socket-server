package org.codemonkey.swiftsocketserver;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Whenever a client is connected or sending a message to the server, there will be a <code>ClientContext</code> associated with it.
 * <p>
 * Contains all session data concerning a given client. Also contains a session data container for external use only. External users can
 * utilize this container to store client related (session) data for the duration of the connection.
 * <p>
 * Only the client's id and the session data container is exposed publicly. The client id is determined during construction so that even
 * when the connection is lost, external users can still access the client's id without connection exceptions.
 * <p>
 * Since {@link #clientInetAddress} does not have to be unique (multiple clients connecting from one host), instances of this classes also
 * act as unique identifiers for client sockets so that the server can track multiple clients from the same host without exposing the Socket
 * instance itself. Doing so allows executable message objects to access a Client's session data (or send responses to a specific client)
 * without needing the Socket or InetAddress to identify the client.
 * <p>
 * This context keeps information about the client which may remain accessible even when the client has disconnected.
 * 
 * @author Benny Bottema
 * @since 1.0
 */
public class ClientContext {

	/**
	 * Client to which this context is dedicated to. Used to read from and write to.
	 */
	private final ClientEndpoint clientEndpoint;

	/**
	 * Client id to which context is dedicated to. Acquired from {@link #clientInetAddress}. Kept separate for when the socket has been
	 * closed already and the user still wants to know the original client id.
	 */
	private final InetAddress clientInetAddress;

	/**
	 * Flag that indicates whether the client has sent a 'Bye Bye' notification to gracefully close the connection, or when the connection
	 * was lost somehow.
	 */
	private boolean clientSaidByeBye;

	/**
	 * Time stamp of the last ping message to the client.
	 * 
	 * @see #pongReceived
	 * @see #isPongReceived()
	 */
	private long pingtime;

	/**
	 * Flag that indicates whether the client has sent a 'pong' notification.
	 * 
	 * @see #pingtime
	 * @see #isPongReceived()
	 */
	private boolean pongReceived;

	/**
	 * A session scoped data container for external use only. Users can access this container to store temporary data concerning the current
	 * client, until the connection is lost.
	 */
	private final Map<Object, Object> sessionData = new HashMap<Object, Object>();

	/**
	 * Stores the given client endpoint and saves its {@link InetAddress} for later use (in case the endpoint is being closed and we lose
	 * the ability the query for the {@link InetAddress}).
	 * <p />
	 * Furthermore initializes a timestamp to start measuring ping / pong timeouts.
	 * 
	 * @param clientEndpoint The client with which we have established a communication channel.
	 */
	public ClientContext(final ClientEndpoint clientEndpoint) {
		this.clientEndpoint = clientEndpoint;
		clientInetAddress = clientEndpoint.getInetAddress();
		pingtime = new Date().getTime();
		pongReceived = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.valueOf(clientEndpoint.getInetAddress());
	}

	/**
	 * @return {@link #clientEndpoint}.
	 */
	final ClientEndpoint getClientEndpoint() {
		return clientEndpoint;
	}

	/**
	 * @return {@link #clientInetAddress}
	 */
	public InetAddress getClientInetAddress() {
		return clientInetAddress;
	}

	/**
	 * @return {@link #sessionData}
	 */
	public Map<Object, Object> getSessionData() {
		return sessionData;
	}

	/**
	 * @return {@link #pingtime}
	 */
	final long getPingtime() {
		return pingtime;
	}

	final void setPingtime(final long pingtime) {
		this.pingtime = pingtime;
	}

	/**
	 * @return {@link #pongReceived}
	 */
	final boolean isPongReceived() {
		return pongReceived;
	}

	final void setPongReceived(final boolean pongReceived) {
		this.pongReceived = pongReceived;
	}

	/**
	 * @return {@link #clientSaidByeBye}
	 */
	final boolean isClientSaidByeBye() {
		return clientSaidByeBye;
	}

	/**
	 * Indicates whether the client has gone away or that the connection has been closed.
	 * 
	 * @return !{@link #clientSaidByeBye} && ! {@link ClientEndpoint#isClosed()}
	 */
	public final boolean isActive() {
		return !isClientSaidByeBye() && !clientEndpoint.isClosed();
	}

	/**
	 * Sets {@link #clientSaidByeBye}. In case of 'true', the socket should be closed as well in one atomic operation, to avoid reading out
	 * a closed socket while setting Bye Bye == true.
	 * 
	 * @param clientSaidByeBye Whether the connection has become obsolete, either because the client left gracefully or by connection error.
	 */
	final void setClientSaidByeBye(final boolean clientSaidByeBye) {
		this.clientSaidByeBye = clientSaidByeBye;
	}
}