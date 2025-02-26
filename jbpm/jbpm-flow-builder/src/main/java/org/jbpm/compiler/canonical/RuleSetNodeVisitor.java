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
package org.jbpm.compiler.canonical;

import java.text.MessageFormat;

import org.drools.ruleunits.api.RuleUnitData;
import org.drools.ruleunits.api.SingletonStore;
import org.drools.ruleunits.impl.AssignableChecker;
import org.drools.ruleunits.impl.GeneratedRuleUnitDescription;
import org.drools.ruleunits.impl.ReflectiveRuleUnitDescription;
import org.drools.ruleunits.impl.factory.RuleUnitComponentFactoryImpl;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.factory.RuleSetNodeFactory;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.jbpm.workflow.instance.rule.DecisionRuleType;
import org.jbpm.workflow.instance.rule.RuleType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.ruleunit.RuleUnitComponentFactory;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.kogito.decision.DecisionModels;
import org.kie.kogito.rules.RuleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.UnknownType;

import static org.jbpm.ruleflow.core.factory.RuleSetNodeFactory.METHOD_DECISION;
import static org.jbpm.ruleflow.core.factory.RuleSetNodeFactory.METHOD_PARAMETER;

public class RuleSetNodeVisitor extends AbstractNodeVisitor<RuleSetNode> {

    public static final Logger logger = LoggerFactory.getLogger(ProcessToExecModelGenerator.class);

    private final AssignableChecker assignableChecker;

    public RuleSetNodeVisitor(ClassLoader contextClassLoader) {
        super(contextClassLoader);
        this.assignableChecker = AssignableChecker.create(contextClassLoader);
    }

    @Override
    protected String getNodeKey() {
        return "ruleSetNode";
    }

    @Override
    public void visitNode(String factoryField, RuleSetNode node, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        String nodeName = node.getName();

        body.addStatement(getAssignedFactoryMethod(factoryField, RuleSetNodeFactory.class, getNodeId(node), getNodeKey(), getWorkflowElementConstructor(node.getId())))
                .addStatement(getNameMethod(node, "Rule"));

        RuleType ruleType = node.getRuleType();
        if (ruleType.getName().isEmpty()) {
            throw new IllegalArgumentException(
                    MessageFormat.format(
                            "Rule task \"{0}\" is invalid: you did not set a unit name, a rule flow group or a decision model.", nodeName));
        }

        addParams(node, body, getNodeId(node));

        NameExpr methodScope = new NameExpr(getNodeId(node));
        MethodCallExpr m;
        if (ruleType.isRuleFlowGroup()) {
            m = handleRuleFlowGroup(ruleType);
        } else if (ruleType.isRuleUnit()) {
            m = handleRuleUnit(variableScope, metadata, node, nodeName, ruleType);
        } else if (ruleType.isDecision()) {
            m = handleDecision((DecisionRuleType) ruleType);
        } else {
            throw new IllegalArgumentException("Rule task " + nodeName + "is invalid: unsupported rule language " + node.getLanguage());
        }
        m.setScope(methodScope);
        body.addStatement(m);
        addNodeMappings(node, body, getNodeId(node));
        visitMetaData(node.getMetaData(), body, getNodeId(node));
        body.addStatement(getDoneMethod(getNodeId(node)));
    }

    private void addParams(RuleSetNode node, BlockStmt body, String nodeId) {
        node.getParameters()
                .forEach((k, v) -> body.addStatement(getFactoryMethod(nodeId, METHOD_PARAMETER, new StringLiteralExpr(k), new StringLiteralExpr(v.toString()))));
    }

    private MethodCallExpr handleDecision(DecisionRuleType ruleType) {

        StringLiteralExpr namespace = new StringLiteralExpr(ruleType.getNamespace());
        StringLiteralExpr model = new StringLiteralExpr(ruleType.getModel());
        Expression decision = ruleType.getDecision() == null ? new NullLiteralExpr() : new StringLiteralExpr(ruleType.getDecision());

        // app.get(org.kie.kogito.decision.DecisionModels.class).getDecisionModel(namespace, model)
        MethodCallExpr decisionModels =
                new MethodCallExpr(new NameExpr("app"), "get")
                        .addArgument(new ClassExpr().setType(DecisionModels.class.getCanonicalName()));
        MethodCallExpr decisionModel =
                new MethodCallExpr(decisionModels, "getDecisionModel")
                        .addArgument(namespace)
                        .addArgument(model);

        BlockStmt actionBody = new BlockStmt();
        LambdaExpr lambda = new LambdaExpr(new Parameter(new UnknownType(), "()"), actionBody);
        actionBody.addStatement(new ReturnStmt(decisionModel));

        return new MethodCallExpr(METHOD_DECISION)
                .addArgument(namespace)
                .addArgument(model)
                .addArgument(decision)
                .addArgument(lambda);
    }

    private MethodCallExpr handleRuleUnit(VariableScope variableScope, ProcessMetaData metadata, RuleSetNode ruleSetNode, String nodeName, RuleType ruleType) {
        String unitName = ruleType.getName();
        ProcessContextMetaModel processContext = new ProcessContextMetaModel(variableScope, getClassLoader());
        RuleUnitDescription description;

        try {
            Class<?> unitClass = loadUnitClass(unitName, metadata.getPackageName());
            description = new ReflectiveRuleUnitDescription((Class<? extends RuleUnitData>) unitClass);
        } catch (ClassNotFoundException e) {
            logger.warn("Rule task \"{}\": cannot load class {}. " +
                    "The unit data object will be generated.", nodeName, unitName);

            GeneratedRuleUnitDescription d = generateRuleUnitDescription(unitName, processContext);
            RuleUnitComponentFactoryImpl impl = (RuleUnitComponentFactoryImpl) RuleUnitComponentFactory.get();
            impl.registerRuleUnitDescription(d);
            description = d;
        }

        RuleUnitHandler handler = new RuleUnitHandler(description, processContext, ruleSetNode, assignableChecker);
        Expression ruleUnitFactory = handler.invoke();

        return new MethodCallExpr("ruleUnit")
                .addArgument(new StringLiteralExpr(ruleType.getName()))
                .addArgument(ruleUnitFactory);

    }

    private GeneratedRuleUnitDescription generateRuleUnitDescription(String unitName, ProcessContextMetaModel processContext) {
        GeneratedRuleUnitDescription d = new GeneratedRuleUnitDescription(unitName, getClassLoader());
        for (Variable variable : processContext.getVariables()) {
            d.putDatasourceVar(
                    variable.getName(),
                    SingletonStore.class.getCanonicalName(),
                    variable.getType().getStringType());
        }
        return d;
    }

    private MethodCallExpr handleRuleFlowGroup(RuleType ruleType) {
        // build supplier for rule runtime
        BlockStmt actionBody = new BlockStmt();
        LambdaExpr lambda = new LambdaExpr(new Parameter(new UnknownType(), "()"), actionBody);

        // app.config().get(org.kie.kogito.rules.RuleConfig.class)
        MethodCallExpr ruleConfig = new MethodCallExpr(
                new MethodCallExpr(new NameExpr("app"), "config"), "get")
                        .addArgument(new ClassExpr().setType(RuleConfig.class.getCanonicalName()));

        VariableDeclarationExpr configVar = new VariableDeclarationExpr(new ClassOrInterfaceType(null, RuleConfig.class.getCanonicalName()), "ruleConfig");
        actionBody.addStatement(new AssignExpr(configVar, ruleConfig, AssignExpr.Operator.ASSIGN));

        MethodCallExpr ruleRuntimeSupplier = new MethodCallExpr(
                new NameExpr("org.drools.project.model.ProjectRuntime.INSTANCE"), "newKieSession",
                NodeList.nodeList(new StringLiteralExpr("defaultStatelessKieSession")));

        VariableDeclarationExpr sessionVar = new VariableDeclarationExpr(new ClassOrInterfaceType(null, KieSession.class.getCanonicalName()), "ksession");
        actionBody.addStatement(new AssignExpr(sessionVar, ruleRuntimeSupplier, AssignExpr.Operator.ASSIGN));

        actionBody.addStatement(new JavaParser().parseStatement("ruleConfig.ruleEventListeners().agendaListeners().forEach(ksession::addEventListener);").getResult().get());
        actionBody.addStatement(new JavaParser().parseStatement("ruleConfig.ruleEventListeners().ruleRuntimeListeners().forEach(ksession::addEventListener);").getResult().get());

        actionBody.addStatement(new ReturnStmt("ksession"));

        return new MethodCallExpr("ruleFlowGroup")
                .addArgument(new StringLiteralExpr(ruleType.getName()))
                .addArgument(lambda);

    }

    private Class<?> loadUnitClass(String unitName, String packageName) throws ClassNotFoundException {
        ClassNotFoundException ex;
        try {
            return getClassLoader().loadClass(unitName);
        } catch (ClassNotFoundException e) {
            ex = e;
        }
        if (packageName == null || packageName.isEmpty()) {
            throw ex;
        }
        // maybe the name is not qualified. Let's try with tacking the packageName at the front
        try {
            return getClassLoader().loadClass(packageName + "." + unitName);
        } catch (ClassNotFoundException e) {
            // throw the original error
            throw ex;
        }
    }

    private boolean hasClass(String className) {
        try {
            loadUnitClass(className, null);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
