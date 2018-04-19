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

import org.cryptacular.bean.AEADBlockCipherBean;
import org.cryptacular.bean.BufferedBlockCipherBean;
import org.cryptacular.bean.KeyStoreFactoryBean;
import org.cryptacular.io.FileResource;
import org.cryptacular.spec.AEADBlockCipherSpec;
import org.cryptacular.spec.BufferedBlockCipherSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit test for {@link EncryptedTranscoder}.
 *
 * @author Marvin S. Addison
 */
@RunWith(Parameterized.class)
public class EncryptedTranscoderTest {

    private EncryptedTranscoder transcoder;

    private Serializable encodable;

    public EncryptedTranscoderTest(
            final EncryptedTranscoder transcoder, final Serializable encodable) {
        this.transcoder = transcoder;
        this.encodable = encodable;
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        final KeyStoreFactoryBean ksFactory = new KeyStoreFactoryBean();
        ksFactory.setResource(new FileResource(new File("src/test/resources/test-keystore.jceks")));
        ksFactory.setType("JCEKS");
        ksFactory.setPassword("changeit");

        // Test case #1
        // 128-bit AES in GCM mode with compression
        final EncryptedTranscoder transcoder1 = new EncryptedTranscoder();
        final AEADBlockCipherBean cipherBean1 = new AEADBlockCipherBean();
        cipherBean1.setBlockCipherSpec(new AEADBlockCipherSpec("AES", "GCM"));
        cipherBean1.setKeyStore(ksFactory.newInstance());
        cipherBean1.setKeyAlias("aes128");
        cipherBean1.setKeyPassword("changeit");
        cipherBean1.setNonce(new org.cryptacular.generator.sp80038d.RBGNonce());
        transcoder1.setCipherBean(cipherBean1);
        transcoder1.setCompression(true);

        // Test case #2
        // 128-bit AES in CBC mode without compression
        final EncryptedTranscoder transcoder2 = new EncryptedTranscoder();
        final BufferedBlockCipherBean cipherBean2 = new BufferedBlockCipherBean();
        cipherBean2.setBlockCipherSpec(new BufferedBlockCipherSpec("AES", "CBC", "PKCS7"));
        cipherBean2.setKeyStore(ksFactory.newInstance());
        cipherBean2.setKeyAlias("aes128");
        cipherBean2.setKeyPassword("changeit");
        cipherBean2.setNonce(new org.cryptacular.generator.sp80038a.RBGNonce());
        transcoder2.setCipherBean(cipherBean2);
        transcoder2.setCompression(false);

        return Arrays.asList(new Object[][] {
                {
                        transcoder1,
                        "Four score and seven years ago our forefathers brought forth upon this continent a " +
                                "new nation conceived in liberty and dedicated to the proposition that all men " +
                                "are created equal.",
                },
                {
                        transcoder2,
                        new URL("https://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&" +
                                "q=1600+Pennsylvania+Avenue+Northwest+Washington,+DC+20500&aq=&" +
                                "sll=38.897678,-77.036517&sspn=0.00835,0.007939&vpsrc=6&t=w&" +
                                "g=1600+Pennsylvania+Avenue+Northwest+Washington,+DC+20500&ie=UTF8&hq=&" +
                                "hnear=1600+Pennsylvania+Ave+NW,+Washington,+District+of+Columbia,+20500&" +
                                "ll=38.898521,-77.036517&spn=0.00835,0.007939&z=17&iwloc=A") },
        });
    }

    @Test
    public void testEncodeDecode() throws Exception {
        final byte[] encoded = this.transcoder.encode(this.encodable);
        assertEquals(this.encodable, this.transcoder.decode(encoded));
    }
}

