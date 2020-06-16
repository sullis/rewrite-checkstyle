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
package org.openrewrite.checkstyle

import org.junit.jupiter.api.Test

open class FinalLocalVariableTest: CheckstyleRefactorVisitorTest(FinalLocalVariable::class) {
    @Test
    fun localVariablesAreMadeFinal() {
        val a = jp.parse("""
            public class A {
                {
                    int n = 1;
                    for(int i = 0; i < n; i++) {
                    }
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(configXml()).fix().fixed

        assertRefactored(fixed, """
            public class A {
                {
                    final int n = 1;
                    for(int i = 0; i < n; i++) {
                    }
                }
            }
        """)
    }

    @Test
    fun multiVariables() {
        val a = jp.parse("""
            public class A {
                {
                    int a, b = 1;
                    a = 0;
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(configXml()).fix().fixed

        // the final only applies to any initialized variables (b in this case)
        assertRefactored(fixed, """
            public class A {
                {
                    final int a, b = 1;
                    a = 0;
                }
            }
        """)
    }
}
