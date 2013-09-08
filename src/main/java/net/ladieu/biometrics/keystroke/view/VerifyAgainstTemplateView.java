package net.ladieu.biometrics.keystroke.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.ladieu.biometrics.keystroke.controller.KeystrokeDirector;
import net.ladieu.biometrics.keystroke.controller.KeystrokeDirectorObserver;
import net.ladieu.biometrics.keystroke.controller.KeystrokeNotification;
import net.ladieu.biometrics.keystroke.controller.KeystrokeNotification.KeystrokeNotificationType;
import net.ladieu.biometrics.keystroke.model.KeystrokeMatcher;
import net.ladieu.biometrics.keystroke.model.KeystrokeRecorder;
import net.ladieu.biometrics.keystroke.model.KeystrokeRecorderImpl;
import net.ladieu.biometrics.keystroke.model.KeystrokeSequence;
import net.ladieu.biometrics.keystroke.model.LowerCaseKeystrokeFactory;
import net.ladieu.biometrics.keystroke.model.StatisticalMatcher;

public class VerifyAgainstTemplateView extends JFrame implements
		KeystrokeDirectorObserver {

	private static final long serialVersionUID = -1287644941410094912L;

	private StatisticalMatcher template;
	private JTextArea status;
	private JTextField inputField;

	private KeystrokeRecorder recorder;
	private KeystrokeDirector director;

	private boolean corruptionDetected;

	public VerifyAgainstTemplateView(StatisticalMatcher template) {
		super();
		this.template = template;
		this.setSize(640, 480);
		Box contentBox = new Box(BoxLayout.Y_AXIS);
		contentBox.add(createInputPanel());
		contentBox.add(createResultsPanel());
		this.add(contentBox);
	}

	private JComponent createInputPanel() {
		Box contentBox = new Box(BoxLayout.X_AXIS);

		contentBox.add(new JLabel(
				"Please type the password you used during enrollment:"));

		inputField = new JTextField(20);
		contentBox.add(inputField);
		contentBox.add(createDoneButton());
		return contentBox;
	}

	private JButton createDoneButton() {
		JButton doneButton = new JButton();
		doneButton.setText("Done");

		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VerifyAgainstTemplateView.this.setVisible(false);
				new EntryView().setVisible(true);
			}
		});
		return doneButton;
	}

	private JComponent createResultsPanel() {

		recorder = new KeystrokeRecorderImpl(new LowerCaseKeystrokeFactory());
		director = new KeystrokeDirector();
		director.setRecorder(recorder);
		director.addObserver(this);

		DynamicChartFactory chartFactory = new DynamicChartFactoryImpl(
				director, template);

		inputField.addKeyListener(director);

		Box echoBox = new Box(BoxLayout.Y_AXIS);

		status = new JTextArea();
		echoBox.add(status);

		echoBox.add(chartFactory.createDynamicKeystrokeSequenceChart());

		return echoBox;
	}

	public void notificationReceived(KeystrokeNotification event) {

		if (KeystrokeNotificationType.COMPLETION == event.getType()) {
			finishEntry();
		} else if (KeystrokeNotificationType.CORRUPTION == event.getType()) {
			corruptionDetected = true;
		}
	}

	private void finishEntry() {

		inputField.setText("");

		String message = "";

		KeystrokeSequence sequence = recorder.getResult();

		float distance = 0.0f;

		if (corruptionDetected) {
			message = "No match!";
		} else {
			distance = template.getDistance(sequence);

			if (distance >= getThreshold()) {
				message = "Match!";
			} else {
				message = "No match!";
			}
		}

		message += " (distance: " + distance + ")";

		updateStatus(message);

		status.setText(message);

		director.reset();
		corruptionDetected = false;
	}

	private void updateStatus(String message) {
		status.setText(message);
	}

	private float getThreshold() {
		return KeystrokeMatcher.EXACT_MATCH;
	}
}
