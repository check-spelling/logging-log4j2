/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.core;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.apache.logging.log4j.core.test.junit.LoggerContextSource;
import org.apache.logging.log4j.core.test.junit.Named;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@LoggerContextSource("log4j2-pattern-layout-with-context.xml")
public class PatternResolverDoesNotEvaluateThreadContextTest {

    private static final String PARAMETER = "user";

    private final LoggerContext context;
    private final ListAppender listAppender;

    public PatternResolverDoesNotEvaluateThreadContextTest(
            LoggerContext context,
            @Named("list") ListAppender app) {
        this.context = context;
        this.listAppender = app.clear();
    }

    @Test
    public void testNoUserSet() {
        Logger logger = context.getLogger(getClass());
        logger.info("This is a test");
        List<String> messages = listAppender.getMessages();
        assertTrue(messages != null && messages.size() > 0, "No messages returned");
        String message = messages.get(0);
        assertEquals("INFO org.apache.logging.log4j.core." +
                "PatternResolverDoesNotEvaluateThreadContextTest ${ctx:user} This is a test", message);
    }

    @Test
    public void testMessageIsNotLookedUp() {
        Logger logger = context.getLogger(getClass());
        logger.info("This is a ${upper:test}");
        List<String> messages = listAppender.getMessages();
        assertTrue(messages != null && messages.size() > 0, "No messages returned");
        String message = messages.get(0);
        assertEquals("INFO org.apache.logging.log4j.core." +
                "PatternResolverDoesNotEvaluateThreadContextTest ${ctx:user} This is a ${upper:test}", message);
    }

    @Test
    public void testUser() {
        Logger logger = context.getLogger(getClass());
        ThreadContext.put(PARAMETER, "123");
        try {
            logger.info("This is a test");
        } finally {
            ThreadContext.remove(PARAMETER);
        }
        List<String> messages = listAppender.getMessages();
        assertTrue(messages != null && messages.size() > 0, "No messages returned");
        String message = messages.get(0);
        assertEquals("INFO org.apache.logging.log4j.core." +
                "PatternResolverDoesNotEvaluateThreadContextTest 123 This is a test", message);
    }

    @Test
    public void testUserIsLookup() {
        Logger logger = context.getLogger(getClass());
        ThreadContext.put(PARAMETER, "${java:version}");
        try {
            logger.info("This is a test");
        } finally {
            ThreadContext.remove(PARAMETER);
        }
        List<String> messages = listAppender.getMessages();
        assertTrue(messages != null && messages.size() > 0, "No messages returned");
        String message = messages.get(0);
        assertEquals("INFO org.apache.logging.log4j.core." +
                "PatternResolverDoesNotEvaluateThreadContextTest ${java:version} This is a test", message);
    }

    @Test
    public void testUserHasLookup() {
        Logger logger = context.getLogger(getClass());
        ThreadContext.put(PARAMETER, "user${java:version}name");
        try {
            logger.info("This is a test");
        } finally {
            ThreadContext.remove(PARAMETER);
        }
        List<String> messages = listAppender.getMessages();
        assertTrue(messages != null && messages.size() > 0, "No messages returned");
        String message = messages.get(0);
        assertEquals("INFO org.apache.logging.log4j.core." +
                "PatternResolverDoesNotEvaluateThreadContextTest user${java:version}name This is a test", message);
    }
}
