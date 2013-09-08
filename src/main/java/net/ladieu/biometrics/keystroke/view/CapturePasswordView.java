package net.ladieu.biometrics.keystroke.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class CapturePasswordView extends JFrame {

	private static final long serialVersionUID = -5033454592773134015L;

	private static final int MIN_PW_LENGTH = 5;

	private JTextField passwordField;
	private JLabel status;

	private String username;

	public CapturePasswordView(String username) {
		super();

		this.username = username;
		this.setSize(400, 200);
		this.add(createContentBox());
		this.setVisible(true);
	}

	private Box createContentBox() {
		Box contentBox = new Box(BoxLayout.Y_AXIS);

		contentBox.add(createDirectionsPanel());

		JLabel status = new JLabel("   ");

		contentBox.add(createPasswordInput());
		contentBox.add(status);
		return contentBox;
	}

	private JTextPane createDirectionsPanel() {
		JTextPane directions = new JTextPane();
		directions
				.setText("Please enter a password at least 5 characters in length, consisting of numbers or letters.  Upper case values will be converted to lowercase for the purposes of template capture.");
		directions.setEditable(false);
		return directions;
	}

	private JComponent createPasswordInput() {
		Box contentBox = new Box(BoxLayout.X_AXIS);
		contentBox.add(new JLabel("Password:"));
		passwordField = new JTextField(15);
		contentBox.add(passwordField);
		contentBox.add(createPasswordButton());
		return contentBox;
	}

	private JButton createPasswordButton() {
		JButton passwordButton = new JButton();
		passwordButton.setText("Enroll with Password");
		passwordButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {

				String enteredPassword = passwordField.getText();

				if (!FieldValidator.isValid(enteredPassword, MIN_PW_LENGTH)) {
					status.setText("Password provided [" + enteredPassword
							+ "] is not valid.");
				} else {
					displayTemplateCaptureView(username, enteredPassword);
				}
			}
		});

		return passwordButton;
	}

	private void displayTemplateCaptureView(String username, String password) {
		this.setVisible(false);
		new CaptureTemplateView(username, password).setVisible(true);
	}
}
