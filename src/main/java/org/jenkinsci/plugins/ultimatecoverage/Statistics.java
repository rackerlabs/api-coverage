package org.jenkinsci.plugins.ultimatecoverage;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by prit8976 on 8/4/15.
 */
public class Statistics {

    private long totalPassingCalls;
    private long totalFailingCalls;
    private long allPasses;
    private long allFailures;
    private long uniqueFailures;
    private long uniquePasses;
    private double percentHappy;
    private double percentUnhappy;

    public Map<String, HashSet<String>> getNextHopMap() {
        return nextHopMap;
    }

    public void setNextHopMap(Map<String, HashSet<String>> nextHopMap) {
        this.nextHopMap = nextHopMap;
    }

    private Map<String, HashSet<String>> nextHopMap;

    public HashSet<ArrayList> getAllPassingPaths() {
        return allPassingPaths;
    }

    public HashSet<ArrayList> getAllFailingPaths() {
        return allFailingPaths;
    }

    public void setAllPassingPaths(HashSet<ArrayList> allPassingPaths) {
        this.allPassingPaths = allPassingPaths;
    }

    public void setAllFailingPaths(HashSet<ArrayList> allFailingPaths) {
        this.allFailingPaths = allFailingPaths;
    }

    private HashSet<ArrayList> allPassingPaths;
    private HashSet<ArrayList> allFailingPaths;

    public long getAllPasses() {
        return allPasses;
    }

    public long getAllFailures() {
        return allFailures;
    }

    public double getPercentUnhappy() {
        return Math.round(this.percentUnhappy * 100) / 100;
    }

    public long getUniquePasses() {
        return this.uniquePasses;
    }

    public long getTotalPassingCalls() {
        return this.totalPassingCalls;
    }

    public long getTotalFailingCalls() {
        return totalFailingCalls;
    }

    public long getUniqueFailures() {
        return this.uniqueFailures;
    }

    public double getPercentHappy() {
        return Math.round(this.percentHappy * 100) / 100;
    }

    public void createNextHopMap(String template) {

        Pattern logEntry = Pattern.compile("[^;|\\{|\t|\n|\\s" +
                "]*->(.*?)[^\\s|\t|\n" +
                "|;]*");
        Matcher matchPattern = logEntry.matcher(template);

        nextHopMap = new HashMap<String, HashSet<String>>();
        HashSet<String> hs;
        String[] str;

        while (matchPattern.find()) {
            str = matchPattern.group().split("->");
            str[0] = str[0].trim();
            str[1] = str[1].trim();
            if (nextHopMap.containsKey(str[0]))
                if (nextHopMap.get(str[0]).contains(str[1]))
                    continue;
                else nextHopMap.get(str[0]).add(str[1]);
            else {
                hs = new HashSet<String>();
                hs.add(str[1]);
                nextHopMap.put(str[0], hs);
            }
        }
    }

    public Statistics getStatisticsObject(JSONObject jobj_path, String template) {
        createNextHopMap(template);

        Map<JSONArray, Long> failure_map = new HashMap<JSONArray, Long>();
        Map<JSONArray, Long> pass_map = new HashMap<JSONArray, Long>();

        JSONArray jsonMainArr1 = jobj_path.getJSONArray("steps");
        JSONArray jsonMainArr;
        boolean pass = false;
        String next_hop;
        HashSet<String> node_map;

        for (int j = 0; j < jsonMainArr1.size(); j++) {

            jsonMainArr = JSONArray.fromObject(jsonMainArr1.get(j));

            if (!jsonMainArr.get(jsonMainArr.size() - 1).equals("SA"))
                pass = false;
            else {
                for (int i = 0; i < jsonMainArr.size() - 1; i++) {
                    node_map = nextHopMap.get(jsonMainArr.get(i));

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
                allPasses++;
                if (pass_map.containsKey(jsonMainArr))
                    pass_map.put(jsonMainArr, pass_map.get(jsonMainArr) + 1);
                else {
                    uniquePasses++;
                    pass_map.put(jsonMainArr, (long) 1);
                }
            } else {
                allFailures++;
                if (failure_map.containsKey(jsonMainArr))
                    failure_map.put(jsonMainArr, failure_map.get(jsonMainArr) + 1);
                else {
                    uniqueFailures++;
                    failure_map.put(jsonMainArr, (long) 1);
                }
            }
        }

        allPassingPaths = new HashSet<ArrayList>();
        allFailingPaths = new HashSet<ArrayList>();

        getAllPaths("S0", new ArrayList());

        this.percentHappy = ((double) uniquePasses / (double) totalPassingCalls) * 100.0;
        this.percentUnhappy = ((double) uniqueFailures / (double) totalFailingCalls) * 100.0;

        return this;
    }

    public void getAllPaths(String root, ArrayList path) {

        path.add(root);

        if (nextHopMap.get(root) == null || nextHopMap.get(root).contains(root)) {
            if (root.equals("SA")) {
                allPassingPaths.add(path);
                totalPassingCalls++;
            } else {
                allFailingPaths.add(path);
                totalFailingCalls++;
            }
        } else
            for (String nextHop : nextHopMap.get(root))
                getAllPaths(nextHop, new ArrayList(path));
    }


}