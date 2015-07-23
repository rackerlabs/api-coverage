package org.jenkinsci.plugins.ultimatecoverage;

import hudson.model.Action;

/**
 * Created by prit8976 on 7/20/15.
 */
public class Report implements Action {

    private long totalCalls;
    private long failures;
    private double percent_happy;
    private double percent_unhappy;

    public long getTotalCalls() {
        return totalCalls;
    }

    public long getFailures() {
        return failures;
    }

    public double getPercent_happy() {
        return percent_happy;
    }

    public double getPercent_unhappy() {
        return percent_unhappy;
    }

    Report(long totalCalls,long failures,double percent_happy,double percent_unhappy){
        this.totalCalls = totalCalls;
        this.failures = failures;
        this.percent_happy = percent_happy;
        this.percent_unhappy = percent_unhappy;
    }

    @Override
    public String getIconFileName() {
        return "reporticon";
    }

    @Override
    public String getDisplayName() {
        return "Report";
    }

    @Override
    public String getUrlName() {
        return "Report";
    }
}
