/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.checkstyle.check

import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser

open class DefaultComesLastTest : JavaParser() {
    @Test
    fun moveDefaultToLastAlongWithItsStatementsAndAddBreakIfNecessary() {
        val a = parse("""
            class Test {
                int n;
                {
                    switch (n) {
                        case 1:
                            break;
                        case 2:
                            break;
                        default:
                            System.out.println("default");
                            break;
                        case 3:
                            System.out.println("case3");
                    }
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(_root_ide_package_.org.openrewrite.checkstyle.check.DefaultComesLast()).fix().fixed

        _root_ide_package_.org.openrewrite.checkstyle.check.assertRefactored(fixed, """
            class Test {
                int n;
                {
                    switch (n) {
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            System.out.println("case3");
                            break;
                        default:
                            System.out.println("default");
                    }
                }
            }
        """)
    }

    @Test
    fun moveDefaultToLastWhenSharedWithAnotherCaseStatement() {
        val a = parse("""
            class Test {
                int n;
                {
                    switch (n) {
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                        default:
                            break;
                        case 4:
                        case 5:
                    }
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(_root_ide_package_.org.openrewrite.checkstyle.check.DefaultComesLast()).fix().fixed

        _root_ide_package_.org.openrewrite.checkstyle.check.assertRefactored(fixed, """
            class Test {
                int n;
                {
                    switch (n) {
                        case 1:
                            break;
                        case 2:
                            break;
                        case 4:
                        case 5:
                            break;
                        case 3:
                        default:
                    }
                }
            }
        """)
    }

    @Test
    fun skipIfLastAndSharedWithCase() {
        val a = parse("""
            class Test {
                int n;
                {
                    switch (n) {
                        case 1:
                            break;
                        case 2:
                        default:
                            break;
                        case 3:
                            break;
                    }
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(_root_ide_package_.org.openrewrite.checkstyle.check.DefaultComesLast(true)).fix().fixed

        _root_ide_package_.org.openrewrite.checkstyle.check.assertRefactored(fixed, """
            class Test {
                int n;
                {
                    switch (n) {
                        case 1:
                            break;
                        case 2:
                        default:
                            break;
                        case 3:
                            break;
                    }
                }
            }
        """)
    }

    @Test
    fun defaultIsLastAndThrows() {
        assertUnchangedByRefactoring(_root_ide_package_.org.openrewrite.checkstyle.check.DefaultComesLast(), """
            class Test {
                int n;
                {
                    switch (n) {
                        case 1:
                            break;
                        default:
                            throw new RuntimeException("unexpected value");
                    }
                }
            }
        """)
    }

    @Test
    fun defaultIsLastAndReturnsNonVoid() {
        assertUnchangedByRefactoring(_root_ide_package_.org.openrewrite.checkstyle.check.DefaultComesLast(), """
            class Test {
                public int foo(int n) {
                    switch (n) {
                        case 1:
                            return 1;
                        default:
                            return 2;
                    }
                }
            }
        """)
    }

    @Test
    fun dontAddBreaksIfCasesArentMoving() {
        assertUnchangedByRefactoring(_root_ide_package_.org.openrewrite.checkstyle.check.DefaultComesLast(), """
            class Test {
                int n;
                boolean foo() {
                    switch (n) {
                        case 1:
                        case 2:
                            System.out.println("side effect");
                        default:
                            return true;
                    }
                }
            }
        """)
    }

    @Test
    fun dontRemoveExtraneousDefaultCaseBreaks() {
        assertUnchangedByRefactoring(_root_ide_package_.org.openrewrite.checkstyle.check.DefaultComesLast(), """
            class Test {
                int n;
                void foo() {
                    switch (n) {
                        default:
                            break;
                    }
                }
            }
        """)
    }

    @Test
    fun allCasesGroupedWithDefault() {
        assertUnchangedByRefactoring(_root_ide_package_.org.openrewrite.checkstyle.check.DefaultComesLast(), """
            class Test {
                int n;
                boolean foo() {
                    switch (n) {
                        case 1:
                        case 2:
                        default:
                            return true;
                    }
                }
            }
        """)
    }
}