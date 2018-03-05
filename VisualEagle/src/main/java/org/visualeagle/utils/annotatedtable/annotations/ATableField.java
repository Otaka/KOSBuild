package org.visualeagle.utils.annotatedtable.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ATableField {
    String column();
    int id();
}
