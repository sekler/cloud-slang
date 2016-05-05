/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.systemtests.flows;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.systemtests.StepData;
import io.cloudslang.lang.systemtests.ValueSyntaxParent;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Bonczidai Levente
 * @since 11/6/2015
 */
public class SensitiveValueSyntaxInFlowTest extends ValueSyntaxParent {

    @Test
    public void testValues() throws Exception {
        // compile
        URI resource = getClass().getResource("/yaml/formats/sensitive_values_flow.sl").toURI();
        URI op1 = getClass().getResource("/yaml/noop.sl").toURI();
        URI op2 = getClass().getResource("/yaml/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(op1), SlangSource.fromFile(op2));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        // trigger
        Map<String, StepData> steps = prepareAndRun(compilationArtifact);

        // verify
        StepData flowData = steps.get(EXEC_START_PATH);
        StepData stepData = steps.get(FIRST_STEP_PATH);

        verifyExecutableInputs(flowData);
        verifyExecutableOutputs(flowData);
        verifyStepInputs(stepData);
        verifyStepPublishValues(stepData);
        verifySuccessResult(flowData);
    }

    private void verifyStepInputs(StepData stepData) {
        Map<String, Value> expectedStepArguments = new HashMap<>();

        // properties
        expectedStepArguments.put("input_no_expression", ValueFactory.create("input_no_expression_value"));

        // loaded by Yaml
        expectedStepArguments.put("input_int", ValueFactory.create(22));
        expectedStepArguments.put("input_str_no_quotes", ValueFactory.create("Hi"));
        expectedStepArguments.put("input_str_single", ValueFactory.create("Hi"));
        expectedStepArguments.put("input_str_double", ValueFactory.create("Hi"));
        expectedStepArguments.put("input_yaml_list", ValueFactory.create(Lists.newArrayList(1, 2, 3)));
        HashMap<String, Value> expectedYamlMapFolded = new HashMap<>();
        expectedYamlMapFolded.put("key1", ValueFactory.create("medium"));
        expectedYamlMapFolded.put("key2", ValueFactory.create(false));
        expectedStepArguments.put("input_yaml_map_folded", ValueFactory.create(expectedYamlMapFolded));

        // evaluated via Python
        expectedStepArguments.put("input_python_null", ValueFactory.create(null));
        expectedStepArguments.put("input_python_list", ValueFactory.create(Lists.newArrayList(1, 2, 3)));
        HashMap<String, Value> expectedInputPythonMap = new HashMap<>();
        expectedInputPythonMap.put("key1", ValueFactory.create("value1"));
        expectedInputPythonMap.put("key2", ValueFactory.create("value2"));
        expectedInputPythonMap.put("key3", ValueFactory.create("value3"));
        expectedStepArguments.put("input_python_map", ValueFactory.create(expectedInputPythonMap));
        expectedStepArguments.put("b", ValueFactory.create("b"));
        expectedStepArguments.put("b_copy", ValueFactory.create("b"));
        expectedStepArguments.put("input_concat_1", ValueFactory.create("ab"));
        expectedStepArguments.put("input_concat_2_folded", ValueFactory.create("prefix_ab_suffix"));
        expectedStepArguments.put("step_argument_null", ValueFactory.create(null));

        Assert.assertTrue("Step arguments not bound correctly", includeAllPairs(stepData.getInputs(), expectedStepArguments));
    }

    private void verifyStepPublishValues(StepData stepData) {
        Map<String, Value> expectedStepPublishValues = new HashMap<>();

        expectedStepPublishValues.put("output_no_expression", ValueFactory.create("output_no_expression_value"));
        expectedStepPublishValues.put("publish_int", ValueFactory.create(22));
        expectedStepPublishValues.put("publish_str", ValueFactory.create("publish_str_value"));
        expectedStepPublishValues.put("publish_expression", ValueFactory.create("publish_str_value_suffix"));
        expectedStepPublishValues.put("output_step_argument_null", ValueFactory.create("step_argument_null_value"));

        Assert.assertEquals("Step publish values not bound correctly", expectedStepPublishValues, stepData.getOutputs());
    }
}