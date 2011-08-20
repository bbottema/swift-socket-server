package clockworldclient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public abstract class ClockWindow {

	protected JFrame frame;
	protected JLabel time;

	private JButton btnHalfFPS;
	private JButton btnOneFPS;
	private JButton btnTwoFPS;
	private JButton btnFourFPS;
	private JButton btnSwitchFormat;

	/**
	 * Create the application.
	 */
	public ClockWindow() {
		frame = new JFrame();
		frame.setTitle("- Clock");
		frame.setSize(320, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 15));
		setupSwingComponents();
		setupEventHandlers();
		centerWindow();
		frame.setVisible(true);
	}

	private void setupSwingComponents() {
		// FPS buttons
		btnHalfFPS = new JButton();
		btnHalfFPS.setText(".5 FPS");
		btnOneFPS = new JButton();
		btnOneFPS.setText("1 FPS");
		btnTwoFPS = new JButton();
		btnTwoFPS.setText("2 FPS");
		btnFourFPS = new JButton();
		btnFourFPS.setText("4 FPS");

		// digital clock label
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		time = new JLabel("00:00:00", SwingConstants.CENTER);
		time.setFont(new Font("SansSerif", Font.BOLD, 36));
		time.setForeground(Color.BLUE);
		time.setLocation(100, 100);
		panel.add(time);

		frame.getContentPane().add(btnHalfFPS);
		frame.getContentPane().add(btnOneFPS);
		frame.getContentPane().add(btnTwoFPS);
		frame.getContentPane().add(btnFourFPS);
		frame.getContentPane().add(panel);
		
				// switch format button
				btnSwitchFormat = new JButton();
				btnSwitchFormat.setText("switch 12/24 format");
				frame.getContentPane().add(btnSwitchFormat);
	}

	private void setupEventHandlers() {
		btnHalfFPS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setFPS(.5);
			}
		});
		btnOneFPS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setFPS(1);
			}
		});
		btnTwoFPS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setFPS(2);
			}
		});
		btnFourFPS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setFPS(4);
			}
		});
		btnSwitchFormat.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				switchTimeFormat();
			}
		});
	}

	private void centerWindow() {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = dim.width / 2 - frame.getSize().width / 2;
		int y = dim.height / 2 - frame.getSize().height / 2;
		frame.setLocation(x, y);
	}

	protected abstract void setFPS(double fps);

	protected abstract void switchTimeFormat();
}