package com.thoughtworks.jestr.jpda;

/**
 * Created by IntelliJ IDEA.
 * User: cruise
 * Date: 25 Feb, 2010
 * Time: 10:24:43 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Collector {
    void onMethod(String className, String methodName);
}
