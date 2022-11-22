package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object delegate;
  private final ProfilingState profilingState;

  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState profilingState) {
    this.clock = clock;
    this.delegate = delegate;
    this.profilingState = profilingState;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    Object invokedObject;

    Instant startTime = clock.instant();

    try {
      invokedObject =  method.invoke(delegate, args);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    } finally {
      if (!Objects.isNull(method.getAnnotation(Profiled.class))) {
        Instant endTime = clock.instant();
        profilingState.record(delegate.getClass(), method, Duration.between(startTime, endTime));
      }
    }

    return invokedObject;
  }
}
