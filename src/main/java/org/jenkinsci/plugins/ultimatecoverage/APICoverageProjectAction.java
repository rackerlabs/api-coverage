package org.jenkinsci.plugins.ultimatecoverage;

import hudson.model.AbstractProject;
import hudson.model.Action;

import java.io.Serializable;

/**
 * Created by prit8976 on 7/16/15.
 */
public class APICoverageProjectAction implements Action, Serializable
{
    private AbstractProject<?,?> project;
    protected String template;

    public AbstractProject<?,?> getProject() {
        return this.project;
    }

    public String getTemplate() {
        return this.template;
    }

    @Override
    public String getIconFileName() {
        return "iconfilename";
    }

    public String getDisplayName() {
        return "Project Report";
    }

    public String getUrlName() {
        return "ProjectReport";
    }

    public APICoverageProjectAction(final AbstractProject<?,?> project, String template)
    {
        System.out.println("in APICoverageProjectAction's constructor");
        this.project = project;
        this.template = template;
    }
}
