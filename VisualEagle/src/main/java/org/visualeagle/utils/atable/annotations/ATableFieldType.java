package org.visualeagle.utils.atable.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.visualeagle.utils.atable.TypeOfColumn;

@Retention(RetentionPolicy.RUNTIME)
public @interface ATableFieldType {
    TypeOfColumn getType() default TypeOfColumn.NULL;
}
