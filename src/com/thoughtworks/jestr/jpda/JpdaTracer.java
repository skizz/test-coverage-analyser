package com.thoughtworks.jestr.jpda;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Method;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.event.*;

import java.util.Set;

public class JpdaTracer extends Thread {
    private VirtualMachine vm;
    private CoverageCollector collector;

    public JpdaTracer(VirtualMachine vm) {
        super("Jestr trace thread");
        this.vm = vm;
        this.collector = new CoverageCollector();
    }

    public void setup() {
        EventRequestManager mgr = vm.eventRequestManager();

        MethodEntryRequest menr = mgr.createMethodEntryRequest();
        menr.addClassFilter("com.foo.*");
//        for (int i=0; i<excludes.length; ++i) {
//            menr.addClassExclusionFilter(excludes[i]);
//        }
        menr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        menr.enable();
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
            onMethodEntry(method.declaringType().name(), method.name());
        }
    }

    private void onMethodEntry(String className, String methodName) {
        collector.onMethod(className, methodName);
    }

    private void handleDisconnectedException() {
    }

    public Set<String> testersOf(String clazz) {
        return collector.testersOf(clazz);
    }
}
