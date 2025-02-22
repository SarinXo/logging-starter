package ilya.project.loggingstarter.adpect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Aspect
public class LogExecutionTimeAspect {

    private final static Logger log = LoggerFactory.getLogger(LogExecutionTimeAspect.class);

    @Around("@annotation(ilya.project.loggingstarter.adpect.LogExecutionTime)")
    public Object logExecutionTimeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogExecutionTime annotation = method.getAnnotation(LogExecutionTime.class);

        long start = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            throw e.getCause();
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            log.info("Method {} was accomplished in {} ms", getMethodName(method, annotation), executionTime);
        }
    }

    private String getMethodName(Method annotatedMethod, LogExecutionTime annotation) {
        String annotationMethodName = annotation.methodName();
        return StringUtils.hasText(annotationMethodName)
                ? annotationMethodName
                : annotatedMethod.getName();
    }


}
