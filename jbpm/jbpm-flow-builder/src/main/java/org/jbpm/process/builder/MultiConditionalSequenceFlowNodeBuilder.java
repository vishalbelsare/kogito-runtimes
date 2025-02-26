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
package org.jbpm.process.builder;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.drools.drl.ast.descr.ProcessDescr;
import org.drools.drl.ast.descr.ReturnValueDescr;
import org.jbpm.process.builder.dialect.ProcessDialect;
import org.jbpm.process.builder.dialect.ProcessDialectRegistry;
import org.jbpm.process.instance.impl.ReturnValueConstraintEvaluator;
import org.jbpm.process.instance.impl.RuleConstraintEvaluator;
import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.impl.ConnectionRef;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.Split;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;

public class MultiConditionalSequenceFlowNodeBuilder implements ProcessNodeBuilder {

    public void build(Process process, ProcessDescr processDescr,
            ProcessBuildContext context, Node node) {

        // exclude split as it is handled with separate builder and nodes with non conditional sequence flows
        if (node instanceof Split) {
            return;
        }

        // we need to clone the map, so we can update the original while iterating.
        for (Iterator<Map.Entry<ConnectionRef, Collection<Constraint>>> it = ((NodeImpl) node).getConstraints().entrySet().iterator(); it.hasNext();) {
            Map.Entry<ConnectionRef, Collection<Constraint>> entry = it.next();
            ConnectionRef connection = entry.getKey();
            Collection<Constraint> constraints = new LinkedList<>(entry.getValue());
            Connection outgoingConnection = null;
            for (Connection out : ((NodeImpl) node).getDefaultOutgoingConnections()) {
                if (out.getToType().equals(connection.getToType())
                        && out.getTo().getId().equals(connection.getNodeId())) {
                    outgoingConnection = out;
                }
            }
            if (outgoingConnection == null) {
                throw new IllegalArgumentException("Could not find outgoing connection");
            }
            for (Constraint constraint : constraints) {
                if (constraint != null) {
                    if ("rule".equals(constraint.getType())) {
                        RuleConstraintEvaluator ruleConstraint = new RuleConstraintEvaluator();
                        ruleConstraint.setDialect(constraint.getDialect());
                        ruleConstraint.setName(constraint.getName());
                        ruleConstraint.setPriority(constraint.getPriority());
                        ruleConstraint.setDefault(constraint.isDefault());
                        ((NodeImpl) node).setConstraint(outgoingConnection, ruleConstraint);
                    } else if ("code".equals(constraint.getType())) {
                        ReturnValueConstraintEvaluator returnValueConstraint = new ReturnValueConstraintEvaluator();
                        returnValueConstraint.setDialect(constraint.getDialect());
                        returnValueConstraint.setName(constraint.getName());
                        returnValueConstraint.setPriority(constraint.getPriority());
                        returnValueConstraint.setDefault(constraint.isDefault());
                        ((NodeImpl) node).setConstraint(outgoingConnection, returnValueConstraint);

                        ReturnValueDescr returnValueDescr = new ReturnValueDescr();
                        returnValueDescr.setText(constraint.getConstraint());
                        returnValueDescr.setResource(processDescr.getResource());

                        ProcessDialect dialect = ProcessDialectRegistry.getDialect(constraint.getDialect());
                        dialect.getReturnValueEvaluatorBuilder().build(context, returnValueConstraint, returnValueDescr, (NodeImpl) node);
                    }
                }
            }
        }

    }

}
