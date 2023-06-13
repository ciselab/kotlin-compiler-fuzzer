package org.fuzzer.grammar.ast.structures;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.configuration.Configuration;
import org.fuzzer.dt.FuzzerStatistics;
import org.fuzzer.representations.callables.KCallable;
import org.fuzzer.search.chromosome.*;
import org.fuzzer.grammar.RuleName;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.expressions.ExpressionNode;
import org.fuzzer.grammar.ast.statements.StatementNode;
import org.fuzzer.representations.callables.KFunction;
import org.fuzzer.representations.callables.KIdentifierCallable;
import org.fuzzer.representations.context.Context;
import org.fuzzer.representations.context.KScope;
import org.fuzzer.representations.types.KClassifierType;
import org.fuzzer.representations.types.KType;
import org.fuzzer.utils.RandomNumberGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FunctionDecl extends ASTNode {
    public FunctionDecl(GrammarAST antlrNode, List<ASTNode> children, FuzzerStatistics stats, Configuration cfg) {
        super(antlrNode, children, stats, cfg);
    }

    @Override
    public CodeSnippet getSample(RandomNumberGenerator rng, Context ctx) {

        switch (ctx.getScope()) {
            // A simple function declaration

            case GLOBAL_SCOPE -> {
                String funcName = ctx.getNewIdentifier();
                CodeFragment code = CodeFragment
                        .emptyFragmentOfType(FragmentType.FUNC)
                        .append(RuleName.fun + " ")
                        .append(funcName + " (");

                // Sample a return type
                KClassifierType returnType = (KClassifierType) ctx.getRandomSamplableType();

                // Sample a consistently-types return statement
                ExpressionNode returnNode = new ExpressionNode(antlrNode, 3, stats, cfg);

                var returnStatementAndInstances = returnNode.getRandomExpressionNode(rng)
                        .getSampleOfType(rng, ctx, returnType, true);
                returnType = returnType.withNewGenericInstances(returnStatementAndInstances.second().second());

                // Sample some parameters
                int numberOfParams = rng.fromDiscreteDistribution(cfg.getFuncParamsDist());

                ParameterNode parameterNode = new ParameterNode(this.antlrNode);

                List<KType> sampledTypes = new ArrayList<>();
                List<String> sampledIds = new ArrayList<>();

                // Clone after adding the function to allow for recursion
                KFunction newCallable = new KFunction(funcName, sampledTypes, returnType);
                newCallable.markAsGenerated();
                ctx.addIdentifier(funcName, newCallable);
                Context innerContext = ctx.clone();

                for (int i = 0; i < numberOfParams; i++) {
                    CodeFragment sampledParam = parameterNode.getSample(rng, innerContext);

                    // Cache the sample parameters
                    sampledTypes.add(parameterNode.getSampledType());
                    sampledIds.add(parameterNode.getSampledId());

                    innerContext.addIdentifier(parameterNode.getSampledId(), new KIdentifierCallable(parameterNode.getSampledId(), parameterNode.getSampledType(), false));

                    code = code.append(sampledParam);
                }

                // Change the scope to a function
                innerContext.updateScope(KScope.FUNCTION_SCOPE);

                StatementNode stmtNode = new StatementNode(antlrNode, 3, stats, cfg);

                code = code.append(") : " + returnType.codeRepresentation(returnStatementAndInstances.second().second()) + " {" + System.lineSeparator());

                // Sample some expressions in the function body
                int numberOfStatements = rng.fromDiscreteDistribution(cfg.getFuncStmtsDist());

                for (int i = 0; i < numberOfStatements; i++) {
                    CodeFragment sampleExpr = stmtNode.getSample(rng, innerContext);
                    code = code.extend(sampleExpr);
                }

                code = code.extend("return " + returnStatementAndInstances.first())
                        .extend("}");

                stats.increment(SampleStructure.FUNCTION);
                // TODO: do this for each node, not just functions.
                stats.incrementBy(SampleStructure.CHARS, code.size());

                Set<KCallable> providedCallables = new HashSet<>();
                providedCallables.add(newCallable);

                // Set the structure name so that it can be
                // Used during recombination
                return new CodeSnippet(code, funcName, code.callableDependencies(),  newCallable, stats.clone(), SnippetType.FUNC);
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
