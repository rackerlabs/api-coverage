//package org.jenkinsci.plugins.apicoverage;
//
//import org.jenkinsci.plugins.apicoverage.APICoverageProjectAction;
//import hudson.model.AbstractBuild;
//import hudson.model.AbstractProject;
//import hudson.util.Graph;
//import junit.framework.TestCase;
//import org.jfree.chart.JFreeChart;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.mockito.Mockito.*;
//
///**
// * Created by prit8976 on 8/13/15.
// */
//public class APICoverageProjectActionTest extends TestCase {
//
//    public void testCreateGraph () {
//
//        AbstractProject project = mock(AbstractProject.class);
//        AbstractBuild build = mock(AbstractBuild.class);
//        Reports reports = mock(Reports.class);
//
//        List<Report> reportsList = new ArrayList<Report>();
//
//        when(reports.getExistingReportsList(project)).thenReturn(reportsList);
//
//        Graph testGraph = spy(new Graph(4,4,4) {
//            @Override
//            private JFreeChart createGraph() {
//                return null;
//            }
//        });
//        doReturn(true).when(testGraph).doPng(request, response);
//    }
//
//}