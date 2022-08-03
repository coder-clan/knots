package org.coderclan.knots.annotation;

import java.lang.annotation.*;

/**
 * Indicate that a method annotated by @Idempotent is idempotent.
 * That is if invokes the method several times with the same idempotent ID, only the first invoke will be processed,
 * the following invokes do NOT touch data and return the same value as the first invoke returned.
 *
 * @author aray(dot)chou(dot)cn(at)gmail(dot)com
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Idempotent {

}
