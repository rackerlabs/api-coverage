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

    public APICoverageBuildAction (final AbstractBuild<?, ?> build, final String steps, final String template)
    {
        System.out.println("in APICoverageBuildAction's constructor");
        this.build = build;
        this.steps = steps;
        this.template = template;
    }

    public AbstractBuild<?, ?> getBuild() {
        return this.build;
    }

    public String getSteps(){
        return this.steps;
    }

    public String getTemplate(){
        return this.template;
    }
}
