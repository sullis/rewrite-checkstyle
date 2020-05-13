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

import org.openrewrite.java.JavaParser
import org.junit.jupiter.api.Test

open class EmptyStatementTest: JavaParser() {
    @Test
    fun removeEmptyStatement() {
        val a = parse("""
            public class A {
                {
                    if(1 == 2);
                        System.out.println("always runs");
                    for(;;);
                        System.out.println("always runs");
                    for(String s : new String[0]);
                        System.out.println("always runs");
                    while(true);
                        System.out.println("always runs");
                    while(true);
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(EmptyStatement()).fix().fixed

        assertRefactored(fixed, """
            public class A {
                {
                    if(1 == 2)
                        System.out.println("always runs");
                    for(;;)
                        System.out.println("always runs");
                    for(String s : new String[0])
                        System.out.println("always runs");
                    while(true)
                        System.out.println("always runs");
                }
            }
        """)
    }
}
