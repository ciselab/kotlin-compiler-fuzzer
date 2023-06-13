package org.fuzzer.search.fitness.proximity;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.TestSuite;
import org.fuzzer.utils.RequestMaker;

import java.net.URL;
import java.util.*;

public class CollectiveProximityFitnessFunction extends RemoteEvaluation implements SOPopulationFitnessFunction {


    private final double[][] targets;

    public CollectiveProximityFitnessFunction(URL singleRequestURL, URL multipleRequestURL,
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

    public double[][] getTargets() {
        return targets;
    }

    private double distanceToTargets(Collection<CodeBlock> codeBlocks) {
        double sum = 0.0;

        for (double[] target : targets) {
            sum += distanceToTarget(codeBlocks, target);
        }

        return sum;
    }

    public double evaluate(Collection<CodeBlock> blocks) {
        List<CodeBlock> unevaluatedBlocks = blocks.stream()
                .filter(b -> !isCached(b))
                .toList();
        Map<CodeBlock, double[]> newEmbeddings = getEmbeddings(unevaluatedBlocks);
        addToCache(newEmbeddings);

        return distanceToTargets(blocks);
    }

    @Override
    public double evaluate(TestSuite blocks) {
        return evaluate(blocks.getBlocks());
    }
}
