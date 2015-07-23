package org.jenkinsci.plugins.ultimatecoverage;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.Action;
import hudson.tasks.*;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APICoveragePluginPublisher extends Recorder {

    private final String name;
    private String template;

    @DataBoundConstructor
    public APICoveragePluginPublisher(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean perform(final AbstractBuild build, Launcher launcher, BuildListener listener)
    {
        FilePath fp_path = new FilePath(build.getWorkspace(), "path.json");
        FilePath fp_template = new FilePath(build.getWorkspace(), "template.json");

        String jsonData_path = null;

        try {
            jsonData_path = fp_path.readToString();
            template = fp_template.readToString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //JSONObject jsonObj_path = JSONObject.fromObject(jsonData_path);

        APICoverageBuildAction buildAction;
        buildAction = new APICoverageBuildAction(build, jsonData_path, template);
        build.addAction(buildAction);
        //listener.getLogger().println("path: "+jsonData_path+"\ntemplate: " + template);

        verification(jsonData_path, template);

        return true;
    }

    public void verification(String path, String template){

        System.out.println(template);
        Pattern logEntry = Pattern.compile("[^;|\\{|\t|\n|\\s" +
                "]*->(.*?)[^\\s|\t|\n" +
                "|;]*");
        Matcher matchPattern = logEntry.matcher(template);

        Map<String, HashSet<String>> valid_template_map = new HashMap<String, HashSet<String>>();
        HashSet<String> hs;
        String[] str;

        while(matchPattern.find())
        {
            //System.out.println("raw data: "+matchPattern.group(0));
            str = matchPattern.group().split("->");
            if (valid_template_map.containsKey(str[0]))
                if (valid_template_map.get(str[0]).contains(str[1]))
                    continue;
                else valid_template_map.get(str[0]).add(str[1]);
            else
            {
                hs = new HashSet<String>();
                hs.add(str[1]);
                valid_template_map.put(str[0], hs);
            }
        }
        System.out.println("valid template map: "+valid_template_map+"\n");

        Pattern logEntry1 = Pattern.compile("\\{(.*?)\\}");
        Matcher matchPattern1 = logEntry1.matcher(path);
        Map<JSONArray, Long> failure_map = new HashMap<JSONArray, Long>();
        Map<JSONArray, Long> pass_map = new HashMap<JSONArray, Long>();

        JSONObject j_obj;
        JSONArray jsonMainArr;
        boolean pass = false;
        String next_hop;
        HashSet<String> node_map;

        long total_calls = 0;
        long happy_calls = 0;
        long unhappy_calls = 0;

        while(matchPattern1.find()) {
            //System.out.println("steps thingy: "+matchPattern1.group());
            total_calls++;
            j_obj = JSONObject.fromObject(matchPattern1.group());

            jsonMainArr = j_obj.getJSONArray("steps");

            if (!jsonMainArr.get(jsonMainArr.size()-1).equals("SA"))
                pass = false;
            else
            {
                for(int i=0; i<jsonMainArr.size()-1;i++)
                {
                    node_map = valid_template_map.get(jsonMainArr.get(i));

                    next_hop = jsonMainArr.getString(i + 1);
                    //System.out.println("for node: "+jsonMainArr.get(i)+" next_hop value: "+next_hop);

                    if (node_map.contains(next_hop))
                    {
                        pass = true;
                        if (i == jsonMainArr.size() - 2)
                            break;
                    }
                    else
                    {
                        pass = false;
                        break;
                    }
                }
            }
            if (pass)
            {
                happy_calls++;
                if (pass_map.containsKey(jsonMainArr))
                    pass_map.put(jsonMainArr, pass_map.get(jsonMainArr)+1);
                else
                    pass_map.put(jsonMainArr, (long) 1);
            }
            else
            {
                unhappy_calls++;
                if (failure_map.containsKey(jsonMainArr))
                    failure_map.put(jsonMainArr, failure_map.get(jsonMainArr)+1);
                else
                    failure_map.put(jsonMainArr, (long) 1);
            }
        }
        System.out.println("Total Calls: "+total_calls+"\n");
        System.out.println("Failure map: "+failure_map+"\n");
        System.out.println("Unhappy Calls: "+unhappy_calls+"\n");
        System.out.println("Pass map: "+pass_map+"\n");
        System.out.println("Happy Calls: "+happy_calls+"\n");
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public Action getProjectAction(final AbstractProject<?,?> project) {
        return new APICoverageProjectAction(project, template);
    }

    /**
     * Descriptor for {@link APICoveragePluginPublisher}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/APICoveragePluginPublisher/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private boolean useFrench;

        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         *      <p>
         *      Note that returning {@link FormValidation#error(String)} does not
         *      prevent the form from being saved. It just means that a message
         *      will be displayed to the user. 
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Say hello world: Pritiiiii";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            useFrench = formData.getBoolean("useFrench");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        public boolean getUseFrench() {
            return useFrench;
        }
    }
}

