## Communicating easily converted binary packets ##

The Swift server is designed to easily configure message types to be sent and received.

The point is that you register a message type that uses a code and that you implement an interface that does the encoding/decoding (marshalling, serialization etc.). In fact the server initially was designed to communicate [binary packets](http://www.gpwiki.org/index.php/Binary_Packet#What_is_a_binary_packet.3F) to allow for absolute minimum required data that is platform independent. Since a _Java_ Client was added, support for automated standard serialization was added as well.

The form of a binary packet is as follows, where the first three digits form the message code and the rest of the message is up to the decoding implementation to interpret.

| Login example: | `[[001][3Sam7letmein]` |
|:---------------|:-----------------------|
| Ping Pong message (internal server message):| `[[999][]` |

See [Registering Message Types](RegisteringMessageTypes.md) for more information.