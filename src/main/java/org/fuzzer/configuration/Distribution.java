package org.fuzzer.configuration;

public record Distribution<T>(DistributionType distributionType, T lowerBound, T upperBound) {


}
