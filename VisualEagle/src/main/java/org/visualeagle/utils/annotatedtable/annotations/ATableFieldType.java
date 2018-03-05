package org.visualeagle.utils.annotatedtable.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.visualeagle.utils.annotatedtable.TypeOfColumn;

@Retention(RetentionPolicy.RUNTIME)
public @interface ATableFieldType {
    TypeOfColumn getType() default TypeOfColumn.NULL;
}
