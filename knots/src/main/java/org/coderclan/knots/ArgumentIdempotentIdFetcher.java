package org.coderclan.knots;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.coderclan.knots.annotation.IdempotentIdExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Calculate Idempotent Id by SPEL expression. The SPEL expression is presented by {@link IdempotentIdExpression#value()}.
 *
 * @author aray(dot)chou(dot)cn(at)gmail(dot)com
 */
public class ArgumentIdempotentIdFetcher implements IdempotentIdFetcher {
    private static final Logger log = LoggerFactory.getLogger(ArgumentIdempotentIdFetcher.class);

    /**
     * Calculate Idempotent Id by SPEL expression. This method look for {@link IdempotentIdExpression} annotation on the Method or Class,
     * use return value of {@link IdempotentIdExpression#value()} as SPEL expression, put Parameters of the method into the EL context with key "arg",
     * and then return the value of the SPEL expression.
     *
     * @param proceedingJoinPoint
     * @return return the Idempotent ID calculated by the SPEL represented by {@link IdempotentIdExpression#value()}.
     */
    @Override
    public String getIdempotentId(ProceedingJoinPoint proceedingJoinPoint) {

        // find @IdempotentIdExpression on method
        IdempotentIdExpression idempotentIdExpression = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod().getAnnotation(IdempotentIdExpression.class);

        if (idempotentIdExpression == null) {
            // find @IdempotentIdExpression from class or its super class, if there is no @IdempotentIdExpression on method.
            idempotentIdExpression = AnnotationUtils.findAnnotation(proceedingJoinPoint.getTarget().getClass(), IdempotentIdExpression.class);
        }

        if (idempotentIdExpression == null) {
            throw new IllegalStateException("No @IdempotentIdExpression found on " + ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod() + " or its Class");
        }

//        if (idempotentIdExpression.value() == null) {
//            throw new IllegalStateException("idempotentIdExpression.value() can NOT be null.");
//        }

        Expression exp = getExpression(proceedingJoinPoint, idempotentIdExpression);
        IdempotentElRoot root = new IdempotentElRoot(proceedingJoinPoint.getArgs());
        Object idempotentId = exp.getValue(root);
        return idempotentId == null ? null : idempotentId.toString();
    }


    private static final ConcurrentHashMap<String, Expression> EL_EXPRESSION_CACHE = new ConcurrentHashMap<>();

    private Expression getExpression(ProceedingJoinPoint joinPoint, IdempotentIdExpression idempotent) {

        String key = joinPoint.getSignature().toLongString();

        Expression exp = EL_EXPRESSION_CACHE.get(key);
        if (exp == null) {
            synchronized (EL_EXPRESSION_CACHE) {
                exp = EL_EXPRESSION_CACHE.get(key);
                if (exp == null) {
                    String idempotentIdEl = idempotent.value();
                    ExpressionParser parser = new SpelExpressionParser();
                    exp = parser.parseExpression(idempotentIdEl);
                    EL_EXPRESSION_CACHE.put(key, exp);
                }
            }
        }
        return exp;
    }
}
