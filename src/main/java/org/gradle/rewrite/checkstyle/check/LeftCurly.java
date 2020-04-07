package org.gradle.rewrite.checkstyle.check;

import lombok.Builder;
import org.gradle.rewrite.checkstyle.policy.LeftCurlyPolicy;
import org.gradle.rewrite.checkstyle.policy.Token;
import org.openrewrite.Cursor;
import org.openrewrite.Tree;
import org.openrewrite.java.refactor.JavaRefactorVisitor;
import org.openrewrite.java.tree.J;

import java.util.Set;

import static org.gradle.rewrite.checkstyle.policy.LeftCurlyPolicy.EOL;
import static org.gradle.rewrite.checkstyle.policy.LeftCurlyPolicy.NL;
import static org.gradle.rewrite.checkstyle.policy.Token.*;

@Builder
public class LeftCurly extends JavaRefactorVisitor {
    @Builder.Default
    private final LeftCurlyPolicy option = EOL;

    @Builder.Default
    private final boolean ignoreEnums = true;

    @Builder.Default
    private final Set<Token> tokens = Set.of(
            ANNOTATION_DEF,
            CLASS_DEF,
            CTOR_DEF,
            ENUM_CONSTANT_DEF,
            ENUM_DEF,
            INTERFACE_DEF,
            LAMBDA,
            LITERAL_CASE,
            LITERAL_CATCH,
            LITERAL_DEFAULT,
            LITERAL_DO,
            LITERAL_ELSE,
            LITERAL_FINALLY,
            LITERAL_FOR,
            LITERAL_IF,
            LITERAL_SWITCH,
            LITERAL_SYNCHRONIZED,
            LITERAL_TRY,
            LITERAL_WHILE,
            METHOD_DEF,
            OBJBLOCK,
            STATIC_INIT
    );

    @Override
    public String getName() {
        return "checkstyle.LeftCurly";
    }

    @Override
    public boolean isCursored() {
        return true;
    }

    @Override
    public J visitBlock(J.Block<J> block) {
        J.Block<J> b = refactor(block, super::visitBlock);

        Cursor containing = getCursor().getParentOrThrow();

        boolean spansMultipleLines = LeftCurlyPolicy.NLOW.equals(option) ?
                new SpansMultipleLines(containing.getTree(), block).visit((Tree) containing.getTree()) : false;

        if (!satisfiesPolicy(option, block, containing.getTree(), spansMultipleLines)) {
            b = formatCurly(option, b, spansMultipleLines, containing);
        }

        return b;
    }

    private boolean satisfiesPolicy(LeftCurlyPolicy option, J.Block<J> block, Tree containing, boolean spansMultipleLines) {
        switch (option) {
            case EOL:
                if (ignoreEnums && containing instanceof J.Case) {
                    return true;
                }

                if (block.getStatic() == null) {
                    return !block.getFormatting().getPrefix().contains("\n");
                } else {
                    return !block.getStatic().getFormatting().getSuffix().contains("\n");
                }
            case NL:
                if (block.getStatic() == null) {
                    return block.getFormatting().getPrefix().contains("\n");
                } else {
                    return block.getStatic().getFormatting().getSuffix().contains("\n");
                }
            case NLOW:
            default:
                return (spansMultipleLines && satisfiesPolicy(NL, block, containing, true)) ||
                        (!spansMultipleLines && satisfiesPolicy(EOL, block, containing, false));
        }
    }

    private static J.Block<J> formatCurly(LeftCurlyPolicy option, J.Block<J> block, boolean spansMultipleLines, Cursor containing) {
        switch (option) {
            case EOL:
                // a non-static class initializer can remain as it is
                Tree parent = containing.getParentOrThrow().getTree();
                if ((parent instanceof J.ClassDecl || parent instanceof J.NewClass) && block.getStatic() == null) {
                    return block;
                }

                return block.getStatic() == null ? block.withPrefix(" ") : block.withStatic(block.getStatic().withSuffix(" "));
            case NL:
                return block.getStatic() == null ? block.withPrefix(block.getEndOfBlockSuffix()) :
                        block.withStatic(block.getStatic().withSuffix(block.getEndOfBlockSuffix()));
            case NLOW:
            default:
                return formatCurly(spansMultipleLines ? NL : EOL, block, spansMultipleLines, containing);
        }
    }
}
