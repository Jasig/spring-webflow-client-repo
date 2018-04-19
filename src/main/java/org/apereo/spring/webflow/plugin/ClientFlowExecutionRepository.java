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

import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionFactory;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.FlowExecutionKeyFactory;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;

import java.io.IOException;
import java.io.Serializable;

/**
 * Stores all flow execution state in {@link ClientFlowExecutionKey}, which effectively stores execution state on the
 * client in a form parameter when a view is rendered. The details of encoding flow state into a byte stream is handled
 * by a {@link Transcoder} component.
 *
 * @author Marvin S. Addison
 *
 * @see ClientFlowExecutionKey
 * @see Transcoder
 */
public class ClientFlowExecutionRepository implements FlowExecutionRepository, FlowExecutionKeyFactory {

    /** Client flow storage has not backing store independent from the flow key, so no locking is required. */
    private static final FlowExecutionLock NOOP_LOCK = new FlowExecutionLock() {
        public void lock() {}

        public void unlock() {}
    };

    private FlowExecutionFactory flowExecutionFactory;

    private FlowDefinitionLocator flowDefinitionLocator;

    private Transcoder transcoder;

    public ClientFlowExecutionRepository() {
    }

    public ClientFlowExecutionRepository(
            final FlowExecutionFactory flowExecutionFactory,
            final FlowDefinitionLocator flowDefinitionLocator,
            final Transcoder transcoder) {
        this.flowExecutionFactory = flowExecutionFactory;
        this.flowDefinitionLocator = flowDefinitionLocator;
        this.transcoder = transcoder;
    }

    public FlowExecutionKey parseFlowExecutionKey(final String encodedKey) throws FlowExecutionRepositoryException {
        return ClientFlowExecutionKey.parse(encodedKey);
    }

    @Override
    public FlowExecutionLock getLock(final FlowExecutionKey key) throws FlowExecutionRepositoryException {
        return NOOP_LOCK;
    }

    @Override
    public FlowExecution getFlowExecution(final FlowExecutionKey key) throws FlowExecutionRepositoryException {
        Assert.notNull(flowExecutionFactory, "FlowExecutionFactory cannot be null");
        Assert.notNull(flowDefinitionLocator, "FlowDefinitionLocator cannot be null");
        Assert.notNull(transcoder, "Transcoder cannot be null");
        
        if (!(key instanceof ClientFlowExecutionKey)) {
            throw new IllegalArgumentException(
                    "Expected instance of ClientFlowExecutionKey but got " + key.getClass().getName());
        }
        final byte[] encoded = ((ClientFlowExecutionKey) key).getData();
        try {
            final SerializedFlowExecutionState state = (SerializedFlowExecutionState) this.transcoder.decode(encoded);
            final FlowDefinition flow = this.flowDefinitionLocator.getFlowDefinition(state.getFlowId());
            return this.flowExecutionFactory.restoreFlowExecution(
                    state.getExecution(), flow, key, state.getConversationScope(), this.flowDefinitionLocator);
        } catch (final IOException e) {
            throw new ClientFlowExecutionRepositoryException("Error decoding flow execution", e);
        }
    }

    @Override
    public void putFlowExecution(final FlowExecution flowExecution) throws FlowExecutionRepositoryException {}

    @Override
    public void removeFlowExecution(final FlowExecution flowExecution) throws FlowExecutionRepositoryException {}

    @Override
    public FlowExecutionKey getKey(final FlowExecution execution) {
        try {
            return new ClientFlowExecutionKey(this.transcoder.encode(new SerializedFlowExecutionState(execution)));
        } catch (IOException e) {
            throw new ClientFlowExecutionRepositoryException("Error encoding flow execution", e);
        }
    }

    @Override
    public void updateFlowExecutionSnapshot(final FlowExecution execution) {}

    @Override
    public void removeFlowExecutionSnapshot(final FlowExecution execution) {}

    @Override
    public void removeAllFlowExecutionSnapshots(final FlowExecution execution) {}

    public FlowExecutionFactory getFlowExecutionFactory() {
        return flowExecutionFactory;
    }

    public FlowDefinitionLocator getFlowDefinitionLocator() {
        return flowDefinitionLocator;
    }

    public Transcoder getTranscoder() {
        return transcoder;
    }

    public void setFlowExecutionFactory(final FlowExecutionFactory flowExecutionFactory) {
        this.flowExecutionFactory = flowExecutionFactory;
    }

    public void setFlowDefinitionLocator(final FlowDefinitionLocator flowDefinitionLocator) {
        this.flowDefinitionLocator = flowDefinitionLocator;
    }

    public void setTranscoder(final Transcoder transcoder) {
        this.transcoder = transcoder;
    }

    private static class SerializedFlowExecutionState implements Serializable {
        private static final long serialVersionUID = -4020991769174829876L;

        private final String flowId;

        private final MutableAttributeMap conversationScope;

        private final FlowExecution execution;

        public SerializedFlowExecutionState(final FlowExecution execution) {
            this.execution = execution;
            this.flowId = execution.getDefinition().getId();
            this.conversationScope = execution.getConversationScope();
        }

        public String getFlowId() {
            return flowId;
        }

        public MutableAttributeMap getConversationScope() {
            return conversationScope;
        }

        public FlowExecution getExecution() {
            return execution;
        }
    }
}
