# Knots

The Knots is an <a href="https://en.wikipedia.org/wiki/Idempotence">Idempotence</a> component for the Java Spring Boot
framework. It is named Knots since ancient people use knotted string to record. (Inca people
use <a href="https://en.wikipedia.org/wiki/Quipu">Quipu</a>, which is a kind of knotted strings, to keep records.

Knots does the following to implement Idempotence:

- Intercept method invocations which are annotated with @Idempotent.
- Extract an <code>Idempotent ID</code>
- Set a lock of the Idempotent ID to prevent other threads to execute the invocation with the same Idempotent ID.
- Invoke the method and get the return value of the method if successfully locked, persist the return value with
  Idempotent ID (to database).
- Failing to set the lock of the Idempotent ID, it means some other thread started to execute the invocation already.
  Wait the other thread finishing the invocation and get the persisted return value by Idempotent ID (from database) and
  return the persisted return value.

## Design

## How to use

The module knots-demo demonstrate how to use the Knots.

- Depend on the Knots in maven pom.xml.

<pre>
        &lt;dependency>
            &lt;groupId>org.coderclan&lt;/groupId>
            &lt;artifactId>knots&lt;/artifactId>
        &lt;/dependency>
</pre>

- Implement <code>org.coderclan.knots.ResultChecker</code> and register it as Spring Bean. e.g. <code>
  org.coderclan.knots.ResultChecker</code>
- Annotate the method which need to be idempotent with <code>
  @org.coderclan.knots.annotation.IdempotentIdExpression</code>. e.g. <code>
  org.coderclan.knots.demo.TestService.increaseRewardPoint()</code>

## Configuration