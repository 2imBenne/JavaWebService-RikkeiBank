package com.rikkeibankproject.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class AuditLoggingAspect {

    @AfterReturning("execution(* com.rikkeibankproject.service.CoreBankingService.transferMoney(..))")
    public void logSuccessfulTransfer(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String username = (String) args[0];
        log.info("[AUDIT-LOG] User {} successfully initiated a transfer. Arguments: {}", username, Arrays.toString(args));
    }

    @AfterThrowing(pointcut = "execution(* com.rikkeibankproject.service.CoreBankingService.transferMoney(..))", throwing = "ex")
    public void logFailedTransfer(JoinPoint joinPoint, Exception ex) {
        Object[] args = joinPoint.getArgs();
        String username = (String) args[0];
        log.error("[AUDIT-LOG] Transfer failed for user {}. Reason: {}", username, ex.getMessage());
    }
}
