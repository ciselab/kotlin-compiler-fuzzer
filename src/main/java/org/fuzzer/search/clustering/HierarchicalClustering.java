package org.fuzzer.search.clustering;

import org.fuzzer.search.fitness.DistanceMetric;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalClustering<T> extends ClusteringEngine<T> {

    public HierarchicalClustering(List<Point<T>> points, DistanceMetric distanceMetric) {
        super(points, distanceMetric);
    }

    @Override
    public List<Cluster<T>> cluster(Linkage linkage) {
        List<Cluster<T>> currentSplit = new ArrayList<>(points.stream().map(p -> new Cluster<>(List.of(p))).toList());
        List<Cluster<T>> largestJumpSplit = List.copyOf(currentSplit);

        double largestJump = Double.MIN_VALUE;

        while (currentSplit.size() > 1) {
            double minDistance = Double.MAX_VALUE;
            Cluster<T> c1 = null;
            Cluster<T> c2 = null;

            for (int i = 0; i < currentSplit.size(); i++) {
                for (int j = i + 1; j < currentSplit.size(); j++) {
                    Cluster<T> cluster1 = currentSplit.get(i);
                    Cluster<T> cluster2 = currentSplit.get(j);
                    double distance = distanceBetween(cluster1, cluster2, linkage);
                    if (distance < minDistance) {
                        minDistance = distance;
                        c1 = cluster1;
                        c2 = cluster2;
                    }
                }
            }

            assert c1 != null && c2 != null;
            if (minDistance > largestJump) {
                largestJump = minDistance;
                largestJumpSplit = List.copyOf(currentSplit);
            }

            currentSplit.remove(c1);
            currentSplit.remove(c2);
            currentSplit.add(Cluster.merge(c1, c2));
        }

        return largestJumpSplit;
    }
}
