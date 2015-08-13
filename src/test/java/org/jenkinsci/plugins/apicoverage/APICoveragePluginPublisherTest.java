package org.jenkinsci.plugins.apicoverage;

import hudson.Launcher;
import hudson.model.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by prit8976 on 8/10/15.
 */

public class APICoveragePluginPublisherTest {

    @Rule
    public JenkinsRule jr = new JenkinsRule();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testEmptyFileException () throws Exception {
        String path = "path.json";
        String template = "templateEmpty.json";

        FreeStyleProject p = jr.createFreeStyleProject();
        p.getBuildersList().add(new TestBuilder() {
                                    @Override
                                    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                                        build.getWorkspace().child("templateEmpty.json").copyFrom(getClass().getResource("/templateEmpty.json"));
                                        build.getWorkspace().child("path.json").copyFrom(getClass().getResource("/path.json"));
                                        return true;
                                    }
                                }
        );
        APICoveragePluginPublisher publisher = new APICoveragePluginPublisher(path, template);
        p.getPublishersList().add(publisher);

        FreeStyleBuild b = p.scheduleBuild2(0).get();
        jr.assertBuildStatus(Result.FAILURE, b);
        exception.expect(IOException.class);
        exception.expectMessage("The template file has no content.");

        throw new IOException("The template file has no content.");
    }

    @Test
    public void testPathFileNotInWorkspace () throws Exception {
        String path = "pathNotInWorkspace.json";
        String template = "template.json";

        FreeStyleProject p = jr.createFreeStyleProject();
        p.getBuildersList().add(new TestBuilder() {
                                    @Override
                                    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                                        build.getWorkspace().child("template.json").copyFrom(getClass().getResource("/template.json"));
                                        return true;
                                    }
                                });
        APICoveragePluginPublisher publisher = new APICoveragePluginPublisher(path, template);
        p.getPublishersList().add(publisher);

        FreeStyleBuild b = p.scheduleBuild2(0).get();
        jr.assertBuildStatus(Result.FAILURE, b);
        exception.expect(IOException.class);
        exception.expectMessage("The user entered file name does not exist in the workspace.");

        throw new FileNotFoundException("The user entered file name does not exist in the workspace.");
    }

    @Test
    public void testTemplateFileNotInWorkspace () throws Exception {
        String path = "path.json";
        String template = "templateNotInWorkspace.json";

        FreeStyleProject p = jr.createFreeStyleProject();
        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                build.getWorkspace().child("path.json").copyFrom(getClass().getResource("/path.json"));
                return true;
            }
        });
        APICoveragePluginPublisher publisher = new APICoveragePluginPublisher(path, template);
        p.getPublishersList().add(publisher);

        FreeStyleBuild b = p.scheduleBuild2(0).get();
        jr.assertBuildStatus(Result.FAILURE, b);
        exception.expect(IOException.class);
        exception.expectMessage("The user entered file name does not exist in the workspace.");

        throw new FileNotFoundException("The user entered file name does not exist in the workspace.");
    }

    @Test
    public void testBuildActionCreated () throws Exception {
        String path = "path.json";
        String template = "template.json";

        FreeStyleProject p = jr.createFreeStyleProject();

        p.getBuildersList().add(new TestBuilder() {
                                    @Override
                                    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                                        build.getWorkspace().child("template.json").copyFrom(getClass().getResource("/template.json"));
                                        build.getWorkspace().child("path.json").copyFrom(getClass().getResource("/path.json"));
                                        return true;
                                    }
                                }
        );
        APICoveragePluginPublisher publisher = new APICoveragePluginPublisher(path,template);
        p.getPublishersList().add(publisher);

        FreeStyleBuild b = p.scheduleBuild2(0).get();

        jr.assertBuildStatus(Result.SUCCESS, b);
        APICoverageBuildAction buildAction = b.getAction(APICoverageBuildAction.class);
        APICoverageProjectAction projectAction = p.getAction(APICoverageProjectAction.class);
        assert buildAction!=null;
        assert projectAction!=null;

        JenkinsRule.WebClient wc = jr.createWebClient();

        wc.setJavaScriptEnabled(false);

        wc.getPage(b, "BuildReport");
        wc.getPage(p, "ProjectReport");
    }
}