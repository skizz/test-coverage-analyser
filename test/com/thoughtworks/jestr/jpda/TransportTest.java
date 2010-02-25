package com.thoughtworks.jestr.jpda;

import org.junit.Test;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

import java.io.IOException;

public class TransportTest {

    @Test
    public void shouldBeAbleToMonitorMethodCalls() throws VMStartException, IllegalConnectorArgumentsException, IOException, InterruptedException {
        Collector collector = new PrintingCollector();
        String commandLine = "-cp lib/junit-4.8.1.jar:out/production/sample:out/test/sample org.junit.runner.JUnitCore com.foo.sample.SampleTest";
        JpdaTracer.trace(commandLine, collector, "com.foo.*");
    }

    @Test
    public void shouldRecordTestersOfAClass() throws VMStartException, IllegalConnectorArgumentsException, IOException, InterruptedException {
        String commandLine = "-cp lib/junit-4.8.1.jar:out/production/sample:out/test/sample org.junit.runner.JUnitCore com.foo.sample.SampleTest";
        CoverageCollector collector = new CoverageCollector();
        JpdaTracer.trace(commandLine, collector, "com.foo.*");
        assertThat(collector.testersOf("com.foo.sample.Sample"), hasItem("com.foo.sample.SampleTest"));
    }

    @Test
    public void shouldRequireClassFilterToBeSet() throws VMStartException, IllegalConnectorArgumentsException, IOException, InterruptedException {
        String commandLine = "anything";
        Collector collector = new CoverageCollector();
        try {
            JpdaTracer.trace(commandLine, collector);
            fail("Should throw Exception");
        }
        catch (Exception e) {
            assertThat(e.getMessage(), containsString("At least one classFilter is required. E.g. 'com.foo.*'."));
        }
    }

}
