package org.fuzzer.search.clustering;

import org.fuzzer.search.fitness.DistanceMetric;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DivisiveClustering<T> extends ClusteringEngine<T> {
    private Map<Cluster<T>, Double> diameters;
    public DivisiveClustering(List<Point<T>> pointList, DistanceMetric distanceMetric, Linkage linkage) {
        super(pointList, distanceMetric, linkage);

        diameters = new HashMap<>();
    }

    @Override
    public List<Cluster<T>> cluster(Linkage linkage) {
        List<Cluster<T>> currentSplit = new java.util.ArrayList<>(List.of(new Cluster<>(points)));

        diameters.put(currentSplit.get(0), computeDiameter(currentSplit.get(0)));

        List<Cluster<T>> largestJumpSplit = List.copyOf(currentSplit);
        double largestJump = Double.MIN_VALUE;

        while (currentSplit.stream().anyMatch(c -> diameters.get(c) > 0)) {
            Cluster<T> maxDiameterCluster = findMaxDistanceCluster(currentSplit);
            Point<T> mostDissimilarPoint = findMostDissimilarPoint(maxDiameterCluster);

            List<Point<T>> pointsInOriginalCluster = new LinkedList<>();
            List<Point<T>> pointsInNewCluster = new LinkedList<>();

            Point<T> oldCenter = findCenter(maxDiameterCluster);

            for (Point<T> point : maxDiameterCluster.points()) {
                if (distanceBetween(point, mostDissimilarPoint) < distanceBetween(point, oldCenter)) {
                    pointsInNewCluster.add(point);
                } else {
                    pointsInOriginalCluster.add(point);
                }
            }

            Cluster<T> originalCluster = new Cluster<>(pointsInOriginalCluster);
            Cluster<T> newCluster = new Cluster<>(pointsInNewCluster);

            diameters.remove(maxDiameterCluster);
            diameters.put(originalCluster, computeDiameter(originalCluster));
            diameters.put(newCluster, computeDiameter(newCluster));

            currentSplit.remove(maxDiameterCluster);
            currentSplit.add(newCluster);
            currentSplit.add(originalCluster);

            double jump = distanceBetween(originalCluster, newCluster, linkage);

            if (jump > largestJump) {
                largestJump = jump;
                largestJumpSplit = List.copyOf(currentSplit);
            }
        }

        return largestJumpSplit;
    }

    private double computeDiameter(Cluster<T> cluster) {
        double diameter = Double.MIN_VALUE;
        for (Point<T> point : cluster.points()) {
            for (Point<T> otherPoint : cluster.points()) {
                double distance = distanceBetween(point, otherPoint);
                if (distance > diameter) {
                    diameter = distance;
                }
            }
        }
        return diameter;
    }

    private Cluster<T> findMaxDistanceCluster(List<Cluster<T>> clusters) {
        double maxDistance = Double.MIN_VALUE;
        Cluster<T> maxDistanceCluster = null;

        for (Cluster<T> cluster : clusters) {
            double distance = diameters.get(cluster);
            if (distance > maxDistance) {
                maxDistance = distance;
                maxDistanceCluster = cluster;
            }
        }
        return maxDistanceCluster;
    }

    private Point<T> findMostDissimilarPoint(Cluster<T> cluster) {
        double maxDistance = Double.MIN_VALUE;
        Point<T> maxDistancePoint = null;

        for (Point<T> point : cluster.points()) {
            double distance = 0;
            for (Point<T> otherPoint : cluster.points()) {
                distance += distanceBetween(point, otherPoint);
            }
            if (distance > maxDistance) {
                maxDistance = distance;
                maxDistancePoint = point;
            }
        }
        return maxDistancePoint;
    }

    private Point<T> findCenter(Cluster<T> cluster) {
        double[] center = new double[cluster.points().get(0).coordinates().length];
        for (Point<T> point : cluster.points()) {
            for (int i = 0; i < point.coordinates().length; i++) {
                center[i] += point.coordinates()[i];
            }
        }
        for (int i = 0; i < center.length; i++) {
            center[i] /= cluster.points().size();
        }
        return new Point<>(null, center);
    }
}
