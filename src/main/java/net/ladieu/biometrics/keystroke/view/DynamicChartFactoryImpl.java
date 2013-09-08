package net.ladieu.biometrics.keystroke.view;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.ladieu.biometrics.keystroke.controller.KeystrokeDirector;
import net.ladieu.biometrics.keystroke.controller.KeystrokeDirectorObserver;
import net.ladieu.biometrics.keystroke.controller.KeystrokeNotification;
import net.ladieu.biometrics.keystroke.controller.KeystrokeNotification.KeystrokeNotificationType;
import net.ladieu.biometrics.keystroke.model.Keystroke;
import net.ladieu.biometrics.keystroke.model.StatisticalMatcher;
import net.ladieu.biometrics.keystroke.stats.StatisticalDatasetBuilder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class DynamicChartFactoryImpl implements DynamicChartFactory {

	private KeystrokeDirector changeSource;

	private StatisticalMatcher template;

	public DynamicChartFactoryImpl(KeystrokeDirector changeSource,
			StatisticalMatcher template) {
		super();
		this.changeSource = changeSource;
		this.template = template;
	}

	public ChartPanel createDynamicKeystrokeSequenceChart() {

		DataUpdatingKeystrokeObserver dataUpdater = new DataUpdatingKeystrokeObserver();
		changeSource.addObserver(dataUpdater);

		XYDataset dynamicData = dataUpdater.getDynamicData();

		JFreeChart chart = ChartFactory.createXYLineChart("Keystroke Dynamics",
				"Keystroke", "Timing (ms)", dynamicData,
				PlotOrientation.VERTICAL, true, false, false);

		return new ChartPanel(chart);
	}

	public ChartPanel createDynamicStatisticalChart() {
		StatisticsUpdatingKeystrokeObserver statisticsUpdater = new StatisticsUpdatingKeystrokeObserver(
				template);
		changeSource.addObserver(statisticsUpdater);

		DefaultBoxAndWhiskerCategoryDataset dynamicData = statisticsUpdater
				.getDynamicData();

		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
				"Keystroke Statistics", "Keystroke", "Stats (ms)", dynamicData,
				true);

		return new ChartPanel(chart);
	}

	private class DataUpdatingKeystrokeObserver implements
			KeystrokeDirectorObserver {
		private XYSeriesCollection data;
		private XYSeries currentDwellSeries;
		private XYSeries currentFlightSeries;

		private int currentIndex;
		private Keystroke nextReadableKeystroke;
		private static final int START_INDEX = 0;

		private DataUpdatingKeystrokeObserver() {
			super();

			data = new XYSeriesCollection();
			data.setIntervalPositionFactor(0.0);

			currentDwellSeries = new XYSeries("Dwell", false, true);
			currentFlightSeries = new XYSeries("Flight", false, true);

			data.addSeries(currentDwellSeries);
			data.addSeries(currentFlightSeries);

			initializeSeries();
		}

		public synchronized void notificationReceived(
				KeystrokeNotification event) {
			if (KeystrokeNotificationType.UPDATE == event.getType()) {

				if (START_INDEX == currentIndex) {
					currentDwellSeries.clear();
					currentFlightSeries.clear();
				}

				nextReadableKeystroke = event.getOrigin().getResult()
						.getKeystroke(currentIndex);

				while (null != nextReadableKeystroke
						&& nextReadableKeystroke.isReleased()) {
					reportReleasedKeystroke(nextReadableKeystroke);
					nextReadableKeystroke = nextReadableKeystroke.getNext();
				}

			} else if (KeystrokeNotificationType.COMPLETION == event.getType()) {
				initializeSeries();
			}
		}

		private void reportReleasedKeystroke(Keystroke releasedKeystroke) {
			currentDwellSeries.add(new XYDataItem(currentIndex + 1,
					releasedKeystroke.getDwellTime()));
			currentFlightSeries.add(new XYDataItem(currentIndex + 1,
					releasedKeystroke.getFlightTime()));
			currentIndex++;
		}

		private XYSeriesCollection getDynamicData() {
			return data;
		}

		private void initializeSeries() {
			nextReadableKeystroke = null;
			currentIndex = 0;
		}

	}

	private class StatisticsUpdatingKeystrokeObserver implements
			KeystrokeDirectorObserver {

		private DefaultBoxAndWhiskerCategoryDataset data;

		private StatisticalMatcher templateSource;

		private StatisticsUpdatingKeystrokeObserver(
				StatisticalMatcher templateSource) {
			super();

			this.templateSource = templateSource;
			data = new DefaultBoxAndWhiskerCategoryDataset();
		}

		public synchronized void notificationReceived(
				KeystrokeNotification event) {

			boolean refreshRequired = (KeystrokeNotificationType.FILTER == event
					.getType()
					|| KeystrokeNotificationType.COMPLETION == event.getType() || KeystrokeNotificationType.REFRESH == event
					.getType());

			if (KeystrokeNotificationType.FILTER == event.getType()) {
				templateSource.filterOutliers(getOutliers("flight"),
						getOutliers("dwell"));
			}

			if (refreshRequired) {
				StatisticalDatasetBuilder dataBuilder = new StatisticalDatasetBuilder(
						templateSource);
				dataBuilder.refreshStats(data);
			}
		}

		private DefaultBoxAndWhiskerCategoryDataset getDynamicData() {
			return data;
		}

		/**
		 * Creates a Map keyed by the keystroke index, with values containing
		 * the Set of outliers for that index.
		 * 
		 * @param typeOfData -
		 *            flight or dwell
		 * @return a Map of Outlier sets keyed by index
		 */
		@SuppressWarnings("unchecked")
		private Map<Integer, Set<Number>> getOutliers(String typeOfData) {
			Map<Integer, Set<Number>> outlierMap = new LinkedHashMap<Integer, Set<Number>>();

			List<Comparable> columnKeys = data.getColumnKeys();

			for (Comparable columnKey : columnKeys) {

				List<Number> outliers = data.getOutliers(typeOfData, columnKey);

				Set<Number> outlierDataSet = new HashSet<Number>(outliers);

				// they are keyed by array index + 1, so store them keyed by key
				// value - 1
				outlierMap.put(((Number) columnKey).intValue() - 1,
						outlierDataSet);

			}

			return outlierMap;
		}
	}

}
