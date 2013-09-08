package net.ladieu.biometrics.keystroke.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.ladieu.biometrics.keystroke.model.persistence.SerializingTemplateStorage;
import net.ladieu.biometrics.keystroke.model.persistence.TemplateStorage;
import net.ladieu.biometrics.keystroke.stats.KeystrokeDwellTimeVisitor;
import net.ladieu.biometrics.keystroke.stats.KeystrokeFlightTimeVisitor;
import net.ladieu.biometrics.keystroke.stats.KeystrokeVisitor;
import net.ladieu.biometrics.keystroke.stats.StatisticalDatasetBuilder;

import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

public class StatisticalMatcher implements KeystrokeMatcher {

	private KeystrokeMatcher internalTemplate;

	private List<KeystrokeSequence> templateBasis = new ArrayList<KeystrokeSequence>();

	private List<BoxAndWhiskerItem> flightStats = new ArrayList<BoxAndWhiskerItem>();
	private List<BoxAndWhiskerItem> dwellStats = new ArrayList<BoxAndWhiskerItem>();;

	private TemplateStorage storage = new SerializingTemplateStorage();
	private String username;
	private int samplePoints;
	private boolean statsDirty = true;
	private int numberOfFilteredTemplates = 0;

	/**
	 * If constructed with both username and password, will load a clean slate
	 * for enrollment.
	 * 
	 * @param username
	 *            username to enroll
	 * @param password
	 *            password to be used in authenticating user
	 */
	public StatisticalMatcher(String username, String password) {
		super();
		initialize(username, password);
	}

	/**
	 * If constructed with just a username, will attempt to load the password
	 * data from storage.
	 * 
	 * @param username
	 *            username for which to load template data
	 * @throws IOException
	 *             if no template data exists
	 */
	public StatisticalMatcher(String username) throws IOException {
		super();

		restore(username);
		
		if (templateBasis.isEmpty()) {
			throw new IOException(
					"Template basis must contain at least one sample");
		}

		String password = templateBasis.get(0).getCapturedValue();

		initialize(username, password);
	}

	private void initialize(String username, String password) {
		this.username = username;
		internalTemplate = new ExactPhraseMatcher(password);
		samplePoints = password.length();
	}

	public void addSequence(KeystrokeSequence sequence) {
		this.templateBasis.add(sequence);
		statsDirty = true;
	}

	public void recalculateStats() {
		dwellStats.clear();
		flightStats.clear();
		if (!templateBasis.isEmpty()) {
			dwellStats.addAll(analyzeData(new KeystrokeDwellTimeVisitor()));
			flightStats.addAll(analyzeData(new KeystrokeFlightTimeVisitor()));
		}

		statsDirty = false;
	}

	public float getDistance(KeystrokeSequence sequence) {
		if (KeystrokeMatcher.NO_MATCH == internalTemplate.getDistance(sequence)) {
			return KeystrokeMatcher.NO_MATCH;
		}

		return this.getStatisticalDistance(sequence);
	}

	protected float getStatisticalDistance(KeystrokeSequence sequence) {
		return Float.valueOf(1.0f - (getNumberOfOutliers(sequence) / Float
				.valueOf(samplePoints * 2))); // 2x sample points because we
		// capture set for flight and
		// set for dwell
	}

	private int getNumberOfOutliers(KeystrokeSequence sequence) {

		int numberOfOutliers = 0;

		StatisticalDatasetBuilder statsBuilder = new StatisticalDatasetBuilder(
				this);

		DefaultBoxAndWhiskerCategoryDataset stats = statsBuilder
				.generateStats();

		int keystrokeIndex = 1;
		for (Keystroke currentKeystroke : sequence) {

			long dwell = currentKeystroke.getDwellTime();
			long flight = currentKeystroke.getFlightTime();

			long minFlight = stats.getMinRegularValue("flight", keystrokeIndex)
					.longValue();
			long maxFlight = stats.getMaxRegularValue("flight", keystrokeIndex)
					.longValue();

			long minDwell = stats.getMinRegularValue("dwell", keystrokeIndex)
					.longValue();
			long maxDwell = stats.getMaxRegularValue("dwell", keystrokeIndex)
					.longValue();

			if (dwell < minDwell || dwell > maxDwell) {
				numberOfOutliers++;
			}

			if (flight < minFlight || flight > maxFlight) {
				numberOfOutliers++;
			}

			keystrokeIndex++;
		}

		return numberOfOutliers;
	}

	public List<BoxAndWhiskerItem> getFlightTimeStatistics() {
		if (statsDirty) {
			recalculateStats();
		}
		return new ArrayList<BoxAndWhiskerItem>(flightStats);
	}

	public List<BoxAndWhiskerItem> getDwellTimeStatistics() {
		if (statsDirty) {
			recalculateStats();
		}
		return new ArrayList<BoxAndWhiskerItem>(dwellStats);
	}

	protected List<BoxAndWhiskerItem> analyzeData(
			KeystrokeVisitor<Number> visitor) {

		BoxAndWhiskerItem[] resultArray = new BoxAndWhiskerItem[samplePoints];

		for (int i = 0; i < samplePoints; i++) {
			List<Number> currentSample = new ArrayList<Number>();

			for (KeystrokeSequence currentSequence : templateBasis) {
				currentSample.add(visitor.visitKeystroke(currentSequence
						.getKeystroke(i)));
			}

			resultArray[i] = getStatsForSample(currentSample);
		}

		return Arrays.asList(resultArray);
	}

	protected BoxAndWhiskerItem getStatsForSample(List<Number> sample) {
		return BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(sample);
	}

	public String getTextToMatch() {
		return internalTemplate.getTextToMatch();
	}

	public void filterOutliers(Map<Integer, Set<Number>> flightOutliers,
			Map<Integer, Set<Number>> dwellOutliers) {

		List<KeystrokeSequence> filteredTemplateBasis = new ArrayList<KeystrokeSequence>();

		for (KeystrokeSequence currentSequence : templateBasis) {

			boolean sequenceContainsOutlier = false;
			int keystrokeIndex = 0;
			for (Iterator<Keystroke> i = currentSequence.iterator(); i
					.hasNext()
					&& !sequenceContainsOutlier; keystrokeIndex++) {

				Set<Number> flightOutliersForKeystroke = flightOutliers
						.get(keystrokeIndex);
				Set<Number> dwellOutliersForKeystroke = dwellOutliers
						.get(keystrokeIndex);

				Keystroke currentKeystroke = i.next();

				if (flightOutliersForKeystroke.contains(currentKeystroke
						.getFlightTime())
						|| dwellOutliersForKeystroke.contains(currentKeystroke
								.getDwellTime())) {
					sequenceContainsOutlier = true;
				}
			}

			if (!sequenceContainsOutlier) {
				filteredTemplateBasis.add(currentSequence);
			}
		}

		numberOfFilteredTemplates += templateBasis.size()
				- filteredTemplateBasis.size();

		templateBasis.clear();
		templateBasis.addAll(filteredTemplateBasis);
		statsDirty = true;
	}

	public int getNumberOfTemplatesCaptured() {
		return templateBasis.size();
	}

	public int getNumberOfFilteredTemplates() {
		return numberOfFilteredTemplates;
	}

	public void save() {
		storage.saveTemplate(username, templateBasis);
	}

	public void restore(String username) throws IOException {
		this.templateBasis = storage.getStoredTemplate(username);
		statsDirty = true;
	}

}
