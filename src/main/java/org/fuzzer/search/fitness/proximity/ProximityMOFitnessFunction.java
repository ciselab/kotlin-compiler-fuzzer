package org.fuzzer.search.fitness.proximity;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.fitness.MOFitnessFunction;
import org.fuzzer.search.fitness.SOFitnessFunction;
import org.fuzzer.utils.RequestMaker;

import java.net.URL;
import java.util.*;

public class ProximityMOFitnessFunction extends RemoteEvaluation implements MOFitnessFunction {
    private final double [][] targets;

    public ProximityMOFitnessFunction(URL singleRequestURL, URL multipleRequestURL,
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
    }

    @Override
    public double[] evaluate(CodeBlock block) {
        double[] distances = new double[targets.length];
        int i = 0;

        if (!isCached(block)) {
            double[] newEmbedding = getEmbedding(block);
            addToCache(block, newEmbedding);
        }


        for (double[] target : targets) {
            distances[i++] = distanceToTarget(block, target);
        }

        return distances;
    }
}

