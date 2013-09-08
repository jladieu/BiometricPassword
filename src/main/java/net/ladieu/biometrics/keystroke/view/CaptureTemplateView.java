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
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.ladieu.biometrics.keystroke.controller.KeystrokeDirector;
import net.ladieu.biometrics.keystroke.controller.KeystrokeDirectorObserver;
import net.ladieu.biometrics.keystroke.controller.KeystrokeNotification;
import net.ladieu.biometrics.keystroke.controller.KeystrokeNotification.KeystrokeNotificationType;
import net.ladieu.biometrics.keystroke.model.ExactPhraseMatcher;
import net.ladieu.biometrics.keystroke.model.KeystrokeMatcher;
import net.ladieu.biometrics.keystroke.model.KeystrokeRecorder;
import net.ladieu.biometrics.keystroke.model.KeystrokeRecorderImpl;
import net.ladieu.biometrics.keystroke.model.KeystrokeSequence;
import net.ladieu.biometrics.keystroke.model.LowerCaseKeystrokeFactory;
import net.ladieu.biometrics.keystroke.model.StatisticalMatcher;

public class CaptureTemplateView extends JFrame implements
		KeystrokeDirectorObserver {

	private static final long serialVersionUID = 1186990717421258761L;

	private String username;
	private JTextArea status;
	private JTextField inputField;

	private KeystrokeRecorder recorder;
	private KeystrokeDirector director;

	private ExactPhraseMatcher enrollmentMatcher;
	private StatisticalMatcher template;

	private boolean discardCurrentRecording;

	public CaptureTemplateView(String username, String password) {
		super("Main View");

		this.setSize(640, 480);
		this.username = username;
		Box contentBox = new Box(BoxLayout.Y_AXIS);
		contentBox.add(createInputPanel(password));
		contentBox.add(createStatus());
		contentBox.add(createResultsPanel(username, password));
		this.add(contentBox);
	}

	/**
	 * Helper constructor to allow preloading of data for the provided username.
	 * Helpful for development and debugging.
	 * 
	 * @param username
	 *            username to use
	 * @param password
	 *            password for the user
	 * @param preload
	 *            flag indicating if a preload is required
	 */
	public CaptureTemplateView(String username, String password, boolean preload) {
		this(username, password);
		if (preload) {
			loadData();
		}
	}

	private JComponent createStatus() {
		status = new JTextArea();
		return status;
	}

	private JComponent createInputPanel(String password) {
		Box contentBox = new Box(BoxLayout.X_AXIS);

		contentBox.add(new JLabel("Please type [" + password + "]:"));

		inputField = new JTextField(20);
		contentBox.add(inputField);

		contentBox.add(createFilterButton());
		contentBox.add(createSaveButton());
		contentBox.add(createLoadButton());
		contentBox.add(createDoneButton());

		return contentBox;
	}

	private JComponent createResultsPanel(String username, String password) {

		discardCurrentRecording = false;
		recorder = new KeystrokeRecorderImpl(new LowerCaseKeystrokeFactory());
		director = new KeystrokeDirector();
		director.setRecorder(recorder);
		director.addObserver(this);
		enrollmentMatcher = new ExactPhraseMatcher(password);
		template = new StatisticalMatcher(username, password);

		DynamicChartFactory chartFactory = new DynamicChartFactoryImpl(
				director, template);

		inputField.addKeyListener(director);

		Box contentBox = new Box(BoxLayout.X_AXIS);

		contentBox.add(chartFactory.createDynamicKeystrokeSequenceChart());

		contentBox.add(chartFactory.createDynamicStatisticalChart());

		return contentBox;
	}

	public void notificationReceived(KeystrokeNotification event) {

		if (KeystrokeNotificationType.COMPLETION == event.getType()) {
			finishEntry();
		} else if (KeystrokeNotificationType.CORRUPTION == event.getType()) {
			discardCurrentRecording = true;
		}
	}

	private void finishEntry() {

		inputField.setText("");

		String message = "";

		KeystrokeSequence sequence = recorder.getResult();

		if (discardCurrentRecording) {
			message = "The template you captured was corrupted by a typo, this template will be ignored.";
		} else if (KeystrokeMatcher.EXACT_MATCH == enrollmentMatcher
				.getDistance(sequence)) {
			message = "Match!";
			template.addSequence(sequence);
		} else {
			message += "No match!";
		}

		message += getFilterStatus();
		updateStatus(message);

		status.setText(message);

		director.reset();
		discardCurrentRecording = false;
	}

	private String getFilterStatus() {
		return "\nCurrent Templates: "
				+ template.getNumberOfTemplatesCaptured()
				+ "\nFiltered Templates: "
				+ template.getNumberOfFilteredTemplates();
	}

	private void updateStatus(String message) {
		status.setText(message);
	}

	private JButton createFilterButton() {
		JButton filterButton = new JButton();
		filterButton.setText("Filter Outliers");
		filterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				director.filterOutliers();
				updateStatus(getFilterStatus());
			}
		});

		return filterButton;
	}

	private JButton createSaveButton() {
		JButton saveButton = new JButton();
		saveButton.setText("Save Template");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				template.save();
			}
		});

		return saveButton;
	}

	private JButton createLoadButton() {
		JButton loadButton = new JButton();
		loadButton.setText("Load Template");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				loadData();
			}
		});

		return loadButton;
	}
	
	private JButton createDoneButton() {
		JButton doneButton = new JButton();
		doneButton.setText("Done");
		
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CaptureTemplateView.this.setVisible(false);
				new EntryView().setVisible(true);
			}
		});
		return doneButton;
	}


	private void loadData() {

		try {
			template.restore(username);
		} catch (IOException e) {
			status.setText("Unable to load template for [" + username
					+ "]; data doesn't appear to exist.");
		}
		director.refreshViews();
	}

	public void displayVerifyAgainstTemplateView() {
		this.setVisible(false);
		new VerifyAgainstTemplateView(template).setVisible(true);
	}
}
