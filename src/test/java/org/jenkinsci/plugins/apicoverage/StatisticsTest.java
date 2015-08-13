package org.jenkinsci.plugins.apicoverage;

import junit.framework.TestCase;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.*;

/**
 * Created by prit8976 on 8/11/15.
 */
public class StatisticsTest extends TestCase {

    static Map<String, HashSet<String>> testMap = new HashMap<String, HashSet<String>>();
    static HashSet<ArrayList> testPassPaths = new HashSet<ArrayList>();
    static HashSet<ArrayList> testFailPaths = new HashSet<ArrayList>();
    static JSONObject testJObj = new JSONObject();
    static JSONArray testJArr = new JSONArray();
    Statistics s;

    static{
        testMap.put("S0", new HashSet<String>());
        testMap.get("S0").add("main");
        testMap.get("S0").add("execute");
        testMap.put("main", new HashSet<String>());
        testMap.get("main").add("parse");
        testMap.get("main").add("init");
        testMap.get("main").add("printf");
        testMap.get("main").add("cleanup");
        testMap.put("execute", new HashSet<String>());
        testMap.get("execute").add("make_string");
        testMap.get("execute").add("printf");
        testMap.get("execute").add("compare");
        testMap.put("init", new HashSet<String>());
        testMap.get("init").add("make_string");
        testMap.put("make_string", new HashSet<String>());
        testMap.get("make_string").add("SA");

        ArrayList testAL = new ArrayList();
        ArrayList testAL1 = new ArrayList();

        testAL.add("S0");testAL.add("main");testAL.add("init");testAL.add("make_string");testAL.add("SA");
        testAL1.add("S0");testAL1.add("execute");testAL1.add("make_string");testAL1.add("SA");

        testPassPaths.add(testAL);testPassPaths.add(testAL1);

        ArrayList testAL2 = new ArrayList();
        ArrayList testAL3 = new ArrayList();
        ArrayList testAL4 = new ArrayList();
        ArrayList testAL5 = new ArrayList();
        ArrayList testAL6 = new ArrayList();

        testAL2.add("S0");testAL2.add("main");testAL2.add("parse");
        testAL3.add("S0");testAL3.add("main");testAL3.add("cleanup");
        testAL4.add("S0");testAL4.add("main");testAL4.add("printf");
        testAL5.add("S0");testAL5.add("execute");testAL5.add("printf");
        testAL6.add("S0");testAL6.add("execute");testAL6.add("compare");

        testFailPaths.add(testAL2);testFailPaths.add(testAL3);testFailPaths.add(testAL4);
        testFailPaths.add(testAL5);testFailPaths.add(testAL6);

        testJArr.add("[\"S0\", \"execute\", \"make_string\", \"SA\"]");
        testJArr.add("[\"S0\", \"execute\", \"make_string\", \"SA\"]");
        testJArr.add("[\"S0\", \"execute\", \"make_string\", \"SA\"]");
        testJArr.add("[\"S0\", \"main\", \"parse\"]");
        testJArr.add("[\"S0\", \"main\", \"parse\"]");
        testJArr.add("[\"S0\", \"execute\", \"compare\"]");

        testJObj.put("steps", testJArr);
    }

    String template = "digraph G {\n" +
            "S0->main;\n" +
            "S0->execute;\n" +
            "main->parse;\n" +
            "main->init;\n" +
            "main->cleanup;\n" +
            "execute->make_string;\n" +
            "execute->printf\n" +
            "init->make_string;\n" +
            "main->printf;\n" +
            "execute->compare;\n" +
            "make_string->SA;\n" +
            "}";

    @Test
    public void testCreateNextHopMap () {
        s = new Statistics();
        s.createNextHopMap(template);

        assertNotNull(s.getNextHopMap());
        assertEquals(testMap,s.getNextHopMap());
    }

    @Test
    public void testGetAllPaths () {
        s = new Statistics();
        s.setAllFailingPaths(new HashSet<ArrayList>());
        s.setAllPassingPaths(new HashSet<ArrayList>());

        s.setNextHopMap(testMap);

        s.getAllPaths("S0", new ArrayList());

        assertEquals(s.getAllFailingPaths().size(), 5);
        assertEquals(s.getAllPassingPaths().size(), 2);

        assertEquals(testFailPaths,s.getAllFailingPaths());
        assertEquals(testPassPaths,s.getAllPassingPaths());
    }

    @Test
    public void testGetStatisticsObject() {
        s = new Statistics();
        s.getStatisticsObject(testJObj, template);

        assertEquals(s.getPercentHappy(), 50.0);
        assertEquals(s.getPercentUnhappy(),40.0);

    }
}