/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.aws2.sns.integration;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.sns.Sns2Constants;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperties;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// Must be manually tested. Provide your own accessKey and secretKey using -Daws.manual.access.key and -Daws.manual.secret.key
@EnabledIfSystemProperties({
        @EnabledIfSystemProperty(named = "aws.manual.access.key", matches = ".*", disabledReason = "Access key not provided"),
        @EnabledIfSystemProperty(named = "aws.manual.secret.key", matches = ".*", disabledReason = "Secret key not provided")
})
public class SnsTopicWithKmsEncryptionManualIT extends CamelTestSupport {

    @Test
    public void sendInOnly() throws Exception {
        Exchange exchange = template.send("direct:start", ExchangePattern.InOnly, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Sns2Constants.SUBJECT, "This is my subject");
                exchange.getIn().setBody("This is my message text.");
            }
        });

        assertNotNull(exchange.getIn().getHeader(Sns2Constants.MESSAGE_ID));
    }

    @Test
    public void sendInOut() throws Exception {
        Exchange exchange = template.send("direct:start", ExchangePattern.InOut, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Sns2Constants.SUBJECT, "This is my subject");
                exchange.getIn().setBody("This is my message text.");
            }
        });

        assertNotNull(exchange.getMessage().getHeader(Sns2Constants.MESSAGE_ID));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .to("aws2-sns://MyNewTopic1?accessKey=RAW({{aws.manual.access.key}})&secretKey=RAW({{aws.manual.secret.key}})"
                            + "&region=EU_WEST_1&subject=The+subject+message&serverSideEncryptionEnabled=true&"
                            + "kmsMasterKeyId=RAW(xxx)&autoCreateTopic=true");
            }
        };
    }
}
