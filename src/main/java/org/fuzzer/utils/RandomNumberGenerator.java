package org.fuzzer.utils;

import org.fuzzer.configuration.DistributionType;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
public class RandomNumberGenerator implements Serializable {
    private final Random random;
    private final long seed;

    public RandomNumberGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    public Double fromUniformContinuous(double lowerBound, double upperBound) {
        return lowerBound + random.nextDouble() * (upperBound - lowerBound);
    }
    public Integer fromUniformDiscrete(int lowerBound, int upperBound) {
        return lowerBound + random.nextInt(1 + upperBound - lowerBound);
    }

    public Integer fromDiscreteDistribution(DistributionType distributionType, long lowerBound, long upperBound) {
        switch (distributionType) {
            case UNIFORM:
                return fromUniformDiscrete((int) lowerBound, (int) upperBound);
            case GEOMETRIC:
                return Math.toIntExact(lowerBound + fromGeometric());
            default:
                throw new IllegalArgumentException("Cannot support distribution: " + distributionType);
        }
    }

    public Double fromContinuousDistribution(DistributionType distributionType, double lowerBound, double upperBound) {
        switch (distributionType) {
            case UNIFORM:
                return fromUniformContinuous(lowerBound, upperBound);
            default:
                throw new IllegalArgumentException("Cannot support distribution: " + distributionType);
        }
    }

    public Long getNewSeed() {
        return random.nextLong();
    }

    public boolean randomBoolean() {
        return fromUniformDiscrete(0, 1) == 1;
    }

    public boolean randomBoolean(Double trueProbability) {
        return trueProbability < fromUniformContinuous(0.0, 1.0);
    }
    public Integer fromGeometric() {
        Integer res = 0;
        while (randomBoolean()) {
            res++;
        }

        return res;
    }

    public long getSeed() {
        return seed;
    }

    public <T> T selectFromList(List<T> list) {
        return list.get(fromUniformDiscrete(0, list.size() - 1));
    }

    public Integer randomNumberPrimitive() {
        return fromUniformDiscrete(0, 255);
    }

    public Byte randomByte() {
        byte[] arr = new byte[1];
        random.nextBytes(arr);
        return arr[0];
    }

    public Double randomDoublePrimitive() {
        return fromUniformContinuous(0.0, 255.0);
    }
}
