package org.gradle.rewrite.checkstyle.check

import com.netflix.rewrite.parse.OpenJdkParser
import com.netflix.rewrite.parse.Parser
import org.gradle.rewrite.checkstyle.policy.Token.LITERAL_CASE
import org.gradle.rewrite.checkstyle.policy.Token.LITERAL_DEFAULT
import org.junit.jupiter.api.Test

open class NeedBracesTest : Parser by OpenJdkParser() {
    @Test
    fun addBraces() {
        val a = parse("""
            public class A {
                {
                    while (true);
                    if (n == 1) return true;
                    else return true;
                    while (true) return true;
                    do this.notify(); while (true);
                    for (int i = 0; ; ) this.notify();
                }
            }
        """.trimIndent())

        val fixed = a.refactor().run(NeedBraces.builder().build()).fix()

        assertRefactored(fixed, """
            public class A {
                {
                    while (true) {
                    }
                    if (n == 1) {
                        return true;
                    }
                    else {
                        return true;
                    }
                    while (true) {
                        return true;
                    }
                    do {
                        this.notify();
                    } while (true);
                    for (int i = 0; ; ) {
                        this.notify();
                    }
                }
            }
        """)
    }

    @Test
    fun allowEmptyLoopBody() {
        val a = parse("""
            public class A {
                {
                    while (true);
                    for(int i = 0; i < 10; i++);
                }
            }
        """.trimIndent())

        val fixed = a.refactor().run(NeedBraces.builder()
                .allowEmptyLoopBody(true)
                .build()).fix()

        assertRefactored(fixed, """
            public class A {
                {
                    while (true);
                    for(int i = 0; i < 10; i++);
                }
            }
        """)
    }

    @Test
    fun allowSingleLineStatement() {
        val a = parse("""
            public class A {
                {
                    if (n == 1) return true;
                    while (true) return true;
                    do this.notify(); while (true);
                    for (int i = 0; ; ) this.notify();
                }
            }
        """.trimIndent())

        val fixed = a.refactor().run(NeedBraces.builder()
                .allowSingleLineStatement(true)
                .build()).fix()

        assertRefactored(fixed, """
            public class A {
                {
                    if (n == 1) return true;
                    while (true) return true;
                    do this.notify(); while (true);
                    for (int i = 0; ; ) this.notify();
                }
            }
        """)
    }

    @Test
    fun allowSingleLineStatementInSwitch() {
        val a = parse("""
            public class A {
                {
                    int n = 1;
                    switch (n) {
                      case 1: counter++; break;
                      case 6: counter += 10; break;
                      default: counter = 100; break;
                    }
                }
            }
        """.trimIndent())

        val fixed = a.refactor().run(NeedBraces.builder()
                .tokens(setOf(LITERAL_CASE, LITERAL_DEFAULT))
                .allowSingleLineStatement(true)
                .build()).fix()

        assertRefactored(fixed, """
            public class A {
                {
                    int n = 1;
                    switch (n) {
                      case 1: counter++; break;
                      case 6: counter += 10; break;
                      default: counter = 100; break;
                    }
                }
            }
        """)
    }
}
