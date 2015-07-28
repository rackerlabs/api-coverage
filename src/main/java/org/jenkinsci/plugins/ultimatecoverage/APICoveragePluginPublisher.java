package org.jenkinsci.plugins.ultimatecoverage;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APICoveragePluginPublisher extends Recorder {

    private String templateFile;
    private String stepsFile;

    @DataBoundConstructor
    public APICoveragePluginPublisher(String stepsFile, String templateFile) {

        this.stepsFile = stepsFile;
        this.templateFile = templateFile;
    }

    public String getStepsFile() {
        return this.stepsFile;
    }

    public String getTemplateFile() {return this.templateFile;}

    @Override
    public boolean perform(final AbstractBuild build, Launcher launcher, BuildListener listener) {
        FilePath fp_path = new FilePath(build.getWorkspace(), getStepsFile());
        FilePath fp_template = new FilePath(build.getWorkspace(), getTemplateFile());

        String jsonData_path = null, template = null;

        try {
            jsonData_path = fp_path.readToString();
            template = fp_template.readToString();
        } catch (FileNotFoundException e) {
            System.out.println("The user entered file name does not exist in the workspace. ");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Pattern logEntry1 = Pattern.compile("\\[\"(.*?)\"\\]");
        Matcher matchPattern1 = logEntry1.matcher(jsonData_path);

        JSONArray jArray = new JSONArray();
        String temp;
        String path_str="";

        while (matchPattern1.find()) {
            temp = matchPattern1.group();
            jArray.add(temp);

            path_str = path_str+"{\"steps\":"+matchPattern1.group()+"}\n";
        }

        JSONObject jobj = new JSONObject();
        jobj.put("steps", jArray);

        APICoverageBuildAction buildAction;
        buildAction = new APICoverageBuildAction(build, path_str.trim(), template);
        build.addAction(buildAction);


        Report Report_Obj = new Report(build.getTimeInMillis(), build);
        Report_Obj.getBuildReport(jobj, template);

        buildAction.setReport(Report_Obj);

        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new APICoverageProjectAction(project);
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckTemplateFile(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please provide the file name with extension");

            return FormValidation.ok();
        }

        public FormValidation doCheckStepsFile(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please provide the file name with extension");

            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Enable API Coverage";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }
    }
}

