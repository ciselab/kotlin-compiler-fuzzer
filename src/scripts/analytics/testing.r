file_model <- read.csv("file_model.csv")
all_data <- read.csv("all_data.csv")

m1 <- aov(cog + mcckloc + smellskloc
    ~ func * do_while * assignment * try_catch * if_expr * elvis_op,
    all_data)

summary(m1)

print("------------------------")

m2 <- aov(cog + smells + mcckloc + smellskloc
    ~ func * do_while * assignment * try_catch * if_expr * elvis_op,
    all_data)

summary(m2)

m3 <- aov(lloc + cog + smells + mcckloc + smellskloc
    ~ func * do_while * assignment * try_catch * if_expr * elvis_op,
    all_data)

summary(m3)

m4 <- aov(lloc + mcc + cog + smells + smellskloc
    ~ func * do_while * assignment * try_catch * if_expr * elvis_op,
    all_data)

summary(m4)

m5 <- aov(lloc + mcckloc
    ~ func * do_while * assignment * try_catch * if_expr * elvis_op,
    all_data)

summary(m5)

m6 <- aov(lloc + mcc + mcckloc
    ~ func * do_while * assignment * try_catch * if_expr * elvis_op,
    all_data)

summary(m6)

m7 <- aov(lloc + mcc + mcckloc + smellskloc
    ~ func * do_while * assignment * try_catch * if_expr * elvis_op,
    all_data)

summary(m7)

m8 <- aov(lloc + mcc + mcckloc + smellskloc
    ~ func * do_while * assignment * try_catch * if_expr * elvis_op,
    all_data)

summary(m8)

m9 <- aov(lloc + mcc + smells + mcckloc + smellskloc
    ~ func * do_while * assignment * try_catch * if_expr * elvis_op,
    all_data)

summary(m9)

m10 <- aov(lloc + mcc + smells + smellskloc
    ~ func * do_while * assignment * try_catch * if_expr * elvis_op,
    all_data)

summary(m10)

m11 <- aov(mcc + smells
    ~ func * do_while * assignment * try_catch * if_expr * elvis_op,
    all_data)

summary(m11)

m12 <- aov(sloc + mcc
    ~ func * do_while * assignment * try_catch * if_expr * elvis_op,
    all_data)

summary(m12)