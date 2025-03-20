package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class SummaryTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Summary getSummarySample1() {
        return new Summary().id(1L).periodValue("periodValue1");
    }

    public static Summary getSummarySample2() {
        return new Summary().id(2L).periodValue("periodValue2");
    }

    public static Summary getSummaryRandomSampleGenerator() {
        return new Summary().id(longCount.incrementAndGet()).periodValue(UUID.randomUUID().toString());
    }
}
