package com.thoughtworks.jestr.jpda;

import org.junit.Test;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import org.hamcrest.core.Is;
import static org.hamcrest.core.Is.is;

public class CoverageCollectorTest {

    @Test
    public void shouldRememberWhoCoversAClass() {
        CoverageCollector coverageCollector = new CoverageCollector();
        coverageCollector.onMethod("com.foo.sample.SampleTest", "<init>");
        coverageCollector.onMethod("com.foo.sample.SampleTest", "shouldDoSomethingFunky");
        coverageCollector.onMethod("com.foo.sample.Sample", "countTo");

        assertThat(coverageCollector.testersOf("com.foo.sample.Sample"), hasItem("com.foo.sample.SampleTest"));
    }

    @Test
    public void shouldReturnEmptySetIfNoTesters() {
        CoverageCollector coverageCollector = new CoverageCollector();

        assertThat(coverageCollector.testersOf("com.foo.sample.Sample").size(), is(0));
    }

    @Test
    public void shouldNotRecordTestersIfNotInATest() {
        CoverageCollector coverageCollector = new CoverageCollector();
        coverageCollector.onMethod("com.foo.sample.Sample", "countTo");
        
        assertThat(coverageCollector.testersOf("com.foo.sample.Sample").size(), is(0));
    }

    @Test
    public void shouldReportWhatTestsCoverEachClass() {
        CoverageCollector coverageCollector = new CoverageCollector();
        coverageCollector.onMethod("com.foo.sample.SampleTest", "shouldDoSomethingFunky");
        coverageCollector.onMethod("com.foo.sample.Sample", "countTo");
        coverageCollector.onMethod("com.foo.sample.AnotherTest", "shouldDoSomethingFunky");
        coverageCollector.onMethod("com.foo.sample.Sample", "countTo");

        assertThat(coverageCollector.report(), containsString("Class, Tester"));
        assertThat(coverageCollector.report(), containsString("com.foo.sample.Sample, com.foo.sample.SampleTest"));
        assertThat(coverageCollector.report(), containsString("com.foo.sample.Sample, com.foo.sample.AnotherTest"));
    }
}
