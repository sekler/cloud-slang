/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.ParallelLoopStatement;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.Context;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
@Component
public class ParallelLoopBinding {

    public static final String PARALLEL_LOOP_EXPRESSION_ERROR_MESSAGE = "Error evaluating async loop expression in step";

    @Autowired
    private ScriptEvaluator scriptEvaluator;

    public static String generateParallelLoopExpressionMessage(String nodeName, String message) {
        return PARALLEL_LOOP_EXPRESSION_ERROR_MESSAGE + " '" + nodeName + "', error is: \n" + message;
    }

    public List<Serializable> bindParallelLoopList(
            ParallelLoopStatement parallelLoopStatement,
            Context flowContext,
            Set<SystemProperty> systemProperties,
            String nodeName) {
        Validate.notNull(parallelLoopStatement, "async loop statement cannot be null");
        Validate.notNull(flowContext, "flow context cannot be null");
        Validate.notNull(systemProperties, "system properties cannot be null");
        Validate.notNull(nodeName, "node name cannot be null");

        List<Serializable> evalResult;
        try {
            evalResult = (List<Serializable>) scriptEvaluator.evalExpr(
                    parallelLoopStatement.getExpression(),
                    flowContext.getImmutableViewOfVariables(),
                    systemProperties);
        } catch (Throwable t) {
            throw new RuntimeException(generateParallelLoopExpressionMessage(nodeName, t.getMessage()), t);
        }
        if (CollectionUtils.isEmpty(evalResult)) {
            throw new RuntimeException(generateParallelLoopExpressionMessage(nodeName, "expression is empty"));
        }
        return evalResult;
    }
}
