package com.qh.pay.aop;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/****************************************************************************************
 实现AOP的切面主要有以下几个要素：

 使用@Aspect注解将一个java类定义为切面类
 使用@Pointcut定义一个切入点，可以是一个规则表达式，比如下例中某个package下的所有函数，也可以是一个注解等。
 根据需要在切入点不同位置的切入内容
 使用@Before在切入点开始处切入内容
 使用@After在切入点结尾处切入内容
 使用@AfterReturning在切入点return内容之后切入内容（可以用来对处理返回值做一些加工处理）
 使用@Around在切入点前后切入内容，并自己控制何时执行切入点自身的内容
 使用@AfterThrowing用来处理当切入内容部分抛出异常之后的处理逻辑
 使用@Order(i)注解来标识切面的优先级。i的值越小，优先级越高
 ****************************************************************************************/

/**
 * Web层日志切面
 *
 */
@Aspect
@Order(5)
@Component
public class WebLogFiveAspect {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(WebLogFiveAspect.class);

    ThreadLocal<Long> startTime = new ThreadLocal<>();

    private static final String PRE_TAG = "调用聚富支付============== ";

    @Pointcut("execution(public * com.qh.pay.controller..*.*(..))")
    public void webLog(){}

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        startTime.set(System.currentTimeMillis());

        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 记录下请求内容
        logger.info(PRE_TAG + "(doBefore) URL : " + request.getRequestURL().toString());
        logger.info(PRE_TAG + "(doBefore) HTTP_METHOD : " + request.getMethod());
        logger.info(PRE_TAG + "(doBefore) IP : " + request.getRemoteAddr());
        logger.info(PRE_TAG + "(doBefore) CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        logger.info(PRE_TAG + "(doBefore) ARGS : " + Arrays.toString(joinPoint.getArgs()));

    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容
        logger.info(PRE_TAG + "(doAfterReturning) RESPONSE : " + ret);
        logger.info(PRE_TAG + "(doAfterReturning) SPEND TIME : " + (System.currentTimeMillis() - startTime.get()));
    }
}