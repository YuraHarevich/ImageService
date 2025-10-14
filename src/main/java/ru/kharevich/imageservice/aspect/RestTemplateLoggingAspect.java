package ru.kharevich.imageservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
@Component
@Aspect
@Slf4j
public class RestTemplateLoggingAspect {

    @Pointcut("execution(* org.springframework.web.client.RestTemplate.*(..))")
    public void restTemplateMethods() {}

    @Around("restTemplateMethods()")
    public Object logRestTemplateCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Логируем только вызовы, которые делают HTTP запросы
        if (isHttpMethod(methodName)) {
            return logHttpCall(joinPoint, methodName, args);
        }

        return joinPoint.proceed();
    }

    private Object logHttpCall(ProceedingJoinPoint joinPoint, String methodName, Object[] args) throws Throwable {
        String url = extractUrl(args);
        Object requestBody = extractRequestBody(args);

        // Логирование запроса
        logRestTemplateRequest(methodName, url, requestBody);

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // Логирование ответа
            logRestTemplateSuccess(methodName, url, result, executionTime);

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            // Логирование ошибки
            logRestTemplateError(methodName, url, e, executionTime);
            throw e;
        }
    }

    private void logRestTemplateRequest(String method, String url, Object body) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n🎯 REST TEMPLATE OUTGOING REQUEST\n");
        logMessage.append("╔═══════════════════════════════════════════════════════════\n");
        logMessage.append("║ Method: ").append(method).append("\n");
        logMessage.append("║ URL: ").append(url).append("\n");
        logMessage.append("║ Timestamp: ").append(LocalDateTime.now()).append("\n");

        if (body != null) {
            String bodyStr = body.toString();
            logMessage.append("║ Request Body: ").append(bodyStr.length() > 500 ? bodyStr.substring(0, 500) + "..." : bodyStr).append("\n");
        }
        logMessage.append("╚═══════════════════════════════════════════════════════════");

        log.info(logMessage.toString());
    }

    private void logRestTemplateSuccess(String method, String url, Object result, long executionTime) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n✅ REST TEMPLATE SUCCESS RESPONSE\n");
        logMessage.append("╔═══════════════════════════════════════════════════════════\n");
        logMessage.append("║ Method: ").append(method).append("\n");
        logMessage.append("║ URL: ").append(url).append("\n");
        logMessage.append("║ Execution Time: ").append(executionTime).append("ms\n");
        logMessage.append("║ Timestamp: ").append(LocalDateTime.now()).append("\n");
        logMessage.append("║ Response Type: ").append(result != null ? result.getClass().getSimpleName() : "void").append("\n");
        logMessage.append("╚═══════════════════════════════════════════════════════════");

        log.info(logMessage.toString());
    }

    private void logRestTemplateError(String method, String url, Exception e, long executionTime) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n❌ REST TEMPLATE ERROR RESPONSE\n");
        logMessage.append("╔═══════════════════════════════════════════════════════════\n");
        logMessage.append("║ Method: ").append(method).append("\n");
        logMessage.append("║ URL: ").append(url).append("\n");
        logMessage.append("║ Execution Time: ").append(executionTime).append("ms\n");
        logMessage.append("║ Timestamp: ").append(LocalDateTime.now()).append("\n");
        logMessage.append("║ Error Type: ").append(e.getClass().getSimpleName()).append("\n");
        logMessage.append("║ Error Message: ").append(e.getMessage()).append("\n");
        logMessage.append("╚═══════════════════════════════════════════════════════════");

        log.error(logMessage.toString());
    }

    private boolean isHttpMethod(String methodName) {
        return methodName.startsWith("getFor") ||
                methodName.startsWith("postFor") ||
                methodName.startsWith("put") ||
                methodName.startsWith("delete") ||
                methodName.startsWith("exchange") ||
                methodName.startsWith("execute");
    }

    private String extractUrl(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof String) {
                return (String) arg;
            } else if (arg instanceof java.net.URI) {
                return arg.toString();
            }
        }
        return "Unknown";
    }

    private Object extractRequestBody(Object[] args) {
        for (Object arg : args) {
            if (arg != null && !(arg instanceof String) && !(arg instanceof java.net.URI)) {
                return arg;
            }
        }
        return null;
    }
}
