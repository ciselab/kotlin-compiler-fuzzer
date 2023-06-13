package org.fuzzer.search.fitness.proximity;

import org.fuzzer.search.chromosome.CodeBlock;
import org.fuzzer.search.clustering.Point;
import org.fuzzer.search.fitness.DistanceMetric;
import org.fuzzer.utils.RequestMaker;
import org.fuzzer.utils.StringUtilities;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RemoteEvaluation {

    private final RequestMaker singleEvalRequestMaker;

    private final RequestMaker multipleEvalRequestMaker;
    private final Map<CodeBlock, double[]> cache;

    public RemoteEvaluation(RequestMaker singleRequestMaker, RequestMaker multipleRequestMaker) {
        this.singleEvalRequestMaker = singleRequestMaker;
        this.multipleEvalRequestMaker = multipleRequestMaker;
        this.cache = new HashMap<>();
    }

    public double[] getFromCache(CodeBlock code) {
        if (!cache.containsKey(code)) {
            throw new IllegalArgumentException("Code not in cache.");
        }

        return cache.get(code);
    }

    public boolean isCached(CodeBlock code) {
        return cache.containsKey(code);
    }

    public void addToCache(CodeBlock code, double[] embedding) {
        cache.put(code, embedding);
    }

    public void addToCache(Map<CodeBlock, double[]> embeddings) {
        cache.putAll(embeddings);
    }

    private static Map<CodeBlock, double[]> processMultipleEval(Map<String, CodeBlock> ids, String data) {

        Map<CodeBlock, double[]> res = new HashMap<>();
        JSONObject json = new JSONObject(data);

        for (String key : json.keySet()) {
            String embedding = json.getJSONArray(key).toString();
            res.put(ids.get(key), processSingleEval(embedding));
        }

        return res;
    }

    protected static double[][] processNestedLists(String data) {
        data = data.substring(data.indexOf('[') + 1, data.indexOf("]]") - 1);

        return Arrays.stream(data.split("],"))
                .map(s -> s.substring(s.indexOf('[') + 1))
                .map(s -> s.split(","))
                .map(s -> Arrays.stream(s)
                        .map(Double::valueOf)
                        .mapToDouble(Double::doubleValue)
                        .toArray())
                .collect(Collectors.toList())
                .toArray(new double[0][0]);
    }
    private static double[] processSingleEval(String data) {
        data = data.substring(data.indexOf('[') + 1, data.indexOf(']') - 1);

        return Arrays.stream(data.split(","))
                .map(Double::valueOf)
                .mapToDouble(Double::doubleValue)
                .toArray();
    }

    public Map<CodeBlock, double[]> getEmbeddings(Collection<CodeBlock> codeBlocks) {
        JSONArray json = new JSONArray();
        Map<String, CodeBlock> ids = new HashMap<>();

        for (CodeBlock c : codeBlocks) {
            String id = Integer.toString(c.hashCode());
            ids.put(id, c);
            // Create an object for this code block
            JSONObject codeObject = new JSONObject();
            codeObject.put("name", Integer.toString(c.hashCode()));
            codeObject.put("text", c.text());

            // Add it to the array
            json.put(codeObject);
        }

        return processMultipleEval(ids, multipleEvalRequestMaker.executeGetReq(json.toString()));
    }

    public double[] getEmbedding(CodeBlock codeBlock) {
        String code = codeBlock.text();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "");
        jsonObject.put("text", StringUtilities.escapeQuotes(code));
        String payload = jsonObject.toString();

        String embedding = singleEvalRequestMaker.executeGetReq(payload);

        return processSingleEval(embedding);
    }

    protected double distanceToTarget(Collection<CodeBlock> codeBlocks, double[] targetPoint) {
        double minDistance = Double.MAX_VALUE;

        for (CodeBlock b : codeBlocks) {
            double d = Point.distance(getFromCache(b), targetPoint, DistanceMetric.EUCLIDEAN);
            minDistance = Math.min(minDistance, d);
        }

        return minDistance;
    }

    protected double distanceToTarget(CodeBlock codeBlock, double[] targetPoint) {
        return Point.distance(getFromCache(codeBlock), targetPoint, DistanceMetric.EUCLIDEAN);
    }
}
