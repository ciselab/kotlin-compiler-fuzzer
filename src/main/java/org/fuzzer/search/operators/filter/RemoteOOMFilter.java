//package org.fuzzer.search.operators.filter;
//
//import org.fuzzer.search.chromosome.CodeBlock;
//import org.fuzzer.utils.RequestMaker;
//
//import java.net.URL;
//import java.util.Collection;
//import java.util.List;
//
//public class RemoteOOMFilter implements CodeBlockFilter {
//    private final RequestMaker singleBlockRM;
//
//    private final RequestMaker multipleBlockRM;
//
//    public RemoteOOMFilter(URL singleBlockUrl, URL multipleBlockUrl) {
//        this.singleBlockRM = new RequestMaker(singleBlockUrl);
//        this.multipleBlockRM = new RequestMaker(multipleBlockUrl);
//    }
//
//    private String formatPayload(CodeBlock block) {
//        double[] blockFeatures = block.getOomLanguageFeatures();
//    }
//
//    @Override
//    public boolean accepts(CodeBlock block) {
//        return false;
//    }
//
//    @Override
//    public List<CodeBlock> filter(Collection<CodeBlock> blocks) {
//        return null;
//    }
//}
