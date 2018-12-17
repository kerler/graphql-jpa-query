package com.introproventures.graphql.jpa.query.schema.impl.visitor;

import com.github.behaim.explorer.*;
import com.github.behaim.explorer.abstract_visitor.AbstractVisitorToVisitEachObjectNoMoreThanOnce;
import com.github.behaim.util.ClassUtilsForExplorer;
import com.introproventures.graphql.jpa.query.schema.impl.util.MyFieldUtils;
import graphql.schema.GraphQLInputType;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.lang.reflect.Field;
import java.util.Objects;

public class VisitorToCopyFieldValueRecursively extends AbstractVisitorToVisitEachObjectNoMoreThanOnce {

    public static class Parameters {
        public final GraphQLInputType graphQLInputType;
        public final EntityType<?> entityTypeOfTargetObject;
        public final String targetFieldName;
        public final Object targetObject;

        public Parameters(GraphQLInputType graphQLInputType, EntityType<?> entityTypeOfTargetObject, String targetFieldName, Object targetObject) {
            this.graphQLInputType = graphQLInputType;
            this.entityTypeOfTargetObject = entityTypeOfTargetObject;
            this.targetFieldName = targetFieldName;
            this.targetObject = targetObject;
        }
    }

    public static class WrapperToTargetObject {
        public final Parameters parameters;

        public WrapperToTargetObject(Parameters parameters) {
            this.parameters = parameters;
        }
    }

    @Override
    protected Object doCreateContextObject(Object param) {
        if(! (param instanceof Parameters)) {
            throw new IllegalArgumentException("param is not String type.");
        }

        final Parameters parameters = (Parameters) param;

        return new WrapperToTargetObject(parameters);
    }

    @Override
    protected VisitationResult doVisitFieldOfObject(Object context, Object object, Field field, Trace trace) {
        Object fieldValue = getFieldValue(object, field);

        return new VisitationResult(new DefaultFieldContext(field), fieldValue, true);
    }

    @Override
    public boolean doVisitFieldSingleValue(Object context, Object containingObject, Field field, Object fieldValue, FieldTraceKind fieldTraceKind, Trace trace) {
        final WrapperToTargetObject wrapperToTargetObject = convertContextType(context);

        final Attribute<?, ?> attribute = wrapperToTargetObject.parameters.entityTypeOfTargetObject.getAttribute(wrapperToTargetObject.parameters.targetFieldName);
        if (Objects.isNull(attribute)) {
            throw new IllegalArgumentException("Entity type '" + wrapperToTargetObject.parameters.entityTypeOfTargetObject.getName() + "' has no attribute named '" + wrapperToTargetObject.parameters.targetFieldName + "'.");
        }

        if (ClassUtilsForExplorer.isBasicClass(attribute.getJavaType())) {
            setBasicTypeFieldValue(wrapperToTargetObject.parameters.targetObject, wrapperToTargetObject.parameters.targetFieldName, fieldValue);
        } else {
            throw new IllegalArgumentException("Can not update entity attribute value using value '" + fieldValue
                    + "' with type '" + fieldValue.getClass().getName() + "'.");
        }

        return false;
    }

    private void setBasicTypeFieldValue(Object bean, String fieldName, Object fieldValue) {
        //TODO: Need to add test cases to test each possible type of java.lang.reflect.Field. Also refer to QraphQLJpaBaseDataFetcher::convertValue().
        MyFieldUtils.writeFieldUsingSetterMethodThenUsingFieldWriting(bean, fieldName, fieldValue);
    }

    public WrapperToTargetObject convertContextType(Object context) {
        return (WrapperToTargetObject) context;
    }
}
