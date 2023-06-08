#!/usr/bin/env Rscript

library(AICcmodavg)
library(optparse)

option_list <- list(
    make_option(c("-i", "--input"),
        type = "character", default = NULL,
        help = "dataset file name", metavar = "character"
    ),
    make_option(c("-o", "--output"),
        type = "character", default = "stdout",
        help = "output file name", metavar = "character"
    ),
    make_option(c("-f", "--file-model"),
        type = "logical", default = TRUE,
        help = "whether to output the file model [default= %default]",
        metavar = "logical", dest = "file"
    ),
        make_option(c("-c", "--compiler-model"),
        type = "logical", default = TRUE,
        help = "whether to output the compiler model [default= %default]",
        metavar = "logical", dest = "comp"
    ),
        make_option(c("-l", "--lang-model"),
        type = "logical", default = TRUE,
        help = "whether to output the language model [default= %default]",
        metavar = "logical", dest = "lang"
    ),
    make_option(c("-a", "--aicc"),
        type = "logical", default = TRUE,
        help = "whether to compute the second-order Akaike Information Criterion [default= %default]",
        metavar = "logical", dest = "aicc"
    )
)

opt <- parse_args(OptionParser(option_list = option_list))

if (opt$aicc && !(opt$file && opt$comp)) {
    stop("Aicc is only enabled when both models are computed.", call. = NULL)
}

dataset <- read.csv(opt$input)
if (opt$output != "stdout") {
    sink(opt$output)
}

if (opt$file) {
    file_model <- aov(
        crash ~ loc * sloc * lloc * mcc * cog * smells * mcckloc * smellskloc,
        data = dataset
    )

    print(summary(file_model))
}

if (opt$comp) {
    compiler_model <- aov(
        crash ~ k1_time * k2_time * k1_mem * k2_mem,
        data = dataset
    )

    print(summary(compiler_model))
}

if (opt$lang) {
    lang_model <- aov(
        crash ~ do_while * assignment * try_catch * if_expr * elvis_op,
        data = dataset
    )

    print(summary(lang_model))
}

if (opt$aicc) {
    model_list <- list(compiler_model, file_model, lang_model)
    model_names <- c("compiler_model", "file_model", "lang_model")

    aicc <- aictab(cand.set = model_list, modnames = model_names, sort = TRUE)
    print(aicc)
}

sink()