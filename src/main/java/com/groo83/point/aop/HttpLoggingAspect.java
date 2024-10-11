package com.groo83.point.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class HttpLoggingAspect {

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object httpLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String calledMethod = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        String requestParams = Arrays.toString(joinPoint.getArgs());
        String requestHeaders = getHeaders(request).toString();

        log.info("==================================================================");
        log.info("URL : {}", request.getRequestURL());
        log.info("HTTP METHOD : {}", request.getMethod());
        log.info("CLASS METHOD : {}", calledMethod);
        log.info("REQUEST IP : {}", request.getRemoteAddr());
        log.info("REQUEST HEADERS : {}", requestHeaders);
        log.info("REQUEST PARAMS : {}", requestParams);
        log.info("==================================================================");
        Object proceed = joinPoint.proceed();
        log.info("==================================================================");

        String response = proceed.toString();
        log.info("RESPONSE : {}", response);
        log.info("==================================================================");

        log.info("[ONE LINE] URL : {}, HTTP METHOD : {}, " +
                "CLASS METHOD : {}, " +
                "REQUEST IP : {}, " +
                "REQUEST HEADERS : {}, " +
                "REQUEST PARAMS : {}, " +
                "RESPONSE = {} "
                , request.getRequestURL(), request.getMethod(), calledMethod, request.getRemoteAddr(), requestHeaders, requestParams, response);


        return proceed;
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headerMap = new LinkedHashMap<>();
        Enumeration<String> headers = request.getHeaderNames();

        while(headers.hasMoreElements()) {
            String key = headers.nextElement();
            String value = request.getHeader(key);

            headerMap.put(key, value);
        }

        return headerMap;
    }
}

