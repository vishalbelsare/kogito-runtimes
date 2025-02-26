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
package org.kie.kogito.quarkus.workflow.deployment;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.BeforeAll;
import org.kie.kogito.codegen.process.persistence.proto.AbstractProtoGeneratorTest;
import org.kie.kogito.codegen.process.persistence.proto.ProtoGenerator;

/**
 * This class is intended to cover only JandexProtoGenerator specific tests (if any)
 *
 * NOTE: Add all tests to AbstractProtoGeneratorTest class to test both JandexProtoGenerator and ReflectionProtoGenerator
 */
class JandexProtoGeneratorTest extends AbstractProtoGeneratorTest<ClassInfo> {

    protected static Index indexWithAllClass;

    @BeforeAll
    protected static void indexOfTestClasses() {
        indexWithAllClass = JandexTestUtils.createTestIndex();
    }

    @Override
    protected ProtoGenerator.Builder<ClassInfo, JandexProtoGenerator> protoGeneratorBuilder() {
        return JandexProtoGenerator.builder(indexWithAllClass);
    }

    @Override
    protected ClassInfo convertToType(Class<?> clazz) {
        return JandexTestUtils.findClassInfo(indexWithAllClass, clazz);
    }

}
