package ioc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Bean(scope = Bean.Type.PROTOTYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
}
