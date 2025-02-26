/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
package org.kie.kogito.addons.quarkus.camel.integration.test;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.emptyOrNullString;

@QuarkusIntegrationTest
public class CamelCustomFunctionIT {

    @Test
    void verifyBodyAndHeaders() {
        given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body("{ \"numbers\": 2 }")
                .post("/send_body_headers")
                .then()
                .statusCode(201)
                .assertThat()
                .body("workflowdata.id", notNullValue(String.class))
                .body("workflowdata.numbers", equalTo(2));
    }

    @Test
    void verifyBody() {
        given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body("{ \"numbers\": 2 }")
                .post("/send_body")
                .then()
                .statusCode(201)
                .assertThat()
                .body("workflowdata.id", emptyOrNullString())
                .body("workflowdata.numbers", equalTo(2));
    }

    @Test
    void verifyArbitraryBody() {
        given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body("{ \"numbers\": 2 }")
                .post("/send_arbitrary_body")
                .then()
                .statusCode(201)
                .assertThat()
                .body("workflowdata.id", emptyOrNullString())
                .body("workflowdata.numbers", equalTo(2));
    }

    @Test
    void verifyNoArgs() {
        given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body("{ \"numbers\": 2 }")
                .post("/send_nothing")
                .then()
                .statusCode(201)
                .assertThat()
                .body("workflowdata.id", emptyOrNullString())
                .body("workflowdata.numbers", equalTo(2));
    }
}
