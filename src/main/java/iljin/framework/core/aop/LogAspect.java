// Log 관련 내용은 이 곳에서 관리

package iljin.framework.core.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Before("execution(* iljin.framework.*.*Controller.*(..))")
    public void onBeforeHandler(JoinPoint joinPoint) {
        logger.info("================== onBeforeThing");
    }

    @After("execution(* iljin.framework.*.*Controller.*(..))")
    public void onAfterHandler(JoinPoint joinPoint) {
        logger.info("================== onAfterHandler");
    }

    @AfterReturning(pointcut = "execution(* iljin.framework.*.*Controller.*(..))", returning = "result")
    public void onAfterReturningHandler(JoinPoint joinpoint, Object result) {

        Optional<Object> resultOptional = Optional.ofNullable(result);

        logger.info("================== onAfterReturning");

        if(resultOptional.isPresent()) {
            logger.info("return: " + result.toString());
        } else {
            logger.info("return null");
        }

    }

    @AfterThrowing(pointcut = "execution(* iljin.framework.*.*Controller.*(..))", throwing = "ex")
    public void onAfterThrowingHandler(JoinPoint joinPoint, Throwable ex) {
        logger.info("================== onAfterThrowing");
        logger.info("throwing: " + ex.toString());
    }

}
