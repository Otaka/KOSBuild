package org.visualeagle.utils.annotatedtable.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Dmitry
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotatedTableField {
    String column();
    int id();
}
