package org.sinekartads.dto.formats;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DateTimeDTOProperty { 
	String format() default "dd/MM/yyyy HH:mm";
}
