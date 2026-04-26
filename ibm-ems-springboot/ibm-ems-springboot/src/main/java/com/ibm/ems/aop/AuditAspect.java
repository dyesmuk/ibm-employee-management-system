package com.ibm.ems.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    // Pointcut: all public methods in controllers
    @Pointcut("execution(public * com.ibm.ems.controller..*(..))")
    public void controllerMethods() {}

    // Pointcut: all public methods in services
    @Pointcut("execution(public * com.ibm.ems.service..*(..))")
    public void serviceMethods() {}

    @Around("controllerMethods()")
    public Object logController(ProceedingJoinPoint pjp) throws Throwable {
        long start      = System.currentTimeMillis();
        String method   = pjp.getSignature().toShortString();

        log.info("[API] → {}", method);

        Object result = pjp.proceed();

        long elapsed = System.currentTimeMillis() - start;
        log.info("[API] ← {} completed in {}ms", method, elapsed);

        return result;
    }

    /**
     * Log service method timing.
     * Useful for identifying slow queries and N+1 problems.
     */
    @Around("serviceMethods()")
    public Object logService(ProceedingJoinPoint pjp) throws Throwable {
        long start    = System.currentTimeMillis();
        Object result = pjp.proceed();
        long elapsed  = System.currentTimeMillis() - start;

        if (elapsed > 500) {
            // Warn on slow service calls — may indicate missing DB indexes
            log.warn("[SERVICE] SLOW ({} ms): {}", elapsed, pjp.getSignature().toShortString());
        } else {
            log.debug("[SERVICE] {} in {}ms", pjp.getSignature().toShortString(), elapsed);
        }
        return result;
    }

    @AfterThrowing(pointcut = "controllerMethods() || serviceMethods()", throwing = "ex")
    public void logException(Exception ex) {
        // Log message at WARN — stack trace at DEBUG to avoid noisy prod logs
        log.warn("[EXCEPTION] {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        log.debug("[EXCEPTION] Full stack trace:", ex);
    }
}
