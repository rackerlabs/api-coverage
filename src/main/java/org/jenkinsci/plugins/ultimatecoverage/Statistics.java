package org.jenkinsci.plugins.ultimatecoverage;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by prit8976 on 8/4/15.
 */
public class Statistics {

    private long totalCalls;
    private long failures;
    private long passes;
    private double percentHappy;
    private double percentUnhappy;

    public double getPercentUnhappy() {
        return (double)Math.round(this.percentUnhappy * 100) / 100;
    }

    public long getPasses() {
        return this.passes;
    }

    public long getTotalCalls() {
        return this.totalCalls;
    }

    public long getFailures() {
        return this.failures;
    }

    public double getPercentHappy() {
        return (double)Math.round(this.percentHappy * 100) / 100;
    }

    public Map<String, HashSet<String>> getValidTemplateMap(String template) {
        Pattern logEntry = Pattern.compile("[^;|\\{|\t|\n|\\s" +
                "]*->(.*?)[^\\s|\t|\n" +
                "|;]*");
        Matcher matchPattern = logEntry.matcher(template);

        Map<String, HashSet<String>> valid_template_map = new HashMap<String, HashSet<String>>();
        HashSet<String> hs;
        String[] str;

        while (matchPattern.find()) {
            str = matchPattern.group().split("->");
            if (valid_template_map.containsKey(str[0]))
                if (valid_template_map.get(str[0]).contains(str[1]))
                    continue;
                else valid_template_map.get(str[0]).add(str[1]);
            else {
                hs = new HashSet<String>();
                hs.add(str[1]);
                valid_template_map.put(str[0], hs);
            }
        }

        System.out.println(valid_template_map);
        return valid_template_map;
    }

    public Statistics getStats(JSONObject jobj_path, String template)
    {
        Map<String, HashSet<String>> valid_template_map = getValidTemplateMap(template);

        Map<JSONArray, Long> failure_map = new HashMap<JSONArray, Long>();
        Map<JSONArray, Long> pass_map = new HashMap<JSONArray, Long>();

        JSONArray jsonMainArr1 = jobj_path.getJSONArray("steps");
        JSONArray jsonMainArr;
        boolean pass = false;
        String next_hop;
        HashSet<String> node_map;

        long total_calls = 0;
        long happy_calls = 0;
        long unhappy_calls = 0;

        for (int j = 0; j < jsonMainArr1.size(); j++) {
            total_calls++;

            jsonMainArr = JSONArray.fromObject(jsonMainArr1.get(j));

            if (!jsonMainArr.get(jsonMainArr.size() - 1).equals("SA"))
                pass = false;
            else {
                for (int i = 0; i < jsonMainArr.size() - 1; i++) {
                    node_map = valid_template_map.get(jsonMainArr.get(i));

                    next_hop = jsonMainArr.getString(i + 1);

                    if (node_map.contains(next_hop)) {
                        pass = true;
                        if (i == jsonMainArr.size() - 2)
                            break;
                    } else {
                        pass = false;
                        break;
                    }
                }
            }
            if (pass) {
                happy_calls++;
                if (pass_map.containsKey(jsonMainArr))
                    pass_map.put(jsonMainArr, pass_map.get(jsonMainArr) + 1);
                else
                    pass_map.put(jsonMainArr, (long) 1);
            } else {
                unhappy_calls++;
                if (failure_map.containsKey(jsonMainArr))
                    failure_map.put(jsonMainArr, failure_map.get(jsonMainArr) + 1);
                else
                    failure_map.put(jsonMainArr, (long) 1);
            }
        }

        System.out.println("failure map: "+failure_map);
        System.out.println("pass map"+pass_map);
//        System.out.println("Total Calls: "+total_calls+"\n");
//        System.out.println("Failure map: "+failure_map+"\n");
//        System.out.println("Unhappy Calls: "+unhappy_calls+"\n");
//        System.out.println("Pass map: "+pass_map+"\n");
//        System.out.println("Happy Calls: "+happy_calls+"\n");

        this.failures = unhappy_calls;
        this.passes = happy_calls;
        this.totalCalls = total_calls;
        this.percentHappy = ((double) happy_calls / (double) total_calls) * 100.0;
        this.percentUnhappy = ((double) unhappy_calls / (double) total_calls) * 100.0;

        return this;
    }
}
