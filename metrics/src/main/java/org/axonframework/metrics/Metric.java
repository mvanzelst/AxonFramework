package org.axonframework.metrics;

public class Metric<T extends Number> {


    private

    void set(T value);

    void reset();

    void increment(T delta);
}
