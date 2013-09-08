package net.ladieu.biometrics.keystroke.view;

import org.jfree.chart.ChartPanel;

public interface DynamicChartFactory {

	ChartPanel createDynamicKeystrokeSequenceChart();

	ChartPanel createDynamicStatisticalChart();
}
