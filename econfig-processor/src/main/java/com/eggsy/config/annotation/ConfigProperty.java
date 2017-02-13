package com.eggsy.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by eggsy on 16-12-1.
 */

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
public @interface ConfigProperty {

    String name() default "";

    String defaultValue() default "";

    String format() default "";

}
