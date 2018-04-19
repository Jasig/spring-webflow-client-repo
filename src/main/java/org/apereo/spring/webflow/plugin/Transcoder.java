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

import java.io.IOException;

/**
 * Provides a strategy pattern interface for transforming an object into a byte array and vice versa.
 * <code>Transcoder</code> components are used by {@link ClientFlowExecutionRepository} for producing the data stored
 * in a {@link ClientFlowExecutionKey}.
 *
 * @author Marvin S. Addison
 *
 * @see ClientFlowExecutionKey
 * @see ClientFlowExecutionRepository
 */
public interface Transcoder {

    /**
     * Encodes an object into a stream of bytes.
     *
     * @param o Object to encode.
     *
     * @return Object encoded as a byte array.
     *
     * @throws IOException On encoding errors.
     */
    byte[] encode(Object o) throws IOException;


    /**
     * Decodes a stream of bytes produced by {@link #encode(Object)} back into the original object.
     *
     * @param encoded Encoded representation of an object.
     *
     * @return Object decoded from byte array.
     *
     * @throws IOException On decoding errors.
     */
    Object decode(byte[] encoded) throws IOException;
}
