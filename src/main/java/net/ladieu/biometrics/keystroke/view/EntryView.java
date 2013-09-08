package net.ladieu.biometrics.keystroke.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.ladieu.biometrics.keystroke.model.StatisticalMatcher;

public class EntryView extends JFrame {

	private static final long serialVersionUID = -3997271663463873919L;
	private static final int MIN_USERNAME_LENGTH = 3;
	private JTextField usernameField;
	private JLabel status;

	public EntryView() {
		super();
		Box contentBox = new Box(BoxLayout.Y_AXIS);

		status = new JLabel("  ");
		contentBox.add(status);
		contentBox.add(createUsernameInput());

		Box buttonBox = new Box(BoxLayout.X_AXIS);

		buttonBox.add(createEnrollButton());
		buttonBox.add(createVerifyButton());

		contentBox.add(buttonBox);

		this.add(contentBox);

		this.setSize(400, 100);
		this.setVisible(true);
	}

	public static void main(String[] args) {

		new EntryView();
		// new CapturePasswordView().setVisible(true);

		// CaptureTemplateView view = new CaptureTemplateView("josh", "hello",
		// true);
		// view.setVisible(true);
		// view.displayVerifyAgainstTemplateView();
	}

	private JComponent createUsernameInput() {
		Box contentBox = new Box(BoxLayout.X_AXIS);
		contentBox.add(new JLabel("Username:"));
		usernameField = new JTextField(15);
		contentBox.add(usernameField);
		return contentBox;
	}

	private JButton createEnrollButton() {

		JButton passwordButton = new JButton();
		passwordButton.setText("Enroll");
		passwordButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {

				String enteredUsername = usernameField.getText();

				if (!FieldValidator.isValid(enteredUsername,
						MIN_USERNAME_LENGTH)) {
					status
							.setText("Username provided ["
									+ enteredUsername
									+ "] is not valid, it must consist of at least 3 alphanumeric characters.");
				} else {
					displayCapturePasswordView(enteredUsername);
				}
			}
		});

		return passwordButton;
	}

	private void displayCapturePasswordView(String username) {
		CapturePasswordView newView = new CapturePasswordView(username);
		this.setVisible(false);
		newView.setVisible(true);
	}

	private void displayVerifyAgainstTemplateView(String username) {

		StatisticalMatcher template;
		try {
			template = new StatisticalMatcher(username);
			VerifyAgainstTemplateView newView = new VerifyAgainstTemplateView(
					template);
			this.setVisible(false);
			newView.setVisible(true);
		} catch (IOException e) {
			status.setText("Username [" + username
					+ "] has not been enrolled yet.");
		}
	}

	private JButton createVerifyButton() {
		JButton verifyButton = new JButton();
		verifyButton.setText("Verify");
		verifyButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {

				String enteredUsername = usernameField.getText();

				if (!FieldValidator.isValid(enteredUsername,
						MIN_USERNAME_LENGTH)) {
					status
							.setText("Username provided ["
									+ enteredUsername
									+ "] is not valid, it must consist of at least 3 alphanumeric characters.");
				} else {
					displayVerifyAgainstTemplateView(enteredUsername);
				}
			}
		});

		return verifyButton;
	}

}
