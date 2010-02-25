package com.foo.sample;

import org.junit.Test;

public class SampleTest {
    @Test
    public void shouldCountToTen() {
        Sample sample = new Sample();
        sample.countTo(10);
    }
}
