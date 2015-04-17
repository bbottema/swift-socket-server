## About the client-server handshake ##

Unlike in TCP, when running in UDP mode, a client needs to make itself visible explicitly by sending a message to the server. It also needs a message back to get to confirm the message was received by the server successfully.

To accomplish this, the client and server internally perform a simple 'handshake' message exchange. This is a built in mechanism when running in UDP. Internally, the content of the messages are `CONNECT` and `ACK`. Any messages received from the server before the handshake acknowledgment are buffered and executed sequentially once it is received.