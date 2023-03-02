package org.fuzzer.grammar.ast;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.RuleName;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.representations.callables.KFunction;
import org.fuzzer.representations.callables.KIdentifierCallable;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.context.KScope;
import org.fuzzer.representations.types.KClassifierType;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;
import org.fuzzer.utils.Tuple;

import java.util.ArrayList;
import java.util.List;

public class FunctionDecl extends ASTNode {
    public FunctionDecl(GrammarAST antlrNode, List<ASTNode> children) {
        super(antlrNode, children);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx) {
        switch (ctx.getScope()) {
            // A simple function declaration
            case GLOBAL_SCOPE -> {
                CodeFragment code = new CodeFragment();
                code.appendToText(RuleName.fun + " ");
                String funcName = ctx.getNewIdentifier();
                code.appendToText(funcName + " (");

                // Sample a return type
                KClassifierType returnType = (KClassifierType) ctx.getRandomSamplableType();

                // Sample some parameters
                int numberOfParams = rng.fromGeometric();
                ParameterNode parameterNode = new ParameterNode(this.antlrNode);
                List<KType> sampledTypes = new ArrayList<>();
                List<String> sampledIds = new ArrayList<>();

                // Clone after adding the function to allow for recursion
                ctx.addIdentifier(funcName, new KFunction(funcName, sampledTypes, returnType));
                Context clone = ctx.clone();

                for (int i = 0; i < numberOfParams; i++) {
                    CodeFragment sampledParam = parameterNode.getSample(rng, clone);

                    // Cache the sample parameters
                    sampledTypes.add(parameterNode.getSampledType());
                    sampledIds.add(parameterNode.getSampledId());

                    clone.addIdentifier(parameterNode.getSampledId(), new KIdentifierCallable(parameterNode.getSampledId(), parameterNode.getSampledType()));

                    code.appendToText(sampledParam);
                }

                // Change the scope to a function
                clone.updateScope(KScope.FUNCTION_SCOPE);

                ExpressionNode expr = new ExpressionNode(antlrNode, 3);
                expr.recordStatistics(this.stats);

                // Sample a consistently-types return statement
                Tuple<CodeFragment, List<KType>> returnStatementAndInstances = expr.getSampleOfType(rng, ctx, returnType);

                code.appendToText(") : " + returnType.codeRepresentation(returnStatementAndInstances.second()) + " {" + System.lineSeparator());

                // Sample some expressions in the function body
                int numberOfStatements = rng.fromGeometric();

                for (int i = 0; i < numberOfStatements; i++) {
                    CodeFragment sampleExpr = expr.getSample(rng, ctx);
                    code.extend(sampleExpr);
                }

                code.extend("return " + returnStatementAndInstances.first());
                code.extend(new CodeFragment("}"));

                // Record this sample
                if (this.stats != null) {
                    stats.increment(SampleStructure.FUNCTION);
                }

                return code;
            }
            default -> {
                throw new UnsupportedOperationException("Cannot yet sample from " + ctx.getScope() + " scopes.");
            }
        }
    }

    @Override
    public void invariant() {

    }
}
