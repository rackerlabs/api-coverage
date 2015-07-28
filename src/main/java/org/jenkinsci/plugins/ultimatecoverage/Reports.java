package org.jenkinsci.plugins.ultimatecoverage;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prit8976 on 7/23/15.
 */
public class Reports {

    public List<Report> getListOfPreviousReports(AbstractBuild<?, ?> build, final long currentTimestamp) {
        List<Report> previousReports = new ArrayList<Report>();

        List<? extends AbstractBuild<?, ?>> builds = build.getProject().getBuilds();

        for (AbstractBuild<?, ?> currentBuild : builds) {
            Report report = getReportForBuild(currentBuild);
            if (report != null && (report.getTimestamp() != currentTimestamp || builds.size() == 1))
                previousReports.add(report);
        }
        return previousReports;
    }

    public Report getReportForBuild(AbstractBuild<?, ?> currentBuild) {
        if (currentBuild != null) {
            APICoverageBuildAction performanceBuildAction = currentBuild.getAction(APICoverageBuildAction.class);
            if (performanceBuildAction != null)
                return performanceBuildAction.getReport();
        }
        return null;
    }

    public List<Report> getExistingReportsList(AbstractProject<?, ?> project) {
        List<Report> reports = new ArrayList<Report>();
        if (project != null) {
            List<? extends AbstractBuild<?, ?>> builds = project.getBuilds();

            for (AbstractBuild<?, ?> currentBuild : builds) {
                Report report = getReportForBuild(currentBuild);
                if (report != null)
                    reports.add(report);
            }
        }
        return reports;
    }

    public double getPercentFailForProject(AbstractProject<?, ?> project) {
        if (project != null) {
            List<Report> existingReports = getExistingReportsList(project);
            if (existingReports == null)
                return 0.0;

            double projectFailures = 0.0, projectPercentFailures, projectTotalCalls = 0.0;
            for (Report report : existingReports) {
                projectFailures = projectFailures + (double) report.getFailures();
                projectTotalCalls = projectTotalCalls + (double) report.getTotalCalls();
            }

            projectPercentFailures = projectFailures / projectTotalCalls * 100.0;
            return projectPercentFailures;
        }

        return 0.0;
    }

    public double getPercentPassForProject(AbstractProject<?, ?> project) {
        if (project != null) {
            List<Report> existingReports = getExistingReportsList(project);
            if (existingReports == null)
                return 0.0;

            double projectPasses = 0.0, projectPercentPasses, projectTotalCalls = 0.0;
            for (Report report : existingReports) {
                projectPasses = projectPasses + (double) report.getPasses();
                projectTotalCalls = projectTotalCalls + (double) report.getTotalCalls();
            }

            projectPercentPasses = projectPasses / projectTotalCalls * 100.0;
            return projectPercentPasses;
        }

        return 0.0;
    }

    public long getTotalFailCallsForProject(AbstractProject<?, ?> project) {
        if (project != null) {
            List<Report> existingReports = getExistingReportsList(project);
            if (existingReports == null)
                return 0;

            long projectFailures = 0;
            for (Report report : existingReports)
                projectFailures = projectFailures + report.getFailures();

            return projectFailures;
        }

        return 0;
    }

    public long getTotalPassCallsForProject(AbstractProject<?, ?> project) {
        if (project != null) {
            List<Report> existingReports = getExistingReportsList(project);
            if (existingReports == null)
                return 0;

            long projectPasses = 0;
            for (Report report : existingReports)
                projectPasses = projectPasses + report.getPasses();

            return projectPasses;
        }

        return 0;
    }
}
