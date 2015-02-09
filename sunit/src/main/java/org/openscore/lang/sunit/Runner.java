/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.sunit;

import org.openscore.api.ExecutionPlan;
import org.openscore.api.ExecutionStep;
import org.openscore.events.ScoreEvent;
import org.openscore.events.ScoreEventListener;
import org.openscore.lang.api.Slang;
import org.openscore.lang.api.SlangImpl;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.entities.ActionType;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.ScoreLangConstants;
import org.openscore.lang.runtime.events.LanguageEventData;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Runner {

    public static void main(String[] args) throws URISyntaxException {
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/sunitContext.xml");
        context.registerShutdownHook();
        Slang slang = context.getBean(SlangImpl.class);
        URI flowSource = getUri("/sunit/tests/simple-test.sl");
        URI opSource = getUri("/sunit/tests/ops/basic_op.sl");
        URI validatorSource = getUri("/sunit/validators/equal_validator.sl");

        HashSet<SlangSource> dependencies = new HashSet<>();
        dependencies.add(SlangSource.fromFile(opSource));
        dependencies.add(SlangSource.fromFile(validatorSource));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(flowSource), dependencies);
        if (compilationArtifact != null) {
            System.out.println("Complied successfully");
        }
        replaceWithMock(compilationArtifact);

        slang.subscribeOnAllEvents(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent scoreEvent) throws InterruptedException {
                LanguageEventData languageEventData = (LanguageEventData) scoreEvent.getData();
                String message = languageEventData.getPath() + " - " + scoreEvent.getEventType() + ": ";
                Serializable result = languageEventData.get("RESULT");
                if (result != null) message = message + "Result = " + result;
                System.out.println(message);
            }
        });

        slang.run(compilationArtifact, new HashMap(), new HashMap<String, Serializable>());
        try {
            Thread.sleep(10 * 1000);
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static void replaceWithMock(CompilationArtifact compilationArtifact) {
        Map<Long, ExecutionStep> steps = compilationArtifact.getExecutionPlan()
                                                            .getSteps();
        ExecutionStep startTaskStep = steps.get(2L);
        Map<String, ?> actionData = startTaskStep.getActionData();
        Map hooks = (Map) actionData.get(ScoreLangConstants.HOOKS);
        Map mockData = (Map) hooks.get("mock");
        String executableName = (String) actionData.get(ScoreLangConstants.REF_ID);
        Map<String, ExecutionPlan> dependencies = compilationArtifact.getDependencies();
        ExecutionPlan executableExecPlan = dependencies.get(executableName);
        ExecutionStep doActionStep = executableExecPlan.getSteps()
                                                       .get(2L);
        Map<String, Object> doActionActionData = (Map<String, Object>) doActionStep.getActionData();
        doActionActionData.put(ScoreLangConstants.ACTION_TYPE, ActionType.PYTHON);
        List<Map> action_outputs = (List<Map>) mockData.get("action_outputs");
        StringBuilder script = new StringBuilder();
        for (Map outputs : action_outputs) {
            script.append(outputs.keySet()
                                 .iterator()
                                 .next())
                  .append(" = ")
                  .append(outputs.values()
                                 .iterator()
                                 .next())
                  .append(System.lineSeparator());
        }
        doActionActionData.put(ScoreLangConstants.PYTHON_SCRIPT_KEY, script.toString());
    }

    private static URI getUri(String path) throws URISyntaxException {
        return Runner.class.getResource(path)
                           .toURI();
    }

}
