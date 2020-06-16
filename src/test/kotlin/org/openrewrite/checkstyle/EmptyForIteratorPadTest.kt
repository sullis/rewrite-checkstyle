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
import org.openrewrite.checkstyle.policy.PadPolicy

open class EmptyForIteratorPadTest: CheckstyleRefactorVisitorTest(EmptyForIteratorPad::class) {
    @Test
    fun doesntChangeIfIteratorIsPresent() {
        val a = jp.parse("""
            public class A {
                {
                    for (int i = 0; i < 2; i++ );
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(configXml())
                .fix().fixed

        assertRefactored(fixed, """
            public class A {
                {
                    for (int i = 0; i < 2; i++ );
                }
            }
        """)
    }

    @Test
    fun noSpaceInitializerPadding() {
        val a = jp.parse("""
            public class A {
                {
                    for (int i = 0; i < 2; );
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(configXml())
                .fix().fixed

        assertRefactored(fixed, """
            public class A {
                {
                    for (int i = 0; i < 2;);
                }
            }
        """)
    }

    @Test
    fun spaceInitializerPadding() {
        val a = jp.parse("""
            public class A {
                {
                    for (int i = 0; i < 2;);
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(configXml("option" to PadPolicy.SPACE))
                .fix().fixed

        assertRefactored(fixed, """
            public class A {
                {
                    for (int i = 0; i < 2; );
                }
            }
        """)
    }

    @Test
    fun noCheckIfIteratorEndsWithLineTerminator() {
        val a = jp.parse("""
            public class A {
                {
                    for (int i = 0; i < 2;
                        );
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(configXml())
                .fix().fixed

        assertRefactored(fixed, """
            public class A {
                {
                    for (int i = 0; i < 2;
                        );
                }
            }
        """)
    }
}
