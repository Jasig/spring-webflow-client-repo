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

import org.cryptacular.util.CodecUtil;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Spring Webflow execution id that contains the serialized flow execution state as part of the identifier.
 * Keys produced by this class have the form ID_BASE64 where ID is a globally unique identifier and BASE64
 * is the base-64 encoded bytes of a serialized object output stream.
 *
 * @author Marvin S. Addison
 */
public class ClientFlowExecutionKey extends FlowExecutionKey {

    public static final String KEY_FORMAT = "<uuid>_<base64-encoded-flow-state>";

    private static final long serialVersionUID = 3514659327458916297L;

    private static final int HASH_SEED = 31;

    private static final int HASH_FACTOR = 99;

    private UUID id;

    private byte[] data;


    public ClientFlowExecutionKey(final byte[] data) {
        this(UUID.randomUUID(), data);
    }

    public ClientFlowExecutionKey(final UUID id, final byte[] data) {
        Assert.notNull(id, "Flow execution id cannot be null.");
        this.id = id;
        this.data = data;
    }

    public UUID getId() {
        return this.id;
    }

    public byte[] getData() {
        return this.data;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof ClientFlowExecutionKey)) {
            return false;
        }
        final ClientFlowExecutionKey other = (ClientFlowExecutionKey) o;
        return this.id.equals(other.id) && Arrays.equals(this.data, other.data);
    }

    @Override
    public int hashCode() {
        int hash = HASH_SEED;
        hash += HASH_FACTOR * this.id.hashCode();
        for (int i = 0; i < this.data.length; i++) {
            hash += HASH_FACTOR * this.data[i];
        }
        return hash;
    }

    @Override
    public String toString() {
        return this.id + "_" + CodecUtil.b64(this.data);
    }

    public static ClientFlowExecutionKey parse(final String key) throws BadlyFormattedFlowExecutionKeyException {
        final String[] tokens = key.split("_");
        if (tokens.length != 2) {
            throw new BadlyFormattedFlowExecutionKeyException(key, KEY_FORMAT);
        }
        final UUID uuid;
        try {
            uuid = UUID.fromString(tokens[0]);
        } catch (Exception e) {
            throw new BadlyFormattedFlowExecutionKeyException(key, KEY_FORMAT);
        }
        final byte[] decoded;
        try {
            decoded = CodecUtil.b64(tokens[1]);
        } catch (Exception e) {
            throw new BadlyFormattedFlowExecutionKeyException(key, KEY_FORMAT);
        }
        return new ClientFlowExecutionKey(uuid, decoded);
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeUTF(toString());
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        final ClientFlowExecutionKey temp = parse(in.readUTF());
        this.id = temp.id;
        this.data = temp.data;
    }
}
