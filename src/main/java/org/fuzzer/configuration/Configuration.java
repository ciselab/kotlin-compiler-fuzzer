package org.fuzzer.configuration;


import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.syntax.SyntaxNode;
import org.fuzzer.representations.context.Context;
import org.fuzzer.search.algorithm.*;
import org.fuzzer.search.fitness.*;
import org.fuzzer.search.fitness.proximity.CollectiveProximityFitnessFunction;
import org.fuzzer.search.fitness.proximity.ProximityMOFitnessFunction;
import org.fuzzer.search.fitness.proximity.SOPopulationFitnessFunction;
import org.fuzzer.search.fitness.proximity.SingularSOProximityFitnessFunction;
import org.fuzzer.search.operators.mutation.block.MutationOperator;
import org.fuzzer.search.operators.mutation.block.SimpleMutationOperator;
import org.fuzzer.search.operators.recombination.block.RecombinationOperator;
import org.fuzzer.search.operators.recombination.block.SimpleRecombinationOperator;
import org.fuzzer.search.operators.recombination.suite.SuiteRecombinationOperator;
import org.fuzzer.search.operators.recombination.suite.WTSRecombinationOperator;
import org.fuzzer.search.operators.selection.block.*;
import org.fuzzer.search.operators.selection.suite.SuiteSOSelectionOperator;
import org.fuzzer.search.operators.selection.suite.SuiteTournamentSelection;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Configuration {

    private final double simplicityBias;

    private final Distribution<Long> plusNodeDist;

    private final Distribution<Long> starNodeDist;

    private final Distribution<Long> funcStmtsDist;

    private final Distribution<Long> funcParamsDist;

    private final Distribution<Long> doWhileDist;

    private final Distribution<Long> loopDist;

    private final Distribution<Long> tryDist;

    private final Distribution<Long> catchNumberDist;

    private final Distribution<Long> catchStmtDist;

    private final Distribution<Long> finallyDist;

    private final Double finallyProbability;

    private final Distribution<Long> ifDist;

    private final Distribution<Long> elseDist;

    private final Double elseProbability;

    private final SearchStrategy searchStrategy;

    private final Long populationSize;

    private final Long newBlocksGenerated;

    private final DistanceMetric distanceMetric;

    private final Map<SampleStructure, Double> expressionStructureProbability;

    private final Map<SampleStructure, Double> statementStructureProbability;

    private final Tuple<SuiteSOSelectionOperator, Tuple<SOSelectionOperator, SelectionStrategy>> selectionData;

    private final String singleEmbeddingUrl;

    private final String multiEmbeddingUrl;
    private final String targetsUrl;

    private final Long numberOfTargets;

    private final Long numberOfItersPerTarget;

    private final Long blocksPerSuite;

    private final Double suiteMutationProbability;

//    private final String oomPredictionUrl;

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

        LinkedHashMap<String, Object> data = yaml.load(configInputStream);

        if (!data.containsKey(ConfigurationVocabulary.config)) {
            throw new IllegalStateException("Configuration file must start with a config field.");
        }

        data = (LinkedHashMap<String, Object>) data.get(ConfigurationVocabulary.config);

        // Parse the search algorithm parameters
        checkExistence(data, ConfigurationVocabulary.heuristic);

        LinkedHashMap<String, Object> heuristicCfg = (LinkedHashMap<String, Object>) data.get(ConfigurationVocabulary.heuristic);
        LinkedHashMap<String, Object> remoteCfg = (LinkedHashMap<String, Object>) heuristicCfg.get(ConfigurationVocabulary.remote);

        String heuristicName = (String) heuristicCfg.getOrDefault(ConfigurationVocabulary.type, "empty");

        switch (heuristicName) {
            case ConfigurationVocabulary.random -> {
                searchStrategy = SearchStrategy.RANDOM;
                populationSize = null;
                newBlocksGenerated = null;
                distanceMetric = null;
                singleEmbeddingUrl = null;
                multiEmbeddingUrl = null;
                targetsUrl = null;
                numberOfTargets = null;
                numberOfItersPerTarget = null;
                blocksPerSuite = null;
                suiteMutationProbability = null;
            }

            case ConfigurationVocabulary.diversityGA -> {
                checkGAConfiguration(heuristicCfg);

                if (!heuristicCfg.containsKey(ConfigurationVocabulary.distance)) {
                    throw new IllegalStateException("distance-metric must be provided for diversity search.");
                }

                searchStrategy = SearchStrategy.DIVERSITY_GA;
                populationSize = ((Integer) heuristicCfg.get(ConfigurationVocabulary.popSize)).longValue();
                newBlocksGenerated = ((Integer) heuristicCfg.get(ConfigurationVocabulary.newBlocks)).longValue();
                distanceMetric = nameToDistanceMetric((String) heuristicCfg.get(ConfigurationVocabulary.distance));
                singleEmbeddingUrl = null;
                multiEmbeddingUrl = null;
                targetsUrl = null;
                numberOfTargets = null;
                numberOfItersPerTarget = null;
                blocksPerSuite = null;
                suiteMutationProbability = null;
            }

            case ConfigurationVocabulary.structureMoga -> {
                checkGAConfiguration(heuristicCfg);

                searchStrategy = SearchStrategy.STRUCT_MOGA;
                populationSize = ((Integer) heuristicCfg.get(ConfigurationVocabulary.popSize)).longValue();
                newBlocksGenerated = ((Integer) heuristicCfg.get(ConfigurationVocabulary.newBlocks)).longValue();
                distanceMetric = null;
                singleEmbeddingUrl = null;
                multiEmbeddingUrl = null;
                targetsUrl = null;
                numberOfTargets = null;
                numberOfItersPerTarget = null;
                blocksPerSuite = null;
                suiteMutationProbability = null;
            }

            case ConfigurationVocabulary.proximityMoga -> {
                searchStrategy = SearchStrategy.PROXIMITY_MOGA;
                populationSize = ((Integer) heuristicCfg.get(ConfigurationVocabulary.popSize)).longValue();
                newBlocksGenerated = ((Integer) heuristicCfg.get(ConfigurationVocabulary.newBlocks)).longValue();
                distanceMetric = null;
                singleEmbeddingUrl = (String) remoteCfg.get(ConfigurationVocabulary.singleEmbedding);
                multiEmbeddingUrl = (String) remoteCfg.get(ConfigurationVocabulary.multiEmbedding);
                targetsUrl = (String) remoteCfg.get(ConfigurationVocabulary.targets);
                numberOfTargets = ((Integer) remoteCfg.get(ConfigurationVocabulary.numberOfTargets)).longValue();
                numberOfItersPerTarget = null;
                blocksPerSuite = null;
                suiteMutationProbability = null;
            }

            case ConfigurationVocabulary.proximityGA -> {
                checkGAConfiguration(heuristicCfg);

                searchStrategy = SearchStrategy.PROXIMITY_GA;
                populationSize = ((Integer) heuristicCfg.get(ConfigurationVocabulary.popSize)).longValue();
                newBlocksGenerated = ((Integer) heuristicCfg.get(ConfigurationVocabulary.newBlocks)).longValue();
                singleEmbeddingUrl = (String) remoteCfg.get(ConfigurationVocabulary.singleEmbedding);
                multiEmbeddingUrl = (String) remoteCfg.get(ConfigurationVocabulary.multiEmbedding);
                targetsUrl = (String) remoteCfg.get(ConfigurationVocabulary.targets);
                numberOfTargets = ((Integer) remoteCfg.get(ConfigurationVocabulary.numberOfTargets)).longValue();
                numberOfItersPerTarget = ((Integer) heuristicCfg.get(ConfigurationVocabulary.numberOfItersPerTarget)).longValue();
                // TODO: make this configurable
                distanceMetric = null;
                blocksPerSuite = null;
                suiteMutationProbability = null;
            }

            case ConfigurationVocabulary.proximityWTS -> {
                checkGAConfiguration(heuristicCfg);

                searchStrategy = SearchStrategy.PROXIMITY_WTS;
                populationSize = ((Integer) heuristicCfg.get(ConfigurationVocabulary.popSize)).longValue();
                newBlocksGenerated = ((Integer) heuristicCfg.get(ConfigurationVocabulary.newBlocks)).longValue();
                singleEmbeddingUrl = (String) remoteCfg.get(ConfigurationVocabulary.singleEmbedding);
                multiEmbeddingUrl = (String) remoteCfg.get(ConfigurationVocabulary.multiEmbedding);
                targetsUrl = (String) remoteCfg.get(ConfigurationVocabulary.targets);
                numberOfTargets = ((Integer) remoteCfg.get(ConfigurationVocabulary.numberOfTargets)).longValue();
                blocksPerSuite = ((Integer) heuristicCfg.get(ConfigurationVocabulary.blocksPerSuite)).longValue();
                numberOfItersPerTarget = null;
                distanceMetric = null;
                suiteMutationProbability = (Double) heuristicCfg.get(ConfigurationVocabulary.suiteMutationProb);

            }

            default -> {
                throw new IllegalStateException("Cannot support search heuristic: " + heuristicName);
            }
        }


        LinkedHashMap<String, Object> selectionCfg = (LinkedHashMap<String, Object>) heuristicCfg.get(ConfigurationVocabulary.selection);
        selectionData = getSelectionOperator(selectionCfg);

        // Parse the grammar parameters
        checkExistence(data, ConfigurationVocabulary.grammar);

        LinkedHashMap<String, Object> grammarCfg = (LinkedHashMap<String, Object>) data.get(ConfigurationVocabulary.grammar);

        checkExistence(grammarCfg, ConfigurationVocabulary.simplicity);

        simplicityBias = (Double) grammarCfg.get(ConfigurationVocabulary.simplicity);

        checkExistence(grammarCfg, ConfigurationVocabulary.plusDist);
        checkExistence(grammarCfg, ConfigurationVocabulary.starDist);
        checkExistence(grammarCfg, ConfigurationVocabulary.funcStmtsDist);
        checkExistence(grammarCfg, ConfigurationVocabulary.funcParamsDist);
        checkExistence(grammarCfg, ConfigurationVocabulary.doWhileDist);
        checkExistence(grammarCfg, ConfigurationVocabulary.loopDist);
        checkExistence(grammarCfg, ConfigurationVocabulary.tryDist);
        checkExistence(grammarCfg, ConfigurationVocabulary.catchNumberDist);
        checkExistence(grammarCfg, ConfigurationVocabulary.catchStmtDist);
        checkExistence(grammarCfg, ConfigurationVocabulary.finallyDist);
        checkExistence(grammarCfg, ConfigurationVocabulary.ifDist);
        checkExistence(grammarCfg, ConfigurationVocabulary.elseDist);

        LinkedHashMap<String, Object> plusDistCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.plusDist);
        plusNodeDist = parseDistribution(plusDistCfg);

        LinkedHashMap<String, Object> starDistCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.starDist);
        starNodeDist = parseDistribution(starDistCfg);

        LinkedHashMap<String, Object> funcStmtsCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.funcStmtsDist);
        funcStmtsDist = parseDistribution(funcStmtsCfg);

        LinkedHashMap<String, Object> funcParamsCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.funcParamsDist);
        funcParamsDist = parseDistribution(funcParamsCfg);

        LinkedHashMap<String, Object> doWhileCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.doWhileDist);
        doWhileDist = parseDistribution(doWhileCfg);

        LinkedHashMap<String, Object> loopCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.loopDist);
        loopDist = parseDistribution(loopCfg);

        LinkedHashMap<String, Object> tryCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.tryDist);
        tryDist = parseDistribution(tryCfg);

        LinkedHashMap<String, Object> catchNumberCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.catchNumberDist);
        catchNumberDist = parseDistribution(catchNumberCfg);

        LinkedHashMap<String, Object> catchCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.catchStmtDist);
        catchStmtDist = parseDistribution(catchCfg);

        LinkedHashMap<String, Object> finallyCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.finallyDist);
        finallyDist = parseDistribution(finallyCfg);

        checkExistence(finallyCfg, ConfigurationVocabulary.probability);
        finallyProbability = (Double) finallyCfg.get(ConfigurationVocabulary.probability);

        LinkedHashMap<String, Object> ifCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.ifDist);
        ifDist = parseDistribution(ifCfg);

        LinkedHashMap<String, Object> elseCfg = (LinkedHashMap<String, Object>) grammarCfg.get(ConfigurationVocabulary.elseDist);
        elseDist = parseDistribution(elseCfg);

        checkExistence(elseCfg, ConfigurationVocabulary.probability);
        elseProbability = (Double) finallyCfg.get(ConfigurationVocabulary.probability);

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

    private void checkExistence(LinkedHashMap<String, Object> cfg, String field) {
        if (!cfg.containsKey(field)) {
            throw new IllegalStateException(field + " field must be provided.");
        }
    }

    public Distribution<Long> getPlusNodeDist() {
        return plusNodeDist;
    }

    public Distribution<Long> getStarNodeDist() {
        return starNodeDist;
    }

    public Distribution<Long> getFuncStmtsDist() {
        return funcStmtsDist;
    }

    public Distribution<Long> getFuncParamsDist() {
        return funcParamsDist;
    }

    public Distribution<Long> getDoWhileDist() {
        return doWhileDist;
    }

    public Distribution<Long> getLoopDist() {
        return loopDist;
    }

    public Distribution<Long> getTryDist() {
        return tryDist;
    }

    public Distribution<Long> getCatchNumberDist() {
        return catchNumberDist;
    }

    public Distribution<Long> getCatchStmtDist() {
        return catchStmtDist;
    }

    public Distribution<Long> getFinallyDist() {
        return finallyDist;
    }

    public Double getFinallyProbability() {
        return finallyProbability;
    }

    public Distribution<Long> getIfDist() {
        return ifDist;
    }

    public Distribution<Long> getElseDist() {
        return elseDist;
    }

    public Double getElseProbability() {
        return elseProbability;
    }

    public double getSimplicityBias() {
        return simplicityBias;
    }

    public Map<SampleStructure, Double> getExpressionProbabilityTable() {
        return expressionStructureProbability;
    }

    public Map<SampleStructure, Double> getStatementProbabilityTable() {
        return statementStructureProbability;
    }

    private Tuple<SuiteSOSelectionOperator, Tuple<SOSelectionOperator, SelectionStrategy>> getSelectionOperator(LinkedHashMap<String, Object> selectionCfg) {

        checkExistence(selectionCfg, ConfigurationVocabulary.individual);
        checkExistence(selectionCfg, ConfigurationVocabulary.suite);

        LinkedHashMap<String, Object> individualSelectionData = (LinkedHashMap<String, Object>) selectionCfg.get(ConfigurationVocabulary.individual);
        LinkedHashMap<String, Object> suiteSelectionData = (LinkedHashMap<String, Object>) selectionCfg.get(ConfigurationVocabulary.suite);

        checkExistence(individualSelectionData, ConfigurationVocabulary.moSelection);
        checkExistence(individualSelectionData, ConfigurationVocabulary.soSelection);
        checkExistence(individualSelectionData, ConfigurationVocabulary.maxLen);

        Long maxSize = ((Integer) individualSelectionData.get(ConfigurationVocabulary.maxLen)).longValue();

        String moSelection = (String) individualSelectionData.get(ConfigurationVocabulary.moSelection);
        String soSelection = (String) individualSelectionData.get(ConfigurationVocabulary.soSelection);
        String suiteSoSelection = (String) suiteSelectionData.get(ConfigurationVocabulary.soSelection);

        SOSelectionOperator soSelectionOperator;
        SuiteSOSelectionOperator suiteSOSelectionOperator;
        SelectionStrategy moSelectionIndicator;

        switch (soSelection) {
            case ConfigurationVocabulary.tournament -> {
                checkExistence(individualSelectionData, ConfigurationVocabulary.tournamentSize);
                checkExistence(individualSelectionData, ConfigurationVocabulary.tournamentSelectionProb);

                Long tournamentSize = ((Integer) individualSelectionData.get(ConfigurationVocabulary.tournamentSize)).longValue();
                double tournamentSelectionProb = (Double) individualSelectionData.get(ConfigurationVocabulary.tournamentSelectionProb);

                soSelectionOperator = new TournamentSelection(tournamentSize, tournamentSelectionProb, maxSize, null, null);
            }
            case ConfigurationVocabulary.truncated -> {
                checkExistence(individualSelectionData, ConfigurationVocabulary.truncationProp);

                double truncationProp = (Double) individualSelectionData.get(ConfigurationVocabulary.truncationProp);

                soSelectionOperator = new TruncatedSelection(truncationProp, maxSize, null);
            }
            default -> {
                throw new IllegalStateException("Unknown SO selection operator: " + soSelection);
            }
        }

        switch (suiteSoSelection) {
            case ConfigurationVocabulary.tournament -> {
                checkExistence(suiteSelectionData, ConfigurationVocabulary.tournamentSize);
                checkExistence(suiteSelectionData, ConfigurationVocabulary.tournamentSelectionProb);

                Long tournamentSize = ((Integer) suiteSelectionData.get(ConfigurationVocabulary.tournamentSize)).longValue();
                double tournamentSelectionProb = (Double) suiteSelectionData.get(ConfigurationVocabulary.tournamentSelectionProb);

                suiteSOSelectionOperator = new SuiteTournamentSelection(tournamentSize, tournamentSelectionProb, maxSize, null, null);
            }
            default -> {
                throw new IllegalStateException("Unknown Suite SO selection operator: " + soSelection);
            }
        }

        switch (moSelection) {
            case ConfigurationVocabulary.domRank -> {
                moSelectionIndicator = SelectionStrategy.DOMINATION_RANK;
            }
            case ConfigurationVocabulary.domCount -> {
                moSelectionIndicator = SelectionStrategy.DOMINATION_COUNT;
            }
            default -> {
                throw new IllegalStateException("Unknown MO selection operator: " + moSelection);
            }
        }

        return new Tuple<>(suiteSOSelectionOperator, new Tuple<>(soSelectionOperator, moSelectionIndicator));
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
            case ConfigurationVocabulary.linfinity -> {
                return DistanceMetric.LINFINITY;
            }
            default -> {
                throw new IllegalArgumentException("Distance metric " + distanceMetricName + " not supported.");
            }
        }
    }

    public Search getSearchStrategy(SyntaxNode nodeToSample, Long timeBudgetMillis,
                                    Context rootContext, long searchSeed,
                                    long selectionSeed, long mutationSeed,
                                    long recombinationSeed, long snapshotInterval,
                                    String outputDir) {

        RandomNumberGenerator selectionRng = new RandomNumberGenerator(selectionSeed);
        RandomNumberGenerator mutationRng = new RandomNumberGenerator(mutationSeed);
        RandomNumberGenerator recombinationRng = new RandomNumberGenerator(recombinationSeed);

        switch (searchStrategy) {
            case RANDOM -> {
                return new RandomSearch(nodeToSample, timeBudgetMillis, rootContext, searchSeed, snapshotInterval, outputDir);
            }
            case PROXIMITY_GA -> {
                SingularSOProximityFitnessFunction f = null;
                try {
                    f = new SingularSOProximityFitnessFunction(new URL(singleEmbeddingUrl), new URL(multiEmbeddingUrl),
                            new URL(targetsUrl), Integer.parseInt(numberOfTargets.toString()));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                SOSelectionOperator s = selectionData.second().first();

                if (s instanceof TournamentSelection) {
                    ((TournamentSelection) s).setFitnessFunction(f);
                    ((TournamentSelection) s).setRng(selectionRng);
                } else if (s instanceof TruncatedSelection) {
                    ((TruncatedSelection) s).setFitnessFunction(f);
                } else {
                    throw new IllegalStateException("Cannot support selection operator: " + s);
                }

                MutationOperator m = new SimpleMutationOperator(nodeToSample, rootContext, mutationRng);
                RecombinationOperator r = new SimpleRecombinationOperator(recombinationRng);

                return new ProximityGA(nodeToSample, timeBudgetMillis, rootContext, searchSeed,
                        populationSize, newBlocksGenerated, f, s, m, r, null, numberOfItersPerTarget, snapshotInterval, outputDir);
            }
            case PROXIMITY_WTS -> {
                SOPopulationFitnessFunction f = null;
                try {
                    f = new CollectiveProximityFitnessFunction(new URL(singleEmbeddingUrl), new URL(multiEmbeddingUrl),
                            new URL(targetsUrl), Integer.parseInt(numberOfTargets.toString()));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                SuiteSOSelectionOperator s = selectionData.first();

                if (s instanceof SuiteTournamentSelection) {
                    ((SuiteTournamentSelection) s).setFitnessFunction(f);
                    ((SuiteTournamentSelection) s).setRng(selectionRng);
                } else {
                    throw new IllegalStateException("Cannot support selection operator: " + s);
                }

                SuiteRecombinationOperator r = new WTSRecombinationOperator(new RandomNumberGenerator(recombinationSeed));

                return new ProximityWholeTestSuite(nodeToSample, timeBudgetMillis, rootContext, searchSeed,
                        populationSize, newBlocksGenerated, s, r, blocksPerSuite, suiteMutationProbability, snapshotInterval, outputDir);
            }
            case DIVERSITY_GA -> {
                SOFitnessFunction f = new DiversityFitnessFunction(null, distanceMetric);
                SOSelectionOperator s = selectionData.second().first();

                if (s instanceof TournamentSelection) {
                    ((TournamentSelection) s).setFitnessFunction(f);
                    ((TournamentSelection) s).setRng(selectionRng);
                } else if (s instanceof TruncatedSelection) {
                    ((TruncatedSelection) s).setFitnessFunction(f);
                } else {
                    throw new IllegalStateException("Cannot support selection operator: " + s);
                }

                MutationOperator m = new SimpleMutationOperator(nodeToSample, rootContext, mutationRng);
                RecombinationOperator r = new SimpleRecombinationOperator(recombinationRng);

                return new DiversityGA(nodeToSample, timeBudgetMillis, rootContext, searchSeed,
                        populationSize, newBlocksGenerated, f, s, m, r, null, snapshotInterval, outputDir);
            }
            case STRUCT_MOGA -> {
                MOFitnessFunction f = new StructureMOFitness();
                SOFitnessFunction selectionFitness = null;

                SOSelectionOperator soSelector = selectionData.second().first();

                if (soSelector instanceof TournamentSelection) {
                    ((TournamentSelection) soSelector).setFitnessFunction(selectionFitness);
                    ((TournamentSelection) soSelector).setRng(selectionRng);
                } else if (soSelector instanceof TruncatedSelection) {
                    ((TruncatedSelection) soSelector).setFitnessFunction(selectionFitness);
                } else {
                    throw new IllegalStateException("Cannot support selection operator: " + soSelector);
                }

                MOSelectionOperator moSelector;

                // TODO: make this configurable
                boolean[] shouldMinimize = new boolean[13];
                shouldMinimize[0] = true;

                for (int i = 1; i < 12; i++) {
                    shouldMinimize[i] = false;
                }

                switch (selectionData.second().second()) {
                    case DOMINATION_RANK -> {
                        moSelector = new DominationRankSelection(soSelector.getSizeMaxAllowedSize(), f, soSelector, shouldMinimize);
                    }
                    case DOMINATION_COUNT -> {
                        moSelector = new DominationCountSelection(soSelector.getSizeMaxAllowedSize(), f, soSelector, shouldMinimize);
                    }
                    default -> {
                        throw new IllegalStateException("Cannot support selection strategy: " + selectionData.second());
                    }
                }

                MutationOperator m = new SimpleMutationOperator(nodeToSample, rootContext, mutationRng);
                RecombinationOperator r = new SimpleRecombinationOperator(recombinationRng);

                return new MOGA(nodeToSample, timeBudgetMillis, rootContext, searchSeed,
                        populationSize, newBlocksGenerated, f, moSelector, m, r, null, shouldMinimize, snapshotInterval, outputDir);
            }
            case PROXIMITY_MOGA -> {
                ProximityMOFitnessFunction f;
                try {
                    f = new ProximityMOFitnessFunction(new URL(singleEmbeddingUrl), new URL(multiEmbeddingUrl),
                            new URL(targetsUrl), Math.toIntExact(numberOfTargets));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                SOFitnessFunction selectionFitness = null;
                SOSelectionOperator soSelector = selectionData.second().first();

                if (soSelector instanceof TournamentSelection) {
                    ((TournamentSelection) soSelector).setFitnessFunction(selectionFitness);
                    ((TournamentSelection) soSelector).setRng(selectionRng);
                } else if (soSelector instanceof TruncatedSelection) {
                    ((TruncatedSelection) soSelector).setFitnessFunction(selectionFitness);
                } else {
                    throw new IllegalStateException("Cannot support selection operator: " + soSelector);
                }

                MOSelectionOperator moSelector;

                // TODO: make this configurable
                boolean[] shouldMinimize = new boolean[Math.toIntExact(numberOfTargets)];
                for (int i = 1; i < numberOfTargets; i++) {
                    shouldMinimize[i] = true;
                }

                switch (selectionData.second().second()) {
                    case DOMINATION_RANK -> {
                        moSelector = new DominationRankSelection(soSelector.getSizeMaxAllowedSize(), f, soSelector, shouldMinimize);
                    }
                    case DOMINATION_COUNT -> {
                        moSelector = new DominationCountSelection(soSelector.getSizeMaxAllowedSize(), f, soSelector, shouldMinimize);
                    }
                    default -> {
                        throw new IllegalStateException("Cannot support selection strategy: " + selectionData.second());
                    }
                }

                MutationOperator m = new SimpleMutationOperator(nodeToSample, rootContext, mutationRng);
                RecombinationOperator r = new SimpleRecombinationOperator(recombinationRng);

                return new ProximityMOGA(nodeToSample, timeBudgetMillis, rootContext, searchSeed,
                        populationSize, newBlocksGenerated, f, moSelector, m, r, null, shouldMinimize, snapshotInterval, outputDir);
            }
            default -> {
                throw new IllegalStateException("Cannot support search strategy: " + searchStrategy);
            }
        }
    }

    private void checkGAConfiguration(LinkedHashMap<String, Object> heuristicCfg) {
        if (!heuristicCfg.containsKey(ConfigurationVocabulary.popSize)) {
            throw new IllegalStateException("population-size size must be provided for GAs.");
        }

        if (!heuristicCfg.containsKey(ConfigurationVocabulary.type)) {
            throw new IllegalStateException("maximum-length must be provided for diversity search.");
        }

        if (!heuristicCfg.containsKey(ConfigurationVocabulary.newBlocks)) {
            throw new IllegalStateException("new-blocks-generated must be provided for GAs search.");
        }

        if (!heuristicCfg.containsKey(ConfigurationVocabulary.selection)) {
            throw new IllegalStateException("selection must be provided for GAs.");
        }
    }

    private void checkSelectionConfiguration(LinkedHashMap<String, Object> selectionCfg) {
        if (!selectionCfg.containsKey(ConfigurationVocabulary.tournamentSize)) {
            throw new IllegalStateException("tournament-size must be provided for diversity search.");
        }

        if (!selectionCfg.containsKey(ConfigurationVocabulary.tournamentSelectionProb)) {
            throw new IllegalStateException("selection-probability must be provided for diversity search.");
        }

        if (!selectionCfg.containsKey(ConfigurationVocabulary.maxLen)) {
            throw new IllegalStateException("maximum-length must be provided for diversity search.");
        }
    }
}
