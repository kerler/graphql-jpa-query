package com.introproventures.graphql.jpa.query.schema.impl.visitor;

import com.github.behaim.explorer.Trace;
import com.github.behaim.explorer.VisitationResult;
import com.github.behaim.explorer.abstract_visitor.AbstractVisitorToVisitEachObjectNoMoreThanOnce;

import java.lang.reflect.Field;

public class VisitorToCopyFieldValueRecursively extends AbstractVisitorToVisitEachObjectNoMoreThanOnce {
    @Override
    protected VisitationResult doVisitFieldOfObject(Object o, Object o1, Field field, Trace trace) {
        return null; //FIXME
    }
}
