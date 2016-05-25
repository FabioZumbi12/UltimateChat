package br.net.fabiozumbi12.UltimateChat.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigHeader
{
  String[] value();
  
  String path() default "";
}

