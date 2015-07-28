package org.jenkinsci.plugins.ultimatecoverage;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by prit8976 on 7/16/15.
 */
public class APICoverageProjectAction implements Action {
    private AbstractProject<?, ?> project;
    Reports reports;

    public AbstractProject<?, ?> getProject() {
        return this.project;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/ultimate-coverage/img/line_chart_icon.jpg";
    }

    public String getDisplayName() {
        return "API Coverage Project Report";
    }

    public String getUrlName() {
        return "ProjectReport";
    }

    public String getPercentFail() {
        reports = new Reports();
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(reports.getPercentFailForProject(getProject()));
    }

    public String getPercentPass() {
        reports = new Reports();
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(reports.getPercentPassForProject(getProject()));
    }

    public long getTotalPass() {
        reports = new Reports();
        return reports.getTotalPassCallsForProject(getProject());
    }

    public long getTotalFail() {
        reports = new Reports();
        return reports.getTotalFailCallsForProject(getProject());
    }

    public APICoverageProjectAction(final AbstractProject<?, ?> project) {
        System.out.println("in APICoverageProjectAction's constructor");
        this.project = project;
    }

    private void createGraph(final StaplerRequest request, final StaplerResponse response) throws IOException {
        final Graph graph = new GraphImpl("Overall Graph") {

            protected DataSetBuilder<String, NumberOnlyBuildLabel> createDataSet() {
                DataSetBuilder<String, NumberOnlyBuildLabel> dataSetBuilder = new DataSetBuilder<String, NumberOnlyBuildLabel>();

                Reports reports = new Reports();

                List<Report> ExistingReports = reports.getExistingReportsList(getProject());

                for (Report report : ExistingReports) {
                    Run<?, ?> build = report.getBuild();
                    if (build != null) {

                        dataSetBuilder.add(report.getPercentHappy(), "hii", new NumberOnlyBuildLabel(build));
                        dataSetBuilder.add(report.getPercentUnhappy(), "hi", new NumberOnlyBuildLabel(build));

                    }
                }
                return dataSetBuilder;
            }
        };

        graph.doPng(request, response);
    }

    public void doProjectGraph(final StaplerRequest request, final StaplerResponse response) throws IOException {
        createGraph(request, response);
    }

    private abstract class GraphImpl extends Graph {
        private final String graphTitle;

        protected GraphImpl(final String graphName) {
            super(-1, 400, 400);
            this.graphTitle = graphName;
        }

        protected abstract DataSetBuilder<String, NumberOnlyBuildLabel> createDataSet();

        protected JFreeChart createGraph() {
            final CategoryDataset dataset = createDataSet().build();

            final JFreeChart chart = ChartFactory.createLineChart(graphTitle, // title
                    "Build #", // category axis label
                    "Percentage", // value axis label
                    dataset, // data
                    PlotOrientation.VERTICAL, // orientation
                    false, // include legend
                    true, // tooltips
                    false // urls
            );

            chart.setBackgroundPaint(Color.white);

            final CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlinePaint(null);
            plot.setRangeGridlinesVisible(true);
            plot.setRangeGridlinePaint(Color.black);

            final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            rangeAxis.setUpperBound(100.0);
            rangeAxis.setLowerBound(0);

            final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();

            renderer.setSeriesPaint(0, Color.red);
            renderer.setSeriesPaint(1, Color.green);

            renderer.setBaseStroke(new BasicStroke(4.0f));

            return chart;
        }
    }
}
