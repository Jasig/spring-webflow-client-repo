/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.spring.webflow.plugin;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.test.MockExternalContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link ClientFlowExecutionRepository}.
 *
 * @author Marvin S. Addison
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/webflow-config-context.xml")
public class ClientFlowExecutionRepositoryTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void testLaunchAndResumeFlow() throws Exception {
        final FlowExecutor executor = context.getBean(FlowExecutor.class);
        final FlowExecutionResult launchResult = executor.launchExecution(
                "test", new LocalAttributeMap(), new MockExternalContext());
        assertNotNull(launchResult.getPausedKey());
        try {
            final ClientFlowExecutionKey key = ClientFlowExecutionKey.parse(launchResult.getPausedKey());
            assertEquals(key.toString(), launchResult.getPausedKey());
        } catch (BadlyFormattedFlowExecutionKeyException e) {
            fail("Error parsing flow execution key: " + e.getMessage());
        }
        final MockExternalContext context = new MockExternalContext();
        context.setEventId("submit");
        context.getRequestMap().put("vegan", "0");
        final FlowExecutionResult resumeResult = executor.resumeExecution(launchResult.getPausedKey(), context);
        assertNotNull(resumeResult.getOutcome());
        assertEquals("lasagnaDinner", resumeResult.getOutcome().getId());
    }
}

