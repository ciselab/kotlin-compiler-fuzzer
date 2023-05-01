package org.fuzzer.search;

import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.chromosome.CodeSnippet;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.clustering.*;
import org.fuzzer.search.fitness.SOFitnessFunction;
import org.fuzzer.search.operators.recombination.RecombinationOperator;
import org.fuzzer.search.operators.selection.SelectionOperator;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class DiversityGA extends GA {

    public DiversityGA(SyntaxNode nodeToSample, Long timeBudgetMilis,
                       Context rootContext, Long seed,
                       Long populationSize,
                       SOFitnessFunction fitnessFunction,
                       SelectionOperator selectionOperator,
                       RecombinationOperator recombinationOperator,
                       ClusteringEngine<CodeBlock> clusteringEngine) {
        super(nodeToSample, timeBudgetMilis, rootContext, seed, populationSize, fitnessFunction,
                selectionOperator, recombinationOperator, clusteringEngine);
    }

    @Override
    public List<Tuple<CodeFragment, FuzzerStatistics>> search() {
        startGlobalStats();
        List<CodeBlock> pop = getRandomBlocks(populationSize);
        List<CodeBlock> parents;

        while (!exceededTimeBudget()) {

            Long numberOfSelections = populationSize / 4L;

            if (clusteringEngine != null) {
                throw new IllegalArgumentException("Clustering is not supported yet.");
//                clusteringEngine.updatePoints(getPoints(pop));
//                List<Cluster<CodeBlock>> clusters = clusteringEngine.cluster();
//
//                for (Cluster<CodeBlock> cluster : clusters) {
//                    List<CodeBlock> pointsOfCluster = cluster.points().stream().map(Point::data).toList();
//                    Long numberOfClusterSelections = (((double) pointsOfCluster.size()) / )
//
//                    List<CodeBlock> selectedPoints = selectionOperator.select(pointsOfCluster, )
//                }
            } else {
                parents = selectionOperator.select(pop, numberOfSelections);
            }

            List<CodeBlock> children = getChildren(parents);
            List<CodeBlock> newBlocks = getRandomBlocks(populationSize - children.size() - parents.size());

            updatePopulation(pop, parents, children, newBlocks);
        }

        return pop.stream().map(block -> new Tuple<>(block.getText(), block.getStats())).toList();
    }

    private List<Point<CodeBlock>> getPoints(List<CodeBlock> population) {
        return population.stream().map(block -> new Point<>(block, block.getStats().getVisitations())).toList();
    }
}
