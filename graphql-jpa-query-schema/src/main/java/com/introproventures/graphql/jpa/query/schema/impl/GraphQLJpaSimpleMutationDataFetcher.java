package com.introproventures.graphql.jpa.query.schema.impl;

import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.reflect.FieldUtils;

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

        if( field.getArguments().isEmpty() ) {
            return null;
        }

        try {
            final Object singleResult = getObjectFromDB(environment, field);
            testUpdateAFieldValueInObject(singleResult);

            entityManager.persist(singleResult);
            return singleResult;

        } catch (NoResultException ignored) {
            // do nothing
        }

        return null;
    }

    private void testUpdateAFieldValueInObject(Object singleResult) {

        FieldUtils_writeField(singleResult, "homePlanet", "LBG", true);
    }

    private Object getObjectFromDB(DataFetchingEnvironment environment, Field field) {
        // Create entity graph from selection
        EntityGraph<?> entityGraph = buildEntityGraph(field);

        return super.getQuery(environment, field, true)
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
