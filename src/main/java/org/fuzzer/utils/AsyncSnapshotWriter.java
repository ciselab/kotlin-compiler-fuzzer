package org.fuzzer.utils;

import org.fuzzer.search.chromosome.CodeBlock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class AsyncSnapshotWriter extends Thread {
    private final String outputDirectory;

    private final List<CodeBlock> blocks;

    private final File statsFile;

    public AsyncSnapshotWriter(String outputDirectory, List<CodeBlock> blocks) {
        this.outputDirectory = outputDirectory;

        Path path = Paths.get(outputDirectory);
        if (!Files.isDirectory(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        this.blocks = blocks;
        this.statsFile = new File(outputDirectory + "/stats.csv");
    }

    @Override
    public void run() {

        System.out.println("Writing snapshot to " + outputDirectory);

        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Write the statistics of the run
        BufferedWriter statsWriter;

        try {
            statsWriter = new BufferedWriter(new FileWriter(statsFile.getAbsolutePath(), true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            statsWriter.write("file,time,chars,cls,attr,func,method,constr,simple_expr,do_while,assignment,try_catch,if_expr,elvis_op,simple_stmt,k1_exit,k1_time,k1_mem,k1_sz,k2_exit,k2_time,k2_mem,k2_sz,loc,sloc,lloc,cloc,mcc,cog,smells,cmm_ratio,mcckloc,smellskloc");
            statsWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        for (CodeBlock code : blocks) {
            String randomFileName = UUID.randomUUID().toString();
            String outputFileName = outputDirectory + "/" + randomFileName + ".kt";

//            System.out.println("Writing to " + outputFileName);
//            System.out.println(new File(outputFileName).isFile());
//            System.out.println(new File(directoryOutput).isDirectory());

            String text = "fun main(args: Array<String>) {\n";
            text += code.text();
            text += "\n}";

            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(outputFileName));
                writer.write(text);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                statsWriter.newLine();
                statsWriter.write(randomFileName + "," + code.stats().csv());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            statsWriter.newLine();
            statsWriter.flush();
            statsWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
