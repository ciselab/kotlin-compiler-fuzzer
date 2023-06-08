package org.fuzzer.search.clustering;

import org.fuzzer.search.fitness.DistanceMetric;

import java.util.Arrays;
import java.util.Objects;

public record Point<T>(T data, double [] coordinates) {
    public int getDimension() {
        return coordinates.length;
    }

    public double distance(Point<T> other, DistanceMetric distanceMetric) {
        return Point.distance(this.coordinates(), other.coordinates(), distanceMetric);
    }

    public static double distance(double[] p1, double[] p2, DistanceMetric distanceMetric) {
        double sum = 0.0;

        switch (distanceMetric) {
            case EUCLIDEAN:
                for (int i = 0; i < p1.length; i++) {
                    double diff = p1[i] - p2[i];
                    sum += diff * diff;
                }
                return Math.sqrt(sum);
            case MANHATTAN:
                for (int i = 0; i < p1.length; i++) {
                    sum += Math.abs(p1[i] - p2[i]);
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
