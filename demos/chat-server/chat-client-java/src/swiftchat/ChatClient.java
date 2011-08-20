package swiftchat;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import org.codemonkey.swiftsocketclient.SwiftSocketClient;

/**
 * The GUI for this chat app is simply taken from the <a
 * href="http://ecologylab.cse.tamu.edu/research/simplGuide/oodss/tutorials/publicChatTutorial.html">oodss tutoria</a>
 * <p>
 * The only addition by me is creating a {@link SwiftSocketClient} and handling the 'Send Message' button interaction.
 * 
 * @author Benny Bottema
 */
public class ChatClient implements ActionListener {
	private JTextArea echoArea;
	private JFrame frame;
	private JTextField textField;

	private final SwiftSocketClient client;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatClient window = new ChatClient();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ChatClient() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setupSwingComponents();

		// setting up server client
		System.out.println("-----------------------");
		System.out.println("- Starting ChatClient -");
		System.out.println("-----------------------");
		client = new SwiftSocketClient("localhost", 4444);
		client.registerClientMessageToServerType(1, ClientToServerChatMessage.class);
		client.registerServerMessageToClientType(1, ServerToClientChatMessage.class);
		client.registerExecutionContext(ServerToClientChatMessage.class, this);
		client.start();
	}

	private void setupSwingComponents() {
		frame.getContentPane().setLayout(new BorderLayout());

		JScrollPane scrollPane = new JScrollPane();

		echoArea = new JTextArea();
		echoArea.setBorder(new EtchedBorder());
		echoArea.setEditable(false);

		scrollPane.setViewportView(echoArea);
		scrollPane.setAutoscrolls(true);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		JPanel entryPanel = new JPanel();
		entryPanel.setLayout(new BorderLayout());

		textField = new JTextField();
		JButton sendButton = new JButton("Send Message");
		sendButton.addActionListener(this);
		entryPanel.add(textField, BorderLayout.CENTER);
		entryPanel.add(sendButton, BorderLayout.EAST);
		textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendText();
				}
			}

		});

		frame.getContentPane().add(entryPanel, BorderLayout.SOUTH);
		frame.setTitle("Chat Client");
		frame.getContentPane().setSize(400, 400);
		frame.getContentPane().setVisible(true);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		sendText();
	}

	protected void sendText() {
		if (textField.getText() != null && !textField.getText().equals("")) {
			client.sendMessage(new ClientToServerChatMessage(textField.getText()));
			textField.setText("");
		}
	}

	public void updateDiscussion(String message) {
		echoArea.insert(message + "\n", 0);
	}
}