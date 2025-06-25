package com.Teryaq.utils.Aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Component
@Aspect
@EnableAspectJAutoProxy
public class AspectClass {

    private static final Logger logger = LoggerFactory.getLogger(AspectClass.class);

    @Before("" +
            "execution(* com.Teryaq.book.service.*.*(..))"+
            "|| execution(* com.Teryaq.patron.service.*.*(..))" +
            "|| execution(* com.Teryaq.borrowingRecord.service.*.*(..))"
//            "|| execution(* com.LibraryManagementSystem.user.service.*.*(..))"
    )
    public void logBeforeMethod(JoinPoint joinPoint) {
        logger.info("Method called : " + joinPoint.getSignature().toShortString());
    }

    @AfterThrowing("" +
            "execution(* com.Teryaq.book.service.*.*(..))"+
            "|| execution(* com.Teryaq.patron.service.*.*(..))" +
            "|| execution(* com.Teryaq.borrowingRecord.service.*.*(..))"
//            "|| execution(* com.LibraryManagementSystem.user.service.*.*(..))"
    )
    public void aspect(JoinPoint joinPoint) {
        logger.error("Exception thrown in method: {}", joinPoint.getSignature().getName());
    }

    @Around("" +
            "execution(* com.Teryaq.book.service.*.*(..))"+
            "|| execution(* com.Teryaq.patron.service.*.*(..))" +
            "|| execution(* com.Teryaq.borrowingRecord.service.*.*(..))"
//            "|| execution(* com.LibraryManagementSystem.user.service.*.*(..))"
    )
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            logger.info("Execution time of " + joinPoint.getSignature().toShortString() + " : " + duration + "ms");
        }
    }





}

