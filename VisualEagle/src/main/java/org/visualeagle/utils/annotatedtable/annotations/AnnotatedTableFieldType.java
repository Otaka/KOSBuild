package org.visualeagle.utils.annotatedtable.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.visualeagle.utils.annotatedtable.COLUMNTYPE;

/**
 * @author Dmitry
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotatedTableFieldType {
    COLUMNTYPE getType() default COLUMNTYPE.NULL;
}
