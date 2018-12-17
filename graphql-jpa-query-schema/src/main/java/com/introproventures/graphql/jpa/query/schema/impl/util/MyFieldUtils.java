package com.introproventures.graphql.jpa.query.schema.impl.util;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MyFieldUtils {

    /**
     * Use setter method to set field value.
     *
     * When using hibernate lib modified by Open-Care team, the entity's field value must be set using setter method instead of directly setting field value.
     *
     * @param object
     * @param field
     * @param fieldValue
     * @return
     */
    public static void writeFieldUsingSetterMethodThenUsingFieldWriting(Object object, Field field, Object fieldValue) {
        writeFieldUsingSetterMethodThenUsingFieldWriting(object, field.getName(), fieldValue);
    }

    public static void writeFieldUsingSetterMethodThenUsingFieldWriting(Object object, String fieldName, Object fieldValue) {
        String methodName = getSetterMethodName(object, fieldName);

        try {
             MethodUtils.invokeMethod(object, true, methodName, fieldValue);
        } /*catch (NoSuchMethodException noSuchMethodException) {

        }*/ catch (Exception e) {
            try {
                FieldUtils.writeField(object, fieldName, fieldValue, true);
            } catch (IllegalAccessException illegalAccessException ) {
                throw new IllegalArgumentException("Method '" + methodName + "' can not be invoked, and field '" + fieldName + "' can not be directly set value.");
            }
        }
    }

    public static String getSetterMethodName(Object object, Field field) {
        return getSetterMethodName(object, field.getName());
    }

    public static String getSetterMethodName(Object object, String fieldName) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, object.getClass());
            Method setMethod = pd.getWriteMethod(); // This way can get public setter method only.
            return setMethod.getName();
        } catch (IntrospectionException e) {
            return MyMethodUtils.getSetterMethodName(fieldName);
        }
    }


//    public static Object FieldUtils_readField(final Object target, final String fieldName, final boolean forceAccess) {
//        try {
//            return FieldUtils.readField(target, fieldName, forceAccess);
//        } catch (IllegalAccessException e) {
//            throw new IllegalStateException(e);
//        }
//    }
//
//    public static void FieldUtils_writeField(final Object target, final String fieldName, Object value, final boolean forceAccess) {
//        try {
//            FieldUtils.writeField(target, fieldName, value, forceAccess);
//        } catch (IllegalAccessException e) {
//            throw new IllegalStateException(e);
//        }
//    }

}
