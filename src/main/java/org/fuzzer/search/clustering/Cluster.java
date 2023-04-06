package org.fuzzer.search.clustering;

import org.fuzzer.search.fitness.DistanceMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record Cluster<T>(List<Point<T>> points) {

    public static <T> Cluster<T> merge(Cluster<T> c1, Cluster<T> c2) {
        List<Point<T>> points = new ArrayList<>(List.copyOf(c1.points));
        points.addAll(c2.points);
        return new Cluster<>(points);
    }

    // More efficient implementation in the ClusteringEngine class
    public double distance(Cluster<T> other, DistanceMetric distanceMetric, Linkage linkage) {
        switch (linkage) {
            case SINGLE -> {
                double distance = Double.MAX_VALUE;
                for (Point<T> point : points) {
                    for (Point<T> otherPoint : other.points) {
                        distance = Math.min(distance, point.distance(otherPoint, distanceMetric));
                    }
                }
                return distance;
            }
            case COMPLETE -> {
                double distance = Double.MIN_VALUE;
                for (Point<T> point : points) {
                    for (Point<T> otherPoint : other.points) {
                        distance = Math.max(distance, point.distance(otherPoint, distanceMetric));
                    }
                }
                return distance;
            }
            case AVERAGE -> {
                double sum = 0.0;
                for (Point<T> point : points) {
                    for (Point<T> otherPoint : other.points) {
                        sum += point.distance(otherPoint, distanceMetric);
                    }
                }
                return sum / (points.size() * other.points.size());
            }
            default -> throw new IllegalArgumentException("Unsupported linkage: " + linkage);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cluster<?> cluster)) return false;

        return Objects.equals(points, cluster.points);
    }

    @Override
    public int hashCode() {
        return points != null ? points.hashCode() : 0;
    }
}
