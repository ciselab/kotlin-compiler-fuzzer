package org.fuzzer.configuration;


import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.DiversityGA;
import org.fuzzer.search.RandomSearch;
import org.fuzzer.search.Search;
import org.fuzzer.search.fitness.DistanceMetric;
import org.fuzzer.search.fitness.DiversityFitnessFunction;
import org.fuzzer.search.fitness.FitnessFunction;
import org.fuzzer.search.operators.recombination.RecombinationOperator;
import org.fuzzer.search.operators.recombination.SimpleRecombinationOperator;
import org.fuzzer.search.operators.selection.SelectionOperator;
import org.fuzzer.search.operators.selection.TournamentSelection;
import org.fuzzer.utils.RandomNumberGenerator;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Configuration {

    private final double simplicityBias;

    private DistributionType plusNodeDist;

    private Long plusNodeLb;

    private Long plusNodeUb;

    private DistributionType starNodeDist;

    private Long starNodeLb;

    private Long starNodeUb;

    private final SearchStrategy searchStrategy;

    private final Long populationSize;

    private final Long tournamentSize;

    private final Double selectionProbability;

    private final Long newBlocksGenerated;

    private final DistanceMetric distanceMetric;

    private final Long maxAllowedLength;

    private final Map<SampleStructure, Double> expressionStructureProbability;

    private final Map<SampleStructure, Double> statementStructureProbability;


    public Configuration(String fullyQualifiedFileName) {
        File configFile = new File(fullyQualifiedFileName);

        if (!configFile.exists() || !configFile.exists()) {
            throw new IllegalArgumentException("Cannot read config file: " + fullyQualifiedFileName + ".");
        }

        InputStream configInputStream;
        try {
            configInputStream = new FileInputStream(configFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Map<String, SampleStructure> sampleStructuresByName = new LinkedHashMap<>();
        sampleStructuresByName.put(ConfigurationVocabulary.simpleExpr, SampleStructure.SIMPLE_EXPR);
        sampleStructuresByName.put(ConfigurationVocabulary.ifExpr, SampleStructure.IF_EXPR);
        sampleStructuresByName.put(ConfigurationVocabulary.elvisOp, SampleStructure.ELVIS_OP);
        sampleStructuresByName.put(ConfigurationVocabulary.tryCatch, SampleStructure.TRY_CATCH);
        sampleStructuresByName.put(ConfigurationVocabulary.simpleStmt, SampleStructure.SIMPLE_STMT);
        sampleStructuresByName.put(ConfigurationVocabulary.doWhile, SampleStructure.DO_WHILE);
        sampleStructuresByName.put(ConfigurationVocabulary.assignment, SampleStructure.ASSIGNMENT);

        Yaml yaml = new Yaml();

        Map<String, Object> data = yaml.load(configInputStream);

        if (!data.containsKey(ConfigurationVocabulary.config)) {
            throw new IllegalStateException("Configuration file must start with a config field.");
        }

        data = (LinkedHashMap<String, Object>) data.get(ConfigurationVocabulary.config);

        // Parse the search algorithm parameters
        if (!data.containsKey(ConfigurationVocabulary.heuristic)) {
            throw new IllegalStateException("heuristic field must be provided.");
        }
        LinkedHashMap<String, Object> heuristicCfg = (LinkedHashMap<String, Object>) data.get(ConfigurationVocabulary.heuristic);
        String heuristicName = (String) heuristicCfg.getOrDefault(ConfigurationVocabulary.type, "empty");

        switch (heuristicName) {
            case ConfigurationVocabulary.random -> {
                searchStrategy = SearchStrategy.RANDOM;
                populationSize = null;
                tournamentSize = null;
                selectionProbability = null;
                newBlocksGenerated = null;
                distanceMetric = null;
                maxAllowedLength = null;
            }

            case ConfigurationVocabulary.diversityGA -> {
                if (!heuristicCfg.containsKey(ConfigurationVocabulary.population)) {
                    throw new IllegalStateException("population-size size must be provided for diversity search.");
                }

                if (!heuristicCfg.containsKey(ConfigurationVocabulary.tournamentSize)) {
                    throw new IllegalStateException("tournament-size must be provided for diversity search.");
                }

                if (!heuristicCfg.containsKey(ConfigurationVocabulary.selectionProb)) {
                    throw new IllegalStateException("selection-probability must be provided for diversity search.");
                }

                if (!heuristicCfg.containsKey(ConfigurationVocabulary.newBlocks)) {
                    throw new IllegalStateException("new-blocks-generated must be provided for diversity search.");
                }

                if (!heuristicCfg.containsKey(ConfigurationVocabulary.distance)) {
                    throw new IllegalStateException("distance-metric must be provided for diversity search.");
                }

                if (!heuristicCfg.containsKey(ConfigurationVocabulary.maxLen)) {
                    throw new IllegalStateException("maximum-length must be provided for diversity search.");
                }

                searchStrategy = SearchStrategy.DIVERSITY_GA;
                populationSize = ((Integer) heuristicCfg.get(ConfigurationVocabulary.population)).longValue();
                tournamentSize = ((Integer) heuristicCfg.get(ConfigurationVocabulary.tournamentSize)).longValue();
                selectionProbability = (Double) heuristicCfg.get(ConfigurationVocabulary.selectionProb);
                newBlocksGenerated = ((Integer) heuristicCfg.get(ConfigurationVocabulary.newBlocks)).longValue();
                distanceMetric = nameToDistanceMetric((String) heuristicCfg.get(ConfigurationVocabulary.distance));
                maxAllowedLength = ((Integer) heuristicCfg.get(ConfigurationVocabulary.maxLen)).longValue();
            }

            default -> {
                throw new IllegalStateException("Cannot support search heuristic: " + heuristicName);
            }
        }
        // Parse the grammar parameters
        if (!data.containsKey(ConfigurationVocabulary.grammar)) {
            throw new IllegalStateException("grammar field must be provided.");
        }

        LinkedHashMap<String, Object> grammarCfg = (LinkedHashMap<String, Object>) data.get(ConfigurationVocabulary.grammar);

        if (!grammarCfg.containsKey(ConfigurationVocabulary.simplicity)) {
            throw new IllegalStateException("simplicity-bias field must be provided.");
        }

        simplicityBias = (Double) grammarCfg.get(ConfigurationVocabulary.simplicity);

        if (!grammarCfg.containsKey(ConfigurationVocabulary.plusDist)) {
            throw new IllegalStateException("plus-node-dist field must be provided.");
        }

        LinkedHashMap<String, Object> plusDistCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.plusDist);
        plusNodeDist = nameToDistribution((String) plusDistCfg.get(ConfigurationVocabulary.type));

        switch (plusNodeDist) {
            case UNIFORM -> {
                if (!plusDistCfg.containsKey(ConfigurationVocabulary.lb)) {
                    throw new IllegalStateException("Uniform distributions should contain a lower-bound field.");
                }

                plusNodeLb = Long.valueOf((Integer) plusDistCfg.get(ConfigurationVocabulary.lb));

                if (!plusDistCfg.containsKey(ConfigurationVocabulary.ub)) {
                    throw new IllegalStateException("Uniform distributions should contain an upper-bound field.");
                }

                plusNodeUb = Long.valueOf((Integer) plusDistCfg.get(ConfigurationVocabulary.ub));
            }

            case GEOMETRIC -> {
                if (!plusDistCfg.containsKey(ConfigurationVocabulary.lb)) {
                    throw new IllegalStateException("Geometric distributions should contain a lower-bound field.");
                }

                plusNodeLb = Long.valueOf((Integer) plusDistCfg.get(ConfigurationVocabulary.lb));

                // This will remain unused
                plusNodeUb = Long.MAX_VALUE;
            }

            default -> {
                throw new IllegalStateException("Cannot support distribution type: " + plusNodeDist);
            }
        }

        LinkedHashMap<String, Object> starDistCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.starDist);
        Distribution<Long> starDist = parseDistribution(starDistCfg);

        starNodeDist = starDist.distributionType();
        starNodeLb = starDist.lowerBound();
        starNodeUb = starDist.upperBound();



        // Parse the language feature parameters
        if (!data.containsKey(ConfigurationVocabulary.language)) {
            throw new IllegalStateException("language-features field must be provided.");
        }

        LinkedHashMap<String, Object> languageCfg = (LinkedHashMap<String, Object>) data.get(ConfigurationVocabulary.language);

        if (!languageCfg.containsKey(ConfigurationVocabulary.exprs)) {
            throw new IllegalStateException("The language-features field should contain an expressions field.");
        }

        if (!languageCfg.containsKey(ConfigurationVocabulary.stmts)) {
            throw new IllegalStateException("The language-features field should contain a statements field.");
        }

        expressionStructureProbability = new HashMap<>();
        statementStructureProbability = new HashMap<>();

        LinkedHashMap<String, Object> expressionCfg = (LinkedHashMap<String, Object>) languageCfg.get(ConfigurationVocabulary.exprs);
        LinkedHashMap<String, Object> statementsCfg = (LinkedHashMap<String, Object>) languageCfg.get(ConfigurationVocabulary.stmts);

        double exprSum = 0.0, stmtSum = 0.0;

        for (String structureName : sampleStructuresByName.keySet()) {
            if (!(expressionCfg.containsKey(structureName) || statementsCfg.containsKey(structureName))) {
                throw new IllegalStateException(structureName + " should be present in the language feature configuration.");
            }

            LinkedHashMap<String, Object> structureCfg;

            if (expressionCfg.containsKey(structureName)) {
                structureCfg = (LinkedHashMap<String, Object>) expressionCfg.get(structureName);
            } else {
                structureCfg = (LinkedHashMap<String, Object>) statementsCfg.get(structureName);
            }

            if (!structureCfg.containsKey(ConfigurationVocabulary.enable)) {
                throw new IllegalStateException("Sample structure " + structureName + " should contain an enable field.");
            }

            if (!structureCfg.containsKey(ConfigurationVocabulary.weight)) {
                throw new IllegalStateException("Sample structure " + structureName + " should contain a weight field field.");
            }

            boolean isEnabled = (Boolean) structureCfg.get(ConfigurationVocabulary.enable);
            double weight = (Double) structureCfg.get(ConfigurationVocabulary.weight);

            if (expressionCfg.containsKey(structureName)) {

                if (isEnabled) {
                    exprSum += weight;

                    expressionStructureProbability.put(sampleStructuresByName.get(structureName), weight);
                } else {
                    expressionStructureProbability.put(sampleStructuresByName.get(structureName), 0.0);
                }
            } else {
                if (isEnabled) {
                    stmtSum += weight;

                    statementStructureProbability.put(sampleStructuresByName.get(structureName), weight);
                } else {
                    statementStructureProbability.put(sampleStructuresByName.get(structureName), 0.0);
                }
            }
        }

        for (SampleStructure structure : expressionStructureProbability.keySet()) {
            expressionStructureProbability.put(structure, expressionStructureProbability.get(structure) / exprSum);
        }

        for (SampleStructure structure : statementStructureProbability.keySet()) {
            statementStructureProbability.put(structure, statementStructureProbability.get(structure) / stmtSum);
        }

        if (!grammarCfg.containsKey(ConfigurationVocabulary.starDist)) {
            throw new IllegalStateException("star-node-dist field must be provided.");
        }
    }

    public double getSimplicityBias() {
        return simplicityBias;
    }

    public DistributionType getPlusNodeDist() {
        return plusNodeDist;
    }

    public Long getPlusNodeLb() {
        return plusNodeLb;
    }

    public Long getPlusNodeUb() {
        return plusNodeUb;
    }

    public DistributionType getStarNodeDist() {
        return starNodeDist;
    }

    public Long getStarNodeLb() {
        return starNodeLb;
    }

    public Long getStarNodeUb() {
        return starNodeUb;
    }

    public Map<SampleStructure, Double> getExpressionProbabilityTable() {
        return expressionStructureProbability;
    }

    public Map<SampleStructure, Double> getStatementProbabilityTable() {
        return statementStructureProbability;
    }

    private <T extends Number> Distribution<T> parseDistribution(LinkedHashMap<String, Object> distCfg) {

        T lb, ub;

        DistributionType dist = nameToDistribution((String) distCfg.get(ConfigurationVocabulary.type));

        switch (dist) {
            case UNIFORM -> {
                if (!distCfg.containsKey(ConfigurationVocabulary.lb)) {
                    throw new IllegalStateException("Uniform distributions should contain a lower-bound field.");
                }

                lb = (T) Long.valueOf((Integer) distCfg.get(ConfigurationVocabulary.lb));

                if (!distCfg.containsKey(ConfigurationVocabulary.ub)) {
                    throw new IllegalStateException("Uniform distributions should contain an upper-bound field.");
                }

                ub = (T) Long.valueOf((Integer) distCfg.get(ConfigurationVocabulary.ub));
            }

            case GEOMETRIC -> {
                if (!distCfg.containsKey(ConfigurationVocabulary.lb)) {
                    throw new IllegalStateException("Geometric distributions should contain a lower-bound field.");
                }

                lb = (T) Long.valueOf((Integer) distCfg.get(ConfigurationVocabulary.lb));

                // This will remain unused
                ub = (T) Long.valueOf(Integer.MAX_VALUE);
            }

            default -> {
                throw new IllegalStateException("Cannot support distribution type: " + starNodeDist);
            }
        }

        return new Distribution<>(dist, lb, ub);
    }

    private static DistributionType nameToDistribution(String distName) {
        switch (distName) {
            case ConfigurationVocabulary.uniform -> {
                return DistributionType.UNIFORM;
            }
            case ConfigurationVocabulary.geometric -> {
                return DistributionType.GEOMETRIC;
            }
            default -> {
                throw new IllegalArgumentException("Distribution type " + distName + " not supported.");
            }
        }
    }

    private static DistanceMetric nameToDistanceMetric(String distanceMetricName) {
        switch (distanceMetricName) {
            case ConfigurationVocabulary.euclidean -> {
                return DistanceMetric.EUCLIDEAN;
            }
            case ConfigurationVocabulary.manhattan -> {
                return DistanceMetric.MANHATTAN;
            }
            default -> {
                throw new IllegalArgumentException("Distance metric " + distanceMetricName + " not supported.");
            }
        }
    }

    public Search getSearchStrategy(SyntaxNode nodeToSample, Long timeBudgetMilis,
                                    Context rootContext, Long seed) {

        switch (searchStrategy) {
            case RANDOM -> {
                return new RandomSearch(nodeToSample, timeBudgetMilis, rootContext, seed);
            }
            case DIVERSITY_GA -> {
                RandomNumberGenerator selectionRng = new RandomNumberGenerator(seed);

                FitnessFunction f = new DiversityFitnessFunction(null, distanceMetric);
                SelectionOperator s = new TournamentSelection(tournamentSize, selectionProbability,
                        maxAllowedLength, selectionRng, f);
                RecombinationOperator r = new SimpleRecombinationOperator();

                return new DiversityGA(nodeToSample, timeBudgetMilis, rootContext, seed,
                        populationSize, f, s, r);
            }
            default -> {
                throw new IllegalStateException("Cannot support search strategy: " + searchStrategy);
            }
        }
    }
}
