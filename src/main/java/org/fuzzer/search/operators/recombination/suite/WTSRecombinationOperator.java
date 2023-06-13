package org.fuzzer.search.operators.recombination.suite;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.TestSuite;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.LinkedList;
import java.util.List;

public class WTSRecombinationOperator implements SuiteRecombinationOperator {

    private final RandomNumberGenerator rng;

    public WTSRecombinationOperator(RandomNumberGenerator rng) {
        this.rng = rng;
    }

    @Override
    public Tuple<TestSuite, TestSuite> recombine(TestSuite s1, TestSuite s2) {
        double alpha = rng.fromUniformContinuous(0.0, 1.0);

        int s1InO1 = (int) Math.floor(alpha * s1.size());
        int s1InO2 = s1.size() - s1InO1;

        int s2InO1 = (int) Math.floor(alpha * s2.size());
        int s2InO2 = s2.size() - s2InO1;

        List<CodeBlock> o1Blocks = new LinkedList<>(s1.getBlocks().subList(0, s1InO1));
        o1Blocks.addAll(s2.getBlocks().subList(s2.size() - s2InO2, s2.size()));

        List<CodeBlock> o2Blocks = new LinkedList<>(s2.getBlocks().subList(0, s2InO1));
        o2Blocks.addAll(s1.getBlocks().subList(s1.size() - s1InO2, s1.size()));


        return new Tuple<>(new TestSuite(o1Blocks), new TestSuite(o2Blocks));
    }
}
