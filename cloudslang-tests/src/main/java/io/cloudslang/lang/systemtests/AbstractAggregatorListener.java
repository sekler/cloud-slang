/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests;

import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Date: 4/8/2015
 *
 * @author Bonczidai Levente
 */
public abstract class AbstractAggregatorListener implements ScoreEventListener {

    private static final Logger logger = Logger.getLogger(AbstractAggregatorListener.class);

    private final List<LanguageEventData> events = new ArrayList<>();

    public List<LanguageEventData> getEvents() {
        return events;
    }

    @Override
    public synchronized void onEvent(ScoreEvent event) throws InterruptedException {
        LanguageEventData languageEvent = (LanguageEventData) event.getData();
        int b = events.size();
        logger.info("*** " + b);
        events.add(languageEvent);
        int a = events.size();
        logger.info("*** " + a + (a == (b+1) ? "" : "GOT YOU"));
        logger.info("*** [" + event.getEventType() + "] " + languageEvent);
    }

}
