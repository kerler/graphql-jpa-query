package com.introproventures.graphql.jpa.query.schema.impl.util;

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
    public static Object writeFieldUsingSetterMethod(Object object, Field field, Object fieldValue) {
        try {
            String methodName = getSetterMethodName(object, field);

            return MethodUtils.invokeMethod(object, true, methodName, fieldValue);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String getSetterMethodName(Object object, Field field) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), object.getClass());
            Method setMethod = pd.getWriteMethod(); // This way can get public setter method only.
            return setMethod.getName();
        } catch (IntrospectionException e) {
            return MyMethodUtils.getSetterMethodName(field.getName());
        }
    }
}
