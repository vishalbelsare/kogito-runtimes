/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.serverless.workflow;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.jbpm.ruleflow.core.Metadata;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.EventNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.TimerNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.serverless.workflow.parser.ServerlessWorkflowParser;
import org.kie.kogito.serverless.workflow.parser.schema.OpenApiModelSchemaGenerator;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.end.End;
import io.serverlessworkflow.api.start.Start;
import io.serverlessworkflow.api.states.DefaultState.Type;
import io.serverlessworkflow.api.states.SleepState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerlessWorkflowParsingTest extends AbstractServerlessWorkflowParsingTest {

    @ParameterizedTest
    @ValueSource(strings = { "/exec/single-operation.sw.json", "/exec/single-operation.sw.yml" })
    public void testSingleOperationWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(3, process.getNodes().length);

        Node node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof EndNode);

        // now check the composite one to see what nodes it has
        CompositeContextNode compositeNode = (CompositeContextNode) process.getNodes()[2];

        assertEquals(4, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[3];
        assertTrue(node instanceof EndNode);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/single-operation-with-delay.sw.json", "/exec/single-operation-with-delay.sw.yml" })
    public void testSingleOperationWithDelayWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(4, process.getNodes().length);

        Node node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[3];
        assertTrue(node instanceof TimerNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof EndNode);

        // now check the composite one to see what nodes it has
        CompositeContextNode compositeNode = (CompositeContextNode) process.getNodes()[2];

        assertEquals(4, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[3];
        assertTrue(node instanceof EndNode);

        TimerNode timerNode = (TimerNode) process.getNodes()[3];
        assertEquals("PT1S", timerNode.getTimer().getDelay());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/single-service-operation.sw.json", "/exec/single-service-operation.sw.yml" })
    public void testSingleServiceOperationWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(3, process.getNodes().length);

        Node node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof EndNode);

        // now check the composite one to see what nodes it has
        CompositeContextNode compositeNode = (CompositeContextNode) process.getNodes()[2];

        assertEquals(4, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof WorkItemNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[3];
        assertTrue(node instanceof EndNode);

        WorkItemNode workItemNode = (WorkItemNode) compositeNode.getNodes()[1];
        assertEquals("helloWorld", workItemNode.getName());
        assertEquals("org.something.other.TestService", workItemNode.getWork().getParameter("Interface"));
        assertEquals("get", workItemNode.getWork().getParameter("Operation"));
        assertEquals("org.something.other.TestService", workItemNode.getWork().getParameter("interfaceImplementationRef"));
        assertEquals("get", workItemNode.getWork().getParameter("operationImplementationRef"));
        assertEquals("Java", workItemNode.getWork().getParameter("implementation"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/single-eventstate.sw.json", "/exec/single-eventstate.sw.yml" })
    public void testSingleEventStateWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(4, process.getNodes().length);

        Node node = process.getNodes()[1];
        assertTrue(node instanceof StartNode);
        assertEquals(((StartNode) node).getTriggers().size(), 1);
        assertEquals(((StartNode) node).getMetaData(Metadata.TRIGGER_REF), "kafka");
        node = process.getNodes()[0];
        assertTrue(node instanceof EndNode);
        node = process.getNodes()[3];
        assertTrue(node instanceof CompositeContextNode);

        // now check the composite one to see what nodes it has
        CompositeContextNode compositeNode = (CompositeContextNode) process.getNodes()[3];

        assertEquals(4, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[3];
        assertTrue(node instanceof EndNode);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/single-eventstate-multi-eventrefs.sw.json", "/exec/single-eventstate-multi-eventrefs.sw.yml" })
    public void testSingleEventStateMultiEventRefsWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(7, process.getNodes().length);

        Node node = process.getNodes()[0];
        assertTrue(node instanceof EndNode);
        node = process.getNodes()[6];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[5];
        assertTrue(node instanceof Join);
        node = process.getNodes()[1];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[3];
        assertTrue(node instanceof StartNode);

        // now check the composite one to see what nodes it has
        CompositeContextNode compositeNode = (CompositeContextNode) process.getNodes()[6];

        assertEquals(4, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[3];
        assertTrue(node instanceof EndNode);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/single-operation-many-functions.sw.json", "/exec/single-operation-many-functions.sw.yml" })
    public void testSingleOperationWithManyFunctionsWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(3, process.getNodes().length);

        Node node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof EndNode);

        // now check the composite one to see what nodes it has
        CompositeContextNode compositeNode = (CompositeContextNode) process.getNodes()[2];

        assertEquals(6, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[3];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[4];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[5];
        assertTrue(node instanceof EndNode);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/multiple-operations.sw.json", "/exec/multiple-operations.sw.yml" })
    public void testMultipleOperationWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(5, process.getNodes().length);

        Node node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[3];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[4];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof EndNode);

        // now check the composite one to see what nodes it has
        CompositeContextNode compositeNode = (CompositeContextNode) process.getNodes()[2];

        assertEquals(4, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[3];
        assertTrue(node instanceof EndNode);

        compositeNode = (CompositeContextNode) process.getNodes()[3];

        assertEquals(4, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[3];
        assertTrue(node instanceof EndNode);

        compositeNode = (CompositeContextNode) process.getNodes()[4];

        assertEquals(4, compositeNode.getNodes().length);

        node = compositeNode.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = compositeNode.getNodes()[1];
        assertTrue(node instanceof Node);
        node = compositeNode.getNodes()[2];
        assertTrue(node instanceof ActionNode);
        node = compositeNode.getNodes()[3];
        assertTrue(node instanceof EndNode);

    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/single-inject-state.sw.json", "/exec/single-inject-state.sw.yml" })
    public void testSingleInjectWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("function", process.getId());
        assertEquals("test-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(3, process.getNodes().length);

        Node node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof ActionNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof EndNode);

        ActionNode actionNode = (ActionNode) process.getNodes()[2];
        assertEquals("SimpleInject", actionNode.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/parallel-state.sw.json", "/exec/parallel-state.sw.yml" })
    public void testParallelWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("parallelworkflow", process.getId());
        assertEquals("parallel-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(6, process.getNodes().length);

        Node node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof EndNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof Split);
        node = process.getNodes()[3];
        assertTrue(node instanceof Join);
        node = process.getNodes()[4];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[5];
        assertTrue(node instanceof CompositeContextNode);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/transition-produce-event.sw.json", "/exec/transition-produce-event.sw.yml" })
    public void testProduceEventOnTransition(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("produceeventontransition", process.getId());
        assertEquals("Produce Event On Transition", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(5, process.getNodes().length);
        Node node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[3];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[4];
        assertTrue(node instanceof ActionNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof EndNode);

        ActionNode actionNode = (ActionNode) process.getNodes()[4];
        assertEquals("TestKafkaEvent", actionNode.getName());
        assertEquals("ProduceMessage", actionNode.getMetaData("TriggerType"));
        assertEquals("workflowdata", actionNode.getMetaData("MappingVariable"));
        assertEquals("kafka", actionNode.getMetaData("TriggerRef"));
        assertEquals("com.fasterxml.jackson.databind.JsonNode", actionNode.getMetaData("MessageType"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/eventbased-switch-state.sw.json", "/exec/eventbased-switch-state.sw.yml" })
    public void testEventBasedSwitchWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("eventswitchworkflow", process.getId());
        assertEquals("event-switch-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(12, process.getNodes().length);

        Node node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof EndNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof EndNode);
        node = process.getNodes()[3];
        assertTrue(node instanceof ActionNode);
        node = process.getNodes()[4];
        assertTrue(node instanceof Split);
        node = process.getNodes()[5];
        assertTrue(node instanceof ActionNode);
        node = process.getNodes()[6];
        assertTrue(node instanceof ActionNode);
        node = process.getNodes()[7];
        assertTrue(node instanceof ActionNode);
        node = process.getNodes()[8];
        assertTrue(node instanceof EventNode);
        node = process.getNodes()[10];
        assertTrue(node instanceof EventNode);

        Split split = (Split) process.getNodes()[4];
        assertEquals("ChooseOnEvent", split.getName());
        assertEquals(Split.TYPE_XAND, split.getType());

        EventNode firstEventNode = (EventNode) process.getNodes()[8];
        assertEquals("visaApprovedEvent", firstEventNode.getName());

        EventNode secondEventNode = (EventNode) process.getNodes()[10];
        assertEquals("visaDeniedEvent", secondEventNode.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/prchecker.sw.json", "/exec/prchecker.sw.yml" })
    public void testPrCheckerWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("prchecker", process.getId());
        assertEquals("Github PR Checker Workflow", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(11, process.getNodes().length);

        Node node = process.getNodes()[5];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[4];
        assertTrue(node instanceof Join);
        node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[6];
        assertTrue(node instanceof Split);
        node = process.getNodes()[7];
        assertTrue(node instanceof Split);
        node = process.getNodes()[8];
        assertTrue(node instanceof ActionNode);
        node = process.getNodes()[9];
        assertTrue(node instanceof EndNode);
        node = process.getNodes()[10];
        assertTrue(node instanceof EndNode);

        Split split = (Split) process.getNodes()[6];
        assertEquals("CheckBackend", split.getName());
        assertEquals(2, split.getType());
        assertEquals(2, split.getConstraints().size());

        Split split2 = (Split) process.getNodes()[7];
        assertEquals("CheckFrontend", split2.getName());
        assertEquals(2, split2.getType());
        assertEquals(2, split2.getConstraints().size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/transition-produce-multi-events.sw.json", "/exec/transition-produce-multi-events.sw.yml" })
    public void testProduceMultiEventsOnTransition(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("produceeventontransition", process.getId());
        assertEquals("Produce Event On Transition", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(8, process.getNodes().length);
        Node node = process.getNodes()[0];
        assertTrue(node instanceof StartNode);
        node = process.getNodes()[1];
        assertTrue(node instanceof EndNode);
        node = process.getNodes()[2];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[3];
        assertTrue(node instanceof CompositeContextNode);
        node = process.getNodes()[4];
        assertTrue(node instanceof ActionNode);
        node = process.getNodes()[5];
        assertTrue(node instanceof ActionNode);
        node = process.getNodes()[6];
        assertTrue(node instanceof ActionNode);
        node = process.getNodes()[7];
        assertTrue(node instanceof ActionNode);

        ActionNode actionNode = (ActionNode) process.getNodes()[4];
        assertEquals("TestKafkaEvent", actionNode.getName());

        ActionNode actionNode2 = (ActionNode) process.getNodes()[5];
        assertEquals("TestKafkaEvent2", actionNode2.getName());

        ActionNode actionNode3 = (ActionNode) process.getNodes()[6];
        assertEquals("TestKafkaEvent3", actionNode3.getName());

        ActionNode actionNode4 = (ActionNode) process.getNodes()[7];
        assertEquals("TestKafkaEvent4", actionNode4.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/switch-state-produce-events.sw.json", "/exec/switch-state-produce-events.sw.yml" })
    public void testSwitchProduceEventsOnTransitionWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("switchworkflow", process.getId());
        assertEquals("switch-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(16, process.getNodes().length);

        Split split = (Split) process.getNodes()[4];
        assertEquals("ChooseOnAge", split.getName());
        assertEquals(2, split.getType());
        assertEquals(2, split.getConstraints().size());

        boolean haveDefaultConstraint = false;
        for (Constraint constraint : split.getConstraints().values()) {
            haveDefaultConstraint = haveDefaultConstraint || constraint.isDefault();
        }

        assertTrue(haveDefaultConstraint);
    }

    @ParameterizedTest
    @ValueSource(strings = "/exec/switch-state-produce-events-default.sw.json")
    public void testSwitchProduceEventsDefaultOnTransitionWorkflow(String workflowLocation) throws Exception {
        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertEquals("switchworkflow", process.getId());
        assertEquals("switch-wf", process.getName());
        assertEquals("1.0", process.getVersion());
        assertEquals("org.kie.kogito.serverless", process.getPackageName());
        assertEquals(RuleFlowProcess.PUBLIC_VISIBILITY, process.getVisibility());

        assertEquals(17, process.getNodes().length);

        Split split = (Split) process.getNodes()[4];
        assertEquals("ChooseOnAge", split.getName());
        assertEquals(2, split.getType());
        assertEquals(2, split.getConstraints().size());

        boolean haveDefaultConstraint = false;
        for (Constraint constraint : split.getConstraints().values()) {
            haveDefaultConstraint = haveDefaultConstraint || constraint.isDefault();
        }

        assertTrue(haveDefaultConstraint);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/examples/applicantworkflow.sw.json", "/exec/error.sw.json", "/exec/callback.sw.json", "/exec/compensation.sw.json", "/exec/compensation.end.sw.json",
            "/exec/foreach.sw.json" })
    public void testSpecExamplesParsing(String workflowLocation) throws IOException {
        Workflow workflow = Workflow.fromSource(WorkflowTestUtils.readWorkflowFile(workflowLocation));

        assertNotNull(workflow);
        assertNotNull(workflow.getId());
        assertNotNull(workflow.getName());
        assertNotNull(workflow.getStates());
        assertTrue(workflow.getStates().size() > 0);

        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertNotNull(process);
        assertNotNull(process.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/exec/expression.schema.sw.json" })
    public void testSpecWithInputSchema(String workflowLocation) throws IOException {
        Workflow workflow = Workflow.fromSource(WorkflowTestUtils.readWorkflowFile(workflowLocation));

        assertNotNull(workflow);
        assertNotNull(workflow.getDataInputSchema());
        assertTrue(workflow.getStates().size() > 0);

        RuleFlowProcess process = (RuleFlowProcess) getWorkflowParser(workflowLocation);
        assertNotNull(process);
        assertNotNull(process.getId());

        assertEquals(OpenApiModelSchemaGenerator.INPUT_MODEL_REF, process.getMetaData(Metadata.DATA_INPUT_SCHEMA_REF));
    }

    @Test
    public void testMinimumWorkflow() {
        Workflow workflow = createMinimumWorkflow();
        ServerlessWorkflowParser parser = ServerlessWorkflowParser.of(workflow, JavaKogitoBuildContext.builder().build());
        Process process = parser.getProcessInfo().info();
        assertSame(process, parser.getProcessInfo().info());
        assertEquals(ServerlessWorkflowParser.DEFAULT_NAME, process.getName());
        assertEquals(ServerlessWorkflowParser.DEFAULT_VERSION, process.getVersion());
        assertEquals(ServerlessWorkflowParser.DEFAULT_PACKAGE, process.getPackageName());
    }

    private static Workflow createMinimumWorkflow() {
        Workflow workflow = new Workflow();
        workflow.setId("javierito");
        Start start = new Start();
        start.setStateName("javierito");
        End end = new End();
        end.setTerminate(true);
        SleepState startState = new SleepState();
        startState.setType(Type.SLEEP);
        startState.setDuration("1s");
        startState.setName("javierito");
        startState.setEnd(end);
        workflow.setStates(List.of(startState));
        workflow.setStart(start);

        return workflow;
    }

    @Test
    void testWorkflowWithAnnotations() {
        List<String> annotations = List.of("machine learning", "monitoring", "networking");

        Workflow workflow = createMinimumWorkflow().withAnnotations(annotations);

        ServerlessWorkflowParser parser = ServerlessWorkflowParser.of(workflow, JavaKogitoBuildContext.builder().build());
        Process process = parser.getProcessInfo().info();

        // Simplify the following assertions when https://github.com/smallrye/smallrye-open-api/pull/1093 is merged and released

        assertThat(process.getMetaData())
                .containsKey(Metadata.TAGS)
                .hasEntrySatisfying(Metadata.TAGS, tags -> assertThat(tags).isInstanceOf(Collection.class));

        @SuppressWarnings("unchecked")
        Collection<Tag> tags = (Collection<Tag>) process.getMetaData().get(Metadata.TAGS);

        annotations.forEach(annotation -> assertThat(tags).anyMatch(tag -> tag.getName().equals(annotation)));
    }

    @Test
    void workflowWithoutAnnotationsShouldResultInProcessWithoutTags() {
        Workflow workflow = createMinimumWorkflow();

        ServerlessWorkflowParser parser = ServerlessWorkflowParser.of(workflow, JavaKogitoBuildContext.builder().build());
        Process process = parser.getProcessInfo().info();

        assertThat(process.getMetaData()).doesNotContainKey(Metadata.TAGS);
    }

    @Test
    void testWorkflowWithDescription() {
        String description = "This is a description";

        String workflowId = "my-workflow";

        Workflow workflow = createMinimumWorkflow()
                .withId(workflowId)
                .withDescription(description);

        ServerlessWorkflowParser parser = ServerlessWorkflowParser.of(workflow, JavaKogitoBuildContext.builder().build());
        Process process = parser.getProcessInfo().info();

        // Simplify the following assertions when https://github.com/smallrye/smallrye-open-api/pull/1093 is merged and released

        assertThat(process.getMetaData())
                .containsKey(Metadata.TAGS)
                .hasEntrySatisfying(Metadata.TAGS, tags -> assertThat(tags).isInstanceOf(Collection.class));

        @SuppressWarnings("unchecked")
        Collection<Tag> tags = (Collection<Tag>) process.getMetaData().get(Metadata.TAGS);

        assertThat(tags).anyMatch(tag -> workflowId.equals(tag.getName()) && description.equals(tag.getDescription()));
    }
}
