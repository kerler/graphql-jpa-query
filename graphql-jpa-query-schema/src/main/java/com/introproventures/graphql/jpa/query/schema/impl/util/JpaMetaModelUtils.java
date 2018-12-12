package com.introproventures.graphql.jpa.query.schema.impl.util;

import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

public class JpaMetaModelUtils {
    public static boolean isNotIgnored(EmbeddableType<?> attribute) {
        return isNotIgnored(attribute.getJavaType());
    }

    public static boolean isNotIgnored(Attribute<?,?> attribute) {
        return isNotIgnored(attribute.getJavaMember()) && isNotIgnored(attribute.getJavaType());
    }

    public static boolean isIdentity(Attribute<?,?> attribute) {
        return attribute instanceof SingularAttribute && ((SingularAttribute<?,?>)attribute).isId();
    }

    public static boolean isNotIgnored(EntityType<?> entityType) {
        return isNotIgnored(entityType.getJavaType());
    }

    public static boolean isNotIgnored(Member member) {
        return member instanceof AnnotatedElement && isNotIgnored((AnnotatedElement) member);
    }

    public static boolean isNotIgnored(AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            GraphQLIgnore schemaDocumentation = annotatedElement.getAnnotation(GraphQLIgnore.class);
            return schemaDocumentation == null;
        }

        return false;
    }

    public static boolean isEmbeddable(Attribute<?,?> attribute) {
    	return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED;
    }

    public static boolean isBasic(Attribute<?,?> attribute) {
    	return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC;
    }

    public static boolean isElementCollection(Attribute<?,?> attribute) {
    	return  attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION;
    }

    public static boolean isToMany(Attribute<?,?> attribute) {
    	return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY
        		|| attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_MANY;
    }

    public static boolean isToOne(Attribute<?,?> attribute) {
    	return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
        		|| attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE;
    }

    public static boolean isValidInput(Attribute<?,?> attribute) {
        return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC ||
                attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION;
    }
}
