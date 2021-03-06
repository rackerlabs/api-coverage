package org.jenkinsci.plugins.apicoverage;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Created by prit8976 on 7/16/15.
 */
public class APICoverageProjectAction implements Action {
    private AbstractProject<?, ?> project;
    Reports reports;
    private String displayName = "API Coverage Project Report";
    private String iconFileName = "/plugin/api-coverage/img/line_chart_icon.jpg";
    private String urlName = "ProjectReport";

    public AbstractProject<?, ?> getProject() {
        return this.project;
    }

    public String getProjectName() {
        return this.project.getName();
    }

    @Override
    public String getIconFileName() {
        return iconFileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrlName() {
        return urlName;
    }

//    public double getPercentFail() {
//        return Math.round(reports.getPercentFailForProject(getProject()) * 100.0)/100.0;
//    }
//
//    public double getPercentPass() {
//        return Math.round(reports.getPercentPassForProject(getProject()) * 100.0)/100.0;
//    }
//
//    public long getTotalPass() {
//        return reports.getTotalPassCallsForProject(getProject());
//    }
//
//    public long getTotalFail() {
//        return reports.getTotalFailCallsForProject(getProject());
//    }

    public APICoverageProjectAction(final AbstractProject<?, ?> project) {
        System.out.println("in APICoverageProjectAction's constructor");
        this.project = project;
        this.reports = new Reports();
    }

    private void createGraph(final StaplerRequest request, final StaplerResponse response) throws IOException
    {
        new GraphImpl("API Coverage Graph").doPng(request, response);
    }

    public void doProjectGraph(final StaplerRequest request, final StaplerResponse response) throws IOException {
        createGraph(request, response);
    }

    private class GraphImpl extends Graph
    {
        private final String graphTitle;

        protected DataSetBuilder<String, NumberOnlyBuildLabel> createDataSet()
        {
            DataSetBuilder<String, NumberOnlyBuildLabel> dataSetBuilder = new DataSetBuilder<String, NumberOnlyBuildLabel>();

            List<Report> ExistingReports = reports.getExistingReportsList(getProject());

            for (Report report : ExistingReports) {
                Run<?, ?> build = report.getBuild();
                if (build != null)
                {
                    dataSetBuilder.add(report.getStatistics().getPercentHappy(), "Positive API Coverage", new NumberOnlyBuildLabel(build));
                    dataSetBuilder.add(report.getStatistics().getPercentUnhappy(), "Negative API Coverage", new NumberOnlyBuildLabel(build));
                }
            }
            return dataSetBuilder;
        }

        protected GraphImpl(final String graphName)
        {
            super(-1, 400, 400);
            this.graphTitle = graphName;
        }

        protected JFreeChart createGraph()
        {
            final CategoryDataset dataset = createDataSet().build();

            final JFreeChart chart = ChartFactory.createLineChart(graphTitle, // title
                    "Build #", // category axis label
                    "Percentage %", // value axis label
                    dataset, // data
                    PlotOrientation.VERTICAL, // orientation
                    true, // include legend
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
            renderer.setBaseShapesVisible(true);
            renderer.setBaseItemLabelsVisible(Boolean.TRUE);
            renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());

            renderer.setBaseStroke(new BasicStroke(2.0f));

            return chart;
        }
    }
}
