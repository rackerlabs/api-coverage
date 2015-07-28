package org.jenkinsci.plugins.ultimatecoverage;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import net.sf.json.JSONObject;

import java.io.Serializable;

/**
 * Created by prit8976 on 7/20/15.
 */
public class APICoverageBuildAction implements Action, Serializable {

    private JSONObject steps;
    private String template;
    private AbstractBuild<?, ?> build;

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    private Report report;

    @Override
    public String getIconFileName() {
        return "iconfilename";
    }

    @Override
    public String getDisplayName() {
        return "Build Report";
    }

    @Override
    public String getUrlName() {
        return "BuildReport";
    }

    public APICoverageBuildAction(final AbstractBuild<?, ?> build, final JSONObject steps, final String template) {
        this.build = build;
        this.steps = steps;
        this.template = template;
    }

    public AbstractBuild<?, ?> getBuild() {
        return this.build;
    }

    public JSONObject getSteps() {
        return this.steps;
    }

    public String getTemplate() {
        return this.template;
    }
}
