package net.ladieu.biometrics.keystroke.stats;

import java.util.List;

import net.ladieu.biometrics.keystroke.model.StatisticalMatcher;

import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

public class StatisticalDatasetBuilder {

	private StatisticalMatcher templateSource;

	public StatisticalDatasetBuilder(StatisticalMatcher matcher) {
		super();
		this.templateSource = matcher;
	}

	public void refreshStats(DefaultBoxAndWhiskerCategoryDataset data) {

		data.clear();

		List<BoxAndWhiskerItem> dwellStats = templateSource
				.getDwellTimeStatistics();
		List<BoxAndWhiskerItem> flightStats = templateSource
				.getFlightTimeStatistics();

		if (dwellStats.size() != flightStats.size()) {
			throw new IllegalStateException(
					"dwell and flight should have the same size stats listing");
		}

		for (int i = 0; i < dwellStats.size(); i++) {
			data.add(dwellStats.get(i), "dwell", i + 1);
			data.add(flightStats.get(i), "flight", i + 1);
		}
	}

	public DefaultBoxAndWhiskerCategoryDataset generateStats() {
		DefaultBoxAndWhiskerCategoryDataset result = new DefaultBoxAndWhiskerCategoryDataset();

		refreshStats(result);

		return result;
	}
}
