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
package org.kie.kogito.serverless.workflow.suppliers;

import org.jbpm.compiler.canonical.ExpressionSupplier;
import org.jbpm.compiler.canonical.ProcessMetaData;
import org.jbpm.compiler.canonical.descriptors.ExpressionUtils;
import org.kie.kogito.internal.process.runtime.KogitoNode;
import org.kie.kogito.serverless.workflow.actions.SysoutAction;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

public class SysoutActionSupplier extends SysoutAction implements ExpressionSupplier {

    private final ObjectCreationExpr expression;

    public SysoutActionSupplier(String lang, String expr, String inputVar, String... addVars) {
        super(lang, expr, inputVar, addVars);
        this.expression = ExpressionUtils.getObjectCreationExpr(SysoutAction.class, lang, expr, inputVar);
        for (String addVar : addVars) {
            expression.addArgument(ExpressionUtils.getLiteralExpr(addVar));
        }
    }

    @Override
    public Expression get(KogitoNode node, ProcessMetaData metadata) {
        return expression;
    }

}
