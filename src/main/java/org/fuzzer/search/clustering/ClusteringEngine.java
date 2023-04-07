package org.fuzzer.search.clustering;

import org.fuzzer.search.fitness.DistanceMetric;

import java.util.List;

public abstract class ClusteringEngine<T> {
    protected final List<Point<T>> points;

    private final DistanceMetric distanceMetric;

    private final double[][] distanceMatrix;

    public ClusteringEngine(List<Point<T>> points, DistanceMetric distanceMetric) {
        this.points = points;
        this.distanceMetric = distanceMetric;

        distanceMatrix = new double[points.size()][];

        for (int i = 0; i < points.size(); i++) {
            distanceMatrix[i] = new double[points.size()];

            distanceMatrix[i][i] = 0.0;
            for (int j = i + 1; j < points.size(); j++) {
                distanceMatrix[i][j] = points.get(i).distance(points.get(j), distanceMetric);
                distanceMatrix[j][i] = points.get(i).distance(points.get(j), distanceMetric);
            }
        }
    }

    public double distanceBetween(Point<T> p1, Point<T> p2) {
        return distanceMatrix[points.indexOf(p1)][points.indexOf(p2)];
    }

    public double distanceBetween(Cluster<T> c1, Cluster<T> c2, Linkage linkage) {
        switch (linkage) {
            case SINGLE -> {
                double distance = Double.MAX_VALUE;
                for (Point<T> point : c1.points()) {
                    for (Point<T> otherPoint : c2.points()) {
                        distance = Math.min(distance, distanceBetween(point, otherPoint));
                    }
                }
                return distance;
            }
            case COMPLETE -> {
                double distance = Double.MIN_VALUE;
                for (Point<T> point : c1.points()) {
                    for (Point<T> otherPoint : c2.points()) {
                        distance = Math.max(distance, distanceBetween(point, otherPoint));
                    }
                }
                return distance;
            }
            case AVERAGE -> {
                double sum = 0.0;
                for (Point<T> point : c1.points()) {
                    for (Point<T> otherPoint : c2.points()) {
                        sum += distanceBetween(point, otherPoint);
                    }
                }
                return sum / (c1.points().size() * c2.points().size());
            }
            default -> throw new IllegalArgumentException("Unsupported linkage: " + linkage);
        }
    }
    public abstract List<Cluster<T>> cluster(Linkage linkage);
}
