package org.fuzzer.grammar.ast.structures;

import org.antlr.v4.tool.ast.GrammarAST;
import org.fuzzer.generator.CodeFragment;
import org.fuzzer.grammar.RuleName;
import org.fuzzer.grammar.SampleStructure;
import org.fuzzer.grammar.ast.ASTNode;
import org.fuzzer.grammar.ast.expressions.ExpressionNode;
import org.fuzzer.grammar.ast.statements.StatementNode;
import org.fuzzer.representations.callables.KCallable;
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
    public FunctionDecl(GrammarAST antlrNode, List<ASTNode> children) {
        super(antlrNode, children);
    }

    @Override
    public CodeFragment getSample(RandomNumberGenerator rng, Context ctx, Set<String> generatedCallableDependencies) {
        // Stores the dependencies in the statements and expressions of this function
        Set<String> dependentVariableNames = new HashSet<>();

        switch (ctx.getScope()) {
            // A simple function declaration

            case GLOBAL_SCOPE -> {
                CodeFragment code = new CodeFragment();
                code.appendToText(RuleName.fun + " ");
                String funcName = ctx.getNewIdentifier();
                code.appendToText(funcName + " (");

                // Sample a return type
                KClassifierType returnType = (KClassifierType) ctx.getRandomSamplableType();

                // Sample a consistently-types return statement
                ExpressionNode returnNode = new ExpressionNode(antlrNode, 3);
                returnNode.recordStatistics(stats);
                returnNode.useConfiguration(cfg);

                var returnStatementAndInstances = returnNode.getRandomExpressionNode(rng)
                        .getSampleOfType(rng, ctx, returnType, true, dependentVariableNames);
                returnType = returnType.withNewGenericInstances(returnStatementAndInstances.second().second());

                // Sample some parameters
                int numberOfParams = rng.fromGeometric();
                ParameterNode parameterNode = new ParameterNode(this.antlrNode);
                List<KType> sampledTypes = new ArrayList<>();
                List<String> sampledIds = new ArrayList<>();

                // Clone after adding the function to allow for recursion
                KFunction newCallable = new KFunction(funcName, sampledTypes, returnType);
                newCallable.markAsGenerated();
                ctx.addIdentifier(funcName, newCallable);
                Context clone = ctx.clone();

                for (int i = 0; i < numberOfParams; i++) {
                    CodeFragment sampledParam = parameterNode.getSample(rng, clone, dependentVariableNames);

                    // Cache the sample parameters
                    sampledTypes.add(parameterNode.getSampledType());
                    sampledIds.add(parameterNode.getSampledId());

                    clone.addIdentifier(parameterNode.getSampledId(), new KIdentifierCallable(parameterNode.getSampledId(), parameterNode.getSampledType(), false));

                    code.appendToText(sampledParam);
                }

                // Change the scope to a function
                clone.updateScope(KScope.FUNCTION_SCOPE);

                StatementNode stmtNode = new StatementNode(antlrNode, 3);
                stmtNode.recordStatistics(this.stats);
                stmtNode.useConfiguration(cfg);

                // TODO: adapt this
                boolean allowSubtypes = true;
                code.appendToText(") : " + returnType.codeRepresentation(returnStatementAndInstances.second().second()) + " {" + System.lineSeparator());

                // Sample some expressions in the function body
                int numberOfStatements = rng.fromGeometric();

                for (int i = 0; i < numberOfStatements; i++) {
                    CodeFragment sampleExpr = stmtNode.getSample(rng, clone, dependentVariableNames);
                    code.extend(sampleExpr);
                }

                code.extend("return " + returnStatementAndInstances.first());
                code.extend("}");

                stats.increment(SampleStructure.FUNCTION);
                // TODO: do this for each node, not just functions.
                stats.incrementBy(SampleStructure.CHARS, code.size());

                // Set the structure name so that it can be
                // Used during recombination
                code.setName(funcName);

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
