package org.jenkinsci.plugins.apicoverage;

import hudson.model.AbstractBuild;

/**
 * Created by prit8976 on 7/20/15.
 */
public class Report {
    private long timestamp;
    private AbstractBuild build;
    private Statistics statistics;

    public Statistics getStatistics() {
        return this.statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public Report(long timestamp, AbstractBuild build) {
        this.build = build;
        this.timestamp = timestamp;
        this.statistics = new Statistics();
    }
}
