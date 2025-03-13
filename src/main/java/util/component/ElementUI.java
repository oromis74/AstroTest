package util.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ElementUI {

    String id() default "";
    String xpath() default "//*[@id='%id']";
    String xpathOption() default "//*[contains(text(),'%option')]";
    int number() default -1;
}
