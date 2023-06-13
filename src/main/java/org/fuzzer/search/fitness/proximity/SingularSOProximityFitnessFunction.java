package org.fuzzer.search.fitness.proximity;

import org.fuzzer.search.archive.ProximityArchive;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.clustering.Point;
import org.fuzzer.search.fitness.DistanceMetric;
import org.fuzzer.search.fitness.SOFitnessFunction;
import org.fuzzer.utils.RequestMaker;

import java.net.URL;
import java.util.*;

public class SingularSOProximityFitnessFunction extends RemoteEvaluation implements SOFitnessFunction {
    private final double[][] targets;

    private int counter;

    private final ProximityArchive archive;

    private final double[] lastDistances;

    public SingularSOProximityFitnessFunction(URL singleRequestURL, URL multipleRequestURL,
                                              URL targetURL, int numberOfTargets) {
        super(new RequestMaker(singleRequestURL), new RequestMaker(multipleRequestURL));



        String embeddingsString = new RequestMaker(targetURL).executeGetReq("");
        double[][] initialTargets = processNestedLists(embeddingsString);

        this.targets = new double[numberOfTargets][];

        List<double[]> ts = new ArrayList<>(Arrays.stream(initialTargets).toList());
        Collections.shuffle(ts);

        int i = 0;
        for (double[] embedding : ts) {
            this.targets[i++] = embedding;
        }

        this.counter = 0;
        this.archive = new ProximityArchive(targets);
        this.lastDistances = new double[numberOfTargets];
    }

    public void switchTargets() {
        this.counter = (this.counter + 1) % targets.length;
    }

    public boolean coveredAllTargets() {
        return counter >= targets.length;
    }

    public ProximityArchive getArchive() {
        return archive;
    }

    @Override
    public double evaluate(CodeBlock individual) {
        if (!isCached(individual)) {
            double[] e = getEmbedding(individual);
            addToCache(individual, e);
        }

        double[] e = getFromCache(individual);

        for (int i = 0; i < targets.length; i++) {
            lastDistances[i] = Point.distance(e, targets[i], DistanceMetric.EUCLIDEAN);
        }

        archive.processEntry(individual, lastDistances);
        return distanceToTarget(List.of(individual), targets[counter]);
    }

    @Override
    public void updatePopulation(List<CodeBlock> population) {
    }
}
