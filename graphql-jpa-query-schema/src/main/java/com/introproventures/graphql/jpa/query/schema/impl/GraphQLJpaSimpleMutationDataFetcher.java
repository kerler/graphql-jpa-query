package com.introproventures.graphql.jpa.query.schema.impl;

import graphql.language.Argument;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.introproventures.graphql.jpa.query.schema.impl.util.MyFieldUtils;

public class GraphQLJpaSimpleMutationDataFetcher extends QraphQLJpaBaseDataFetcher {
    /**
     * Creates JPA entity DataFetcher instance
     *
     * @param entityManager
     * @param entityType
     */
    public GraphQLJpaSimpleMutationDataFetcher(EntityManager entityManager, EntityType<?> entityType) {
        super(entityManager, entityType);
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        Field field = environment.getFields().iterator().next();

        if (field.getArguments().isEmpty()) {
            throw new IllegalArgumentException("Arguments should not be empty.");
        }

        final Object singleResult = queryOrMakeSingleEntityObject(environment, field);
        setObjectAttributeValuesAccordingToArgumentValues(singleResult, environment, field.getArguments());
        entityManager.persist(singleResult);

        return singleResult;
    }

    private Object queryOrMakeSingleEntityObject(DataFetchingEnvironment environment, Field field) {
        if (hasIdentityArgument(field.getArguments())) {
            //TODO: Should make sure exact one result is returned. 'javax.persistence.NoResultException' may be thrown.
            return querySingleEntityObject(environment, field);
        }

        try {
            return ConstructorUtils.invokeConstructor(entityType.getJavaType());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean hasIdentityArgument(List<Argument> arguments) {
        return arguments.stream()
                .filter(this::isIdentityArgument)
                .findFirst()
                .isPresent();
    }

    private void setObjectAttributeValuesAccordingToArgumentValues(Object singleResult, DataFetchingEnvironment environment, List<Argument> arguments) {

        arguments.stream()
                .filter(this::isNotIdentityArgument)
                .forEach(iterArgument -> setAnAttributeValueOfObject(singleResult, environment, iterArgument));
    }

    private void setAnAttributeValueOfObject(Object singleResult, DataFetchingEnvironment environment, Argument argument) {
        final Attribute<?, ?> attribute = this.entityType.getAttribute(argument.getName());
        if (Objects.isNull(attribute)) {
            throw new IllegalArgumentException("Entity type '" + entityType.getName() + "' has no attribute named '" + argument.getName() + "'.");
        }

        final java.lang.reflect.Field field = FieldUtils.getField(singleResult.getClass(), argument.getName(), true);
        final Object valueWithJavaType = convertValue(environment, argument, argument.getValue());

        if (isPrimitiveOrWrapperOrOtherBasicType(attribute.getJavaType())) {
            setBasicTypeFieldValue(singleResult, field, valueWithJavaType);
        } else {
            throw new IllegalArgumentException("Can not update entity attribute value using value '" + valueWithJavaType
                    + "' with type '" + valueWithJavaType.getClass().getName() + "'.");
        }
    }

    private void setBasicTypeFieldValue(Object bean, java.lang.reflect.Field field, Object fieldValue) {
        //FIXME: Should use Joda lib which can handle Date value well.
        String[] pattern = new String[]{"yyyy-MM", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss"};

        try {
            if (String.class.isAssignableFrom(field.getType()) && !field.getName().equals("id")) {
                MyFieldUtils.writeFieldUsingSetterMethod(bean, field, fieldValue);
            } else if (Date.class.isAssignableFrom(field.getType())) {
                if (fieldValue.toString().contains("Z")) {
                    fieldValue = fieldValue.toString().substring(0, 10);
                }
                Date dateValue = DateUtils.parseDate(fieldValue.toString(), pattern);
                MyFieldUtils.writeFieldUsingSetterMethod(bean, field, dateValue);
            } else if (Integer.class.isAssignableFrom(field.getType())) {
                //FIXME: Here convert from String to double then convert to int. Why not directly convert from String to int?
                //FIXME: Many similar problems in source code below.
                MyFieldUtils.writeFieldUsingSetterMethod(bean, field, NumberUtils.createDouble(fieldValue.toString()).intValue());
            } else if (Boolean.class.isAssignableFrom(field.getType())) {
                MyFieldUtils.writeFieldUsingSetterMethod(bean, field, BooleanUtils.toBooleanObject(fieldValue.toString()));
            } else if (BigDecimal.class.isAssignableFrom(field.getType())) {
                MyFieldUtils.writeFieldUsingSetterMethod(bean, field, NumberUtils.createBigDecimal(fieldValue.toString()));
            } else if (Double.class.isAssignableFrom(field.getType())) {
                MyFieldUtils.writeFieldUsingSetterMethod(bean, field, NumberUtils.createDouble(fieldValue.toString()));
            } else if (Float.class.isAssignableFrom(field.getType())) {
                MyFieldUtils.writeFieldUsingSetterMethod(bean, field, NumberUtils.createFloat(fieldValue.toString()));
            } else if (Long.class.isAssignableFrom(field.getType())) {
                MyFieldUtils.writeFieldUsingSetterMethod(bean, field, NumberUtils.createDouble(fieldValue.toString()).longValue());
            } else if (Short.class.isAssignableFrom(field.getType())) {
                MyFieldUtils.writeFieldUsingSetterMethod(bean, field, NumberUtils.createDouble(fieldValue.toString()).shortValue());
            } else if (Byte.class.isAssignableFrom(field.getType())) {
                MyFieldUtils.writeFieldUsingSetterMethod(bean, field, NumberUtils.createDouble(fieldValue.toString()).byteValue());
            } else if (BigInteger.class.isAssignableFrom(field.getType())) {
                MyFieldUtils.writeFieldUsingSetterMethod(bean, field, NumberUtils.createBigInteger(fieldValue.toString()));
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private boolean isPrimitiveOrWrapperOrOtherBasicType (Class<?> clazz) {
        return ClassUtils.isPrimitiveOrWrapper(clazz)
                || String.class.isAssignableFrom(clazz)
                || Date.class.isAssignableFrom(clazz)
                || BigDecimal.class.isAssignableFrom(clazz)
                || BigInteger.class.isAssignableFrom(clazz);
    }

    private Object querySingleEntityObject(DataFetchingEnvironment environment, Field field) {
        // Create entity graph from selection
        EntityGraph<?> entityGraph = buildEntityGraph(field);

        return super.getQuery(environment, field, true, true)
                .setHint("javax.persistence.fetchgraph", entityGraph)
                .getSingleResult();
    }

}
