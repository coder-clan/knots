package org.coderclan.knots.annotation;

import org.coderclan.knots.ArgumentIdempotentIdFetcher;

import java.lang.annotation.*;

/**
 * IdempotentIdExpression indicates that annotated methods or All methods in annotated class are idempotent,
 * and the Idempotent ID will be calculated by the SPEL expression presented by {@link #value()}.
 * It is used by {@link ArgumentIdempotentIdFetcher}.
 *
 * @author aray(dot)chou(dot)cn(at)gmail(dot)com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
@Idempotent
public @interface IdempotentIdExpression {

    /**
     * SPEL expression which is used to calculate Idempotent ID (which should be unique in the universe).
     * Parameters of the method are put into the EL context with key "arg", the first parameter can be access by "arg[0]", and the second one is arg[1], and so on.
     *
     * @return SPEL Expression  which is used to calculate Idempotent ID.
     */
    String value();
}
