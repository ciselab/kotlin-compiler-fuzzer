package org.fuzzer.search.clustering;

import org.fuzzer.search.fitness.DistanceMetric;

import java.util.Arrays;
import java.util.Objects;

public record Point<T>(T data, double [] coordinates) {
    public int getDimension() {
        return coordinates.length;
    }

    public double distance(Point other, DistanceMetric distanceMetric) {
        double sum = 0.0;

        switch (distanceMetric) {
            case EUCLIDEAN:
                for (int i = 0; i < coordinates.length; i++) {
                    double diff = coordinates[i] - other.coordinates[i];
                    sum += diff * diff;
                }
                return Math.sqrt(sum);
            case MANHATTAN:
                for (int i = 0; i < coordinates.length; i++) {
                    sum += Math.abs(coordinates[i] - other.coordinates[i]);
                }
                return sum;
            default:
                throw new IllegalArgumentException("Unsupported distance metric: " + distanceMetric);
        }
    }

    public double getCoordinate(int i) {
        return coordinates[i];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point<?> point)) return false;

        if (!Objects.equals(data, point.data)) return false;
        return Arrays.equals(coordinates, point.coordinates);
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(coordinates);
        return result;
    }
}
