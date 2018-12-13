package com.introproventures.graphql.jpa.query.schema.impl;

import graphql.language.Argument;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.introproventures.graphql.jpa.query.schema.impl.util.FieldUtil;

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
            return null;
        }

        if (! hasIdentityArgument(field.getArguments())) {
            return null;
        }

        try {
            final Object singleResult = getObjectFromDB(environment, field);
            testUpdateAFieldValueInObject(singleResult, environment, field);

            entityManager.persist(singleResult);
            return singleResult;

        } catch (NoResultException ignored) {
            // do nothing
        }

        return null;
    }

    private boolean hasIdentityArgument(List<Argument> arguments) {
        return arguments.stream()
                .filter(this::isIdentityArgument)
                .findFirst()
                .isPresent();
    }

    private void testUpdateAFieldValueInObject(Object singleResult, DataFetchingEnvironment environment, Field field) {

        field.getArguments().stream()
                .filter(this::isNotIdentityArgument)
                .forEach(iterArgument -> updateASingleAttributeOfObject(singleResult, environment, iterArgument));
    }

    private void updateASingleAttributeOfObject(Object singleResult, DataFetchingEnvironment environment, Argument argument) {
        final Attribute<?, ?> attribute = this.entityType.getAttribute(argument.getName());
        if (Objects.isNull(attribute)) {
            throw new IllegalArgumentException("Entity type '" + entityType.getName() + "' has no attribute named '" + argument.getName() + "'.");
        }

        final java.lang.reflect.Field field = FieldUtils.getField(singleResult.getClass(), argument.getName(), true);

        if (isPrimitiveOrWrapperOrOtherBasicType(attribute.getJavaType())) {
            handleBasicObject(singleResult, field, convertValue(environment, argument, argument.getValue()));
        }

//        FieldUtils_writeField(singleResult, "homePlanet", "LBG", true);
    }

    private void handleBasicObject(Object bean, java.lang.reflect.Field field, Object fieldValue) {
        String[] pattern = new String[]{"yyyy-MM", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss"};

        try {
            if (String.class.isAssignableFrom(field.getType()) && !field.getName().equals("id")) {
                FieldUtil.writeField(bean, field, fieldValue);
            } else if (Date.class.isAssignableFrom(field.getType())) {
                if (fieldValue.toString().contains("Z")) {
                    fieldValue = fieldValue.toString().substring(0, 10);
                }
                Date dateValue = DateUtils.parseDate(fieldValue.toString(), pattern);
                FieldUtil.writeField(bean, field, dateValue);
            } else if (Integer.class.isAssignableFrom(field.getType())) {
                FieldUtil.writeField(bean, field, NumberUtils.createDouble(fieldValue.toString()).intValue());
            } else if (Boolean.class.isAssignableFrom(field.getType())) {
                FieldUtil.writeField(bean, field, BooleanUtils.toBooleanObject(fieldValue.toString()));
            } else if (BigDecimal.class.isAssignableFrom(field.getType())) {
                FieldUtil.writeField(bean, field, NumberUtils.createBigDecimal(fieldValue.toString()));
            } else if (Double.class.isAssignableFrom(field.getType())) {
                FieldUtil.writeField(bean, field, NumberUtils.createDouble(fieldValue.toString()));
            } else if (Float.class.isAssignableFrom(field.getType())) {
                FieldUtil.writeField(bean, field, NumberUtils.createFloat(fieldValue.toString()));
            } else if (Long.class.isAssignableFrom(field.getType())) {
                FieldUtil.writeField(bean, field, NumberUtils.createDouble(fieldValue.toString()).longValue());
            } else if (Short.class.isAssignableFrom(field.getType())) {
                FieldUtil.writeField(bean, field, NumberUtils.createDouble(fieldValue.toString()).shortValue());
            } else if (Byte.class.isAssignableFrom(field.getType())) {
                FieldUtil.writeField(bean, field, NumberUtils.createDouble(fieldValue.toString()).byteValue());
            } else if (BigInteger.class.isAssignableFrom(field.getType())) {
                FieldUtil.writeField(bean, field, NumberUtils.createBigInteger(fieldValue.toString()));
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

    private Object getObjectFromDB(DataFetchingEnvironment environment, Field field) {
        // Create entity graph from selection
        EntityGraph<?> entityGraph = buildEntityGraph(field);

        return super.getQuery(environment, field, true, true)
                .setHint("javax.persistence.fetchgraph", entityGraph)
                .getSingleResult();
    }

    public static Object FieldUtils_readField(final Object target, final String fieldName, final boolean forceAccess) {
        try {
            return FieldUtils.readField(target, fieldName, forceAccess);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void FieldUtils_writeField(final Object target, final String fieldName, Object value, final boolean forceAccess) {
        try {
            FieldUtils.writeField(target, fieldName, value, forceAccess);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
