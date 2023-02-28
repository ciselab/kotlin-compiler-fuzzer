package org.fuzzer.dt;

import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.representations.types.KClassType;
import org.fuzzer.utils.KGrammarVocabulary;
import org.jetbrains.kotlin.spec.grammar.tools.KotlinParseTree;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class FuzzerStatistics {
    private final Map<SampleStructure, Long> extendedGrammarVisitations;

    private final Map<String, Long> syntaxRuleVisitations;

    private final List<Long> defaultCompilerTimes;

    private final List<Long> otherCompilerTimes;

    private Long startTime;

    private Long finishTime;

    private final Set<String> relevantRuleNames;

    public FuzzerStatistics() {
        extendedGrammarVisitations = new HashMap<>();
        syntaxRuleVisitations = new HashMap<>();
        defaultCompilerTimes = new LinkedList<>();
        otherCompilerTimes = new LinkedList<>();
        startTime = System.currentTimeMillis();

        relevantRuleNames = Arrays.stream(
                new String[]{KGrammarVocabulary.classDecl,
                        KGrammarVocabulary.funcDecl,
                        KGrammarVocabulary.propertyDecl,
                        KGrammarVocabulary.statement})
                .collect(Collectors.toSet());
    }

    public void stop() {
        this.finishTime = System.currentTimeMillis();
    }

    public void increment(SampleStructure sampledStruct) {
        extendedGrammarVisitations.put(sampledStruct, extendedGrammarVisitations.getOrDefault(sampledStruct, 0L) + 1);
    }

    public void increment(String syntaxRule) {
        if (relevantRuleNames.contains(syntaxRule)) {
            syntaxRuleVisitations.put(syntaxRule, syntaxRuleVisitations.getOrDefault(syntaxRule, 0L) + 1);
        }
    }

    public void record(Long defaultCompilerTime, Long otherCompilerTime) {
        defaultCompilerTimes.add(defaultCompilerTime);
        otherCompilerTimes.add(otherCompilerTime);
    }

    public void reset() {
        extendedGrammarVisitations.clear();
        syntaxRuleVisitations.clear();
        defaultCompilerTimes.clear();
        otherCompilerTimes.clear();
        startTime = System.currentTimeMillis();
    }

    public void record(KotlinParseTree tree) {
        for (KotlinParseTree child : tree.getChildren()) {
            record(child);
        }

        increment(tree.getName());
    }

    public JSONObject toJson() {
        JSONObject repr = new JSONObject();
        JSONObject sampledGrammar = new JSONObject();
        for (SampleStructure struct : SampleStructure.values()) {
            sampledGrammar.put(struct.name(), extendedGrammarVisitations.getOrDefault(struct, 0L));
        }

        JSONObject antlrGrammar = new JSONObject();
        for (String rule : relevantRuleNames) {
            antlrGrammar.put(rule, syntaxRuleVisitations.getOrDefault(rule, 0L));
        }

        repr.put("ext_grammar", sampledGrammar);
        repr.put("antlr_grammar", antlrGrammar);

        repr.put("c1_time", defaultCompilerTimes.get(0));
        repr.put("c2_time", otherCompilerTimes.get(0));

        reset();

        return repr;
    }
}
