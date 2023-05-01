package org.fuzzer.search.fitness;

import org.fuzzer.search.chromosome.CodeBlock;
public class StructureMOFitness implements MOFitnessFunction {

    private final int numberOfObjectives = 13;

    private final SOFitnessFunction[] objectives;

//    file,time,chars,cls,attr,func,method,constr,simple_expr,do_while,assignment,try_catch,if_expr,elvis_op,simple_stmt
    public StructureMOFitness() {

        objectives = new SOFitnessFunction[numberOfObjectives];
        objectives[0] = new SizeSOFitness();

        for (int i = 1; i < numberOfObjectives; i++) {
            objectives[i] = new SimpleSOFitness(i);
        }
    }

    @Override
    public double[] evaluate(CodeBlock individual) {
        double[] res = new double[numberOfObjectives];

        for (int i = 0; i < numberOfObjectives; i++) {
            res[i] = objectives[i].evaluate(individual);
        }

        return res;
    }
}
