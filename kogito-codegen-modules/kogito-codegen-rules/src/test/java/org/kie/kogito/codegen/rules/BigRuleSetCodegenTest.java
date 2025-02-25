/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.codegen.rules;

import java.util.ArrayList;
import java.util.Collection;

import org.drools.codegen.common.GeneratedFile;
import org.drools.util.io.ByteArrayResource;
import org.junit.jupiter.api.Test;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.codegen.api.utils.KogitoContextTestUtils.withLegacyApi;

public class BigRuleSetCodegenTest {

    @Test
    public void test() {
        // This test is used to check that compilation of large knowledge bases doesn't cause an Out Of Memory
        // We are not running the test with such a big kbase by default to avoid slowing down CI
        // int numberOfResource = 17;
        // int rulesPerResource = 1000;

        int numberOfResource = 2;
        int rulesPerResource = 3;

        Collection<Resource> resources = generateResourcesToBeCompiled(numberOfResource, rulesPerResource);

        RuleCodegen ruleCodegen = RuleCodegen.ofResources(
                withLegacyApi(JavaKogitoBuildContext.builder()).build(), resources);

        Collection<GeneratedFile> generatedFiles = ruleCodegen.generate();

        int legacyApiFiles = 2;
        int domainClasses = 1;
        assertThat(generatedFiles).hasSizeGreaterThan(legacyApiFiles + domainClasses);
    }

    private Collection<Resource> generateResourcesToBeCompiled(int numberOfResources, int rulesPerResource) {
        Collection<Resource> resources = new ArrayList<>();
        for (int i = 0; i < numberOfResources; i++) {
            Resource resource = new ByteArrayResource(generateRules("org.kie.kogito.codegen.test" + i, rulesPerResource).getBytes());
            resource.setResourceType(ResourceType.DRL);
            resource.setSourcePath("org/kie/kogito/codegen/test" + i + "/rules.drl");
            resources.add(resource);
        }
        return resources;
    }

    private String generateRules(String packageName, int n) {
        StringBuilder sb = new StringBuilder("package " + packageName + "\n");
        for (int i = 0; i < n; i++) {
            sb.append(generateRule(i));
        }
        return sb.toString();
    }

    private String generateRule(int seed) {
        return "rule R" + seed + " when\n" +
                "  $i: Integer( this == " + seed + " )\n" +
                "  $s: String( this == $i.toString() )\n" +
                "then\n" +
                "  System.out.println($s);\n" +
                "end\n";
    }
}
