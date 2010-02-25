package com.thoughtworks.jestr.jpda;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Method;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.event.*;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;

public class JpdaTracer extends Thread {
    private VirtualMachine vm;
    private Collector collector;
    List<String> classFilters = new ArrayList<String>();

    public JpdaTracer(VirtualMachine vm, Collector collector) {
        super("Jestr trace thread");
        this.vm = vm;
        this.collector = collector;
    }

    public void setup() {
        EventRequestManager mgr = vm.eventRequestManager();

        MethodEntryRequest menr = mgr.createMethodEntryRequest();
        for (String classFilter : classFilters) {
            menr.addClassFilter(classFilter);
        }
        menr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        menr.enable();
    }

    public void addClassFilter(String classFilter) {
        classFilters.add(classFilter);
    }

    public void run() {
        setup();

        EventQueue queue = vm.eventQueue();
        while (true) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator it = eventSet.eventIterator();
                while (it.hasNext()) {
                    handleEvent(it.nextEvent());
                }
                eventSet.resume();
            } catch (InterruptedException exc) {
                // Ignore
            } catch (VMDisconnectedException discExc) {
                handleDisconnectedException();
                break;
            }
        }


    }

    private void handleEvent(Event event) {
        if (event instanceof MethodEntryEvent) {
            Method method = ((MethodEntryEvent) event).method();
            collector.onMethod(method.declaringType().name(), method.name());
        }
    }

    private void handleDisconnectedException() {
    }

    static void redirect(final InputStream in, final PrintStream out) {
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

    public static void trace(String commandLine, Collector collector, String... classFilters) throws IOException, IllegalConnectorArgumentsException, VMStartException, InterruptedException {
        if (classFilters.length == 0) {
            throw new RuntimeException("At least one classFilter is required. E.g. 'com.foo.*'.");
        }
        LaunchingConnector connector = Bootstrap.virtualMachineManager().defaultConnector();
        Map<String, Connector.Argument> args = connector.defaultArguments();
        Connector.Argument main = args.get("main");
        main.setValue(commandLine);
        VirtualMachine vm = connector.launch(args);
        JpdaTracer tracer = new JpdaTracer(vm, collector);
        for (String classFilter : classFilters) {
            tracer.addClassFilter(classFilter);
        }
        tracer.start();
        Process process = vm.process();
        redirect(process.getErrorStream(), System.err);
        redirect(process.getInputStream(), System.out);
        vm.resume();
        tracer.join();
    }
}
