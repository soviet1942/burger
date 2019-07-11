package annotation.spider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/11 14:22
 * @Description:
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Spider {
    String name() default "default";
    String[] allowedDomains() default {};
    String[] startUrls() default {};
}
