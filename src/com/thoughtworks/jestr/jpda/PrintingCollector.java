package com.thoughtworks.jestr.jpda;

public class PrintingCollector implements Collector {
    public void onMethod(String className, String methodName) {
        System.out.println("Method: " + className + " -> " + methodName);
    }
}
