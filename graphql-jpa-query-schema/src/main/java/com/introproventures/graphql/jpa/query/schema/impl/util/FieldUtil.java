package com.introproventures.graphql.jpa.query.schema.impl.util;

import org.apache.commons.lang3.reflect.MethodUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FieldUtil {
    public static Object writeField(Object object, Field field, Object fieldValue) {
        try {
            String methodName = "";
            try {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), object.getClass());
                Method setMethod = pd.getWriteMethod();
                methodName = setMethod.getName();
            } catch (IntrospectionException e) {
                methodName = MethodUtil.getSetterMethodName(field.getName());
            }

            return MethodUtils.invokeMethod(object, true, methodName, fieldValue);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
