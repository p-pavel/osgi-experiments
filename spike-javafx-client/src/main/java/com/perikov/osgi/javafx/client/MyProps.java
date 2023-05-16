package com.perikov.osgi.javafx.client;
import org.osgi.service.component.annotations.ComponentPropertyType;

@ComponentPropertyType
public @interface MyProps {
  boolean enabled() default false;
  
}
