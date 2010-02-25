package com.thoughtworks.jestr.jpda;

import org.junit.Test;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.Assert.assertThat;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class TransportTest {

    @Test
    public void shouldBeAbleToMonitorMethodCalls() throws VMStartException, IllegalConnectorArgumentsException, IOException, InterruptedException {
        LaunchingConnector connector = Bootstrap.virtualMachineManager().defaultConnector();
        System.out.println(connector);
        Map<String, Connector.Argument> args = connector.defaultArguments();
        Connector.Argument main = args.get("main");
        main.setValue("-cp lib/junit-4.8.1.jar:out/production/sample:out/test/sample org.junit.runner.JUnitCore com.foo.sample.SampleTest");
        VirtualMachine vm = connector.launch(args);
        JpdaTracer tracer = new JpdaTracer(vm);
        tracer.start();
        Process process = vm.process();
        redirect(process.getErrorStream(), System.err);
        redirect(process.getInputStream(), System.out);
        vm.resume();
        tracer.join();

        assertThat(tracer.testersOf("com.foo.sample.Sample"), hasItem("com.foo.sample.SampleTest"));
    }

    @Test
    public void shouldRecordTestersOfAClass() {
    
    }

    private void redirect(final InputStream in, final PrintStream out) {
        Runnable redirector = new Runnable() {
            public void run() {
                try {
                    byte[] buffer = new byte[1024];
                    int count;
                    while ((count = in.read(buffer, 0, 1024)) >= 0) {
                        out.write(buffer, 0, count);
                    }
                    out.flush();
                } catch (IOException e) {
                    System.err.println("Error redirecting stream: " + e);
                }
            }
        };
        Thread thread = new Thread(redirector, "Redirector");
        thread.start();
    }

}
