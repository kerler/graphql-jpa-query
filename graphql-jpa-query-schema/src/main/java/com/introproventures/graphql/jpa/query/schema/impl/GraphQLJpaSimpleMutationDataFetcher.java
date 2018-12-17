package com.introproventures.graphql.jpa.query.schema.impl;

import com.github.behaim.explorer.Explorer;
import com.github.behaim.util.ClassUtilsForExplorer;
import com.introproventures.graphql.jpa.query.schema.impl.visitor.VisitorToCopyFieldValueRecursively;
import graphql.language.Argument;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import graphql.schema.GraphQLInputType;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.introproventures.graphql.jpa.query.schema.impl.util.MyFieldUtils;

public class GraphQLJpaSimpleMutationDataFetcher extends QraphQLJpaBaseDataFetcher {

    private Explorer explorerToCopyFieldValueRecursively = new Explorer(new VisitorToCopyFieldValueRecursively());

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

//        if (field.getArguments().isEmpty()) {
//            throw new IllegalArgumentException("Arguments should not be empty.");
//        }

        final Object singleResult = queryOrMakeSingleEntityObject(environment, field);
        setObjectAttributeValuesAccordingToArgumentValues(singleResult, environment, field.getArguments());
        entityManager.persist(singleResult);

        return singleResult;
    }

    private Object queryOrMakeSingleEntityObject(DataFetchingEnvironment environment, Field field) {
        if (hasIdentityArgument(field.getArguments())) {
            final Object queriedSingleEntityObject = querySingleEntityObject(environment, field);
            if (Objects.nonNull(queriedSingleEntityObject)) {
                return queriedSingleEntityObject;
            }
        }

        return makeSingleEntityObject();
    }

    private Object makeSingleEntityObject() {
        try {
            return ConstructorUtils.invokeConstructor(entityType.getJavaType()); //TODO: Change to use RevisedConstructorUtils.invokeConstructor() so that non-public constructor can be invoked.
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
//                .filter(this::isNotIdentityArgument)
                .forEach(iterArgument -> setAnAttributeValueOfObject(singleResult, environment, iterArgument));
    }

    private void setAnAttributeValueOfObject(Object singleResult, DataFetchingEnvironment environment, Argument argument) {

        final Object valueWithJavaType = convertValue(environment, argument, argument.getValue());
        final GraphQLInputType graphQLInputType = environment.getFieldDefinition().getArgument(argument.getName()).getType();

        explorerToCopyFieldValueRecursively.explore(
                valueWithJavaType
                , new VisitorToCopyFieldValueRecursively.Parameters(
                        graphQLInputType
                        , this.entityType
                        , argument.getName()
                        , singleResult));
    }

    private Object querySingleEntityObject(DataFetchingEnvironment environment, Field field) {
        // Create entity graph from selection
        EntityGraph<?> entityGraph = buildEntityGraph(field);

        final List<?> resultList = super.getQuery(environment, field, true, true)
                .setHint("javax.persistence.fetchgraph", entityGraph)
                .getResultList();

        if (resultList.size() > 1) {
            throw new IllegalArgumentException("Should not get more than one result. Actual number of result is '" + resultList.size() + "'.");
        }

        return (resultList.isEmpty()) ? null : resultList.get(0);
    }
}
