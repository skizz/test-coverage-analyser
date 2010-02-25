package com.thoughtworks.jestr.jpda;

import sun.reflect.FieldAccessor;

import java.util.*;

public class CoverageCollector implements Collector {
    private String inTest = null;
    private Map<String, Set<String>> testers = new HashMap<String, Set<String>>();

    public void onMethod(String className, String methodName) {
        if (isATest(className)) {
            inTest = className;
            return;
        }
        if (inTest==null) return;
        Set<String> testersOfClass = testers.get(className);
        if (testersOfClass == null) {
            testersOfClass = new HashSet<String>();
            testers.put(className, testersOfClass);
        }
        testersOfClass.add(inTest);
    }

    private boolean isATest(String className) {
        return className.endsWith("Test");
    }

    public Set<String> testersOf(String className) {
        Set<String> testersOfClass = testers.get(className);
        if (testersOfClass == null) return Collections.emptySet();
        return testersOfClass;
    }

    public String report() {
        StringBuilder report = new StringBuilder();
        report.append("Class, Tester\n");
        for (Map.Entry<String, Set<String>> entry : testers.entrySet()) {
            for (String tester : entry.getValue()) {
                report.append(entry.getKey()).append(", ");
                report.append(tester);
                report.append("\n");
            }
        }
        return report.toString();  
    }
}
