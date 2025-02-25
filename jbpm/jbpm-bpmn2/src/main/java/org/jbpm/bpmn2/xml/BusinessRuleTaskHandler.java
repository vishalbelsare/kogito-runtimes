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
package org.jbpm.bpmn2.xml;

import java.util.HashMap;
import java.util.Map;

import org.drools.core.xml.ExtensibleXmlParser;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.DataAssociation;
import org.jbpm.workflow.core.node.Assignment;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import static org.jbpm.workflow.core.node.RuleSetNode.DMN_LANG;

public class BusinessRuleTaskHandler extends AbstractNodeHandler {

    private static final String NAMESPACE_PROP = "namespace";
    private static final String MODEL_PROP = "model";
    private static final String DECISION_PROP = "decision";

    protected Node createNode(Attributes attrs) {
        return new RuleSetNode();
    }

    public Class<RuleSetNode> generateNodeFor() {
        return RuleSetNode.class;
    }

    protected Node handleNode(final Node node, final Element element, final String uri,
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);

        RuleSetNode ruleSetNode = (RuleSetNode) node;
        ruleSetNode.setIoSpecification(readIOEspecification(parser, element));

        String language = element.getAttribute("implementation");
        if (language == null || language.equalsIgnoreCase("##unspecified") || language.isEmpty()) {
            language = RuleSetNode.DRL_LANG;
        }
        ruleSetNode.setLanguage(language);

        String ruleFlowGroup = element.getAttribute("ruleFlowGroup");
        if (language.equals(DMN_LANG)) {
            Map<String, String> parameters = new HashMap<>();
            for (DataAssociation dataAssociation : ruleSetNode.getIoSpecification().getDataInputAssociation()) {
                for (Assignment assignment : dataAssociation.getAssignments()) {
                    parameters.put(assignment.getTo().getLabel(), assignment.getFrom().getExpression());
                }
            }

            String namespace = (String) parameters.get(NAMESPACE_PROP);
            String model = (String) parameters.get(MODEL_PROP);
            String decision = (String) parameters.get(DECISION_PROP);
            ruleSetNode.setRuleType(RuleSetNode.RuleType.decision(
                    namespace,
                    model,
                    decision));
        } else {
            ruleSetNode.setRuleType(RuleSetNode.RuleType.of(ruleFlowGroup, language));
        }

        handleScript(ruleSetNode, element, "onEntry");
        handleScript(ruleSetNode, element, "onExit");

        return ruleSetNode;
    }

    public void writeNode(Node node, StringBuilder xmlDump, int metaDataType) {
        RuleSetNode ruleSetNode = (RuleSetNode) node;
        writeNode("businessRuleTask", ruleSetNode, xmlDump, metaDataType);
        RuleSetNode.RuleType ruleType = ruleSetNode.getRuleType();
        if (ruleType != null) {
            xmlDump.append("g:ruleFlowGroup=\"" + XmlBPMNProcessDumper.replaceIllegalCharsAttribute(ruleType.getName()) + "\" " + EOL);
            // else DMN
        }

        xmlDump.append(" implementation=\"" + XmlBPMNProcessDumper.replaceIllegalCharsAttribute(ruleSetNode.getLanguage()) + "\" >" + EOL);

        writeExtensionElements(ruleSetNode, xmlDump);
        writeIO(ruleSetNode.getIoSpecification(), xmlDump);
        endNode("businessRuleTask", xmlDump);
    }

}
