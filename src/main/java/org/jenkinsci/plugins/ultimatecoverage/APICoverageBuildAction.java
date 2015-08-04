package org.jenkinsci.plugins.ultimatecoverage;

import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.io.Serializable;

/**
 * Created by prit8976 on 7/20/15.
 */
public class APICoverageBuildAction implements Action, Serializable {

    private String steps;
    private String template;
    private AbstractBuild<?, ?> build;
    private Report report;

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public int getBuildNumber() {return this.build.number;}

    @Override
    public String getIconFileName() {
        return "/plugin/ultimate-coverage/img/image.png";
    }

    @Override
    public String getDisplayName() {
        return "API Coverage Build Report";
    }

    @Override
    public String getUrlName() {
        return "BuildReport";
    }

    public APICoverageBuildAction(final AbstractBuild<?, ?> build, final String path_str, final String template) {
        this.build = build;
        this.steps = path_str;
        this.template = template;
    }

    public AbstractBuild<?, ?> getBuild() {
        return this.build;
    }

    public String getSteps() {
        return this.steps;
    }

    public String getTemplate() {
        return this.template;
    }
}