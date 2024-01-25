package io.xnchung.breadboard.sample.bizhome;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.xnchung.breadboard.annotations.ParamQualifier;

@ParamQualifier
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface ColorParam {
}
