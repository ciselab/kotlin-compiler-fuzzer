@echo off

rem Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
rem Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.

setlocal
set _KOTLIN_COMPILER=org.jetbrains.kotlin.cli.js.K2JSCompiler

call %~dps0kotlinc.bat %*
