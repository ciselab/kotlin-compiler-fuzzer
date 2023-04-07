#!/usr/bin/env Rscript

library(ggplot2)
library(plyr)

# Plot the file model correlation
file_model <- read.csv("filemodel.csv")

ggplot(data = head(file_model, 25), aes(x = criterion, y = mean_sq)) +
    geom_bar(stat = "identity", color = "black", position = position_dodge()) +
    theme_minimal() +
    theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust = 1)) +
    xlab("Criterion") +
    ylab("Mean Square of ANOVA test") +
    ggtitle("ANOVA test results for file model (1 of 4)", subtitle = waiver())

ggplot(data = tail(head(file_model, 50), 25), aes(x = criterion, y = mean_sq)) +
    geom_bar(stat = "identity", color = "black", position = position_dodge()) +
    theme_minimal() +
    theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust = 1)) +
    xlab("Criterion") +
    ylab("Mean Square of ANOVA test") +
    ggtitle("ANOVA test results for file model (2 of 4)", subtitle = waiver())

ggplot(data = tail(head(file_model, 75), 25), aes(x = criterion, y = mean_sq)) +
    geom_bar(stat = "identity", color = "black", position = position_dodge()) +
    theme_minimal() +
    theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust = 1)) +
    xlab("Criterion") +
    ylab("Mean Square of ANOVA test") +
    ggtitle("ANOVA test results for file model (3 of 4)", subtitle = waiver())

ggplot(data = tail(file_model, 24), aes(x = criterion, y = mean_sq)) +
    geom_bar(stat = "identity", color = "black", position = position_dodge()) +
    theme_minimal() +
    theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust = 1)) +
    xlab("Criterion") +
    ylab("Mean Square of ANOVA test") +
    ggtitle("ANOVA test results for file model (4 of 4)", subtitle = waiver())

# Plot the compiler model correlation
compiler_model <- read.csv("compilermodel.csv")

ggplot(data = compiler_model, aes(x = criterion, y = mean_sq)) +
    geom_bar(stat = "identity", color = "black", position = position_dodge()) +
    theme_minimal() +
    theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust = 1)) +
    xlab("Criterion") +
    ylab("Mean Square of ANOVA test") +
    ggtitle("ANOVA test results for compiler model", subtitle = waiver())

# Plot the aicc results
aicc_data <- read.csv("aicc.csv")

ggplot(data = aicc_data, aes(x = model, y = -aicc)) +
    geom_bar(stat = "identity", color = "black", position = position_dodge()) +
    theme_minimal() +
    theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust = 1)) +
    xlab("Model") +
    ylab("Negative Akaike Information Criterion") +
    ggtitle("AICC analysis of the models", subtitle = waiver())
