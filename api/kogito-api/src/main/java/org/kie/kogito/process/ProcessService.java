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
package org.kie.kogito.process;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.kie.kogito.MapOutput;
import org.kie.kogito.MappableToModel;
import org.kie.kogito.Model;
import org.kie.kogito.auth.SecurityPolicy;
import org.kie.kogito.process.workitem.Attachment;
import org.kie.kogito.process.workitem.AttachmentInfo;
import org.kie.kogito.process.workitem.Comment;

public interface ProcessService {

    <T extends Model> ProcessInstance<T> createProcessInstance(Process<T> process, String businessKey,
            T model,
            String startFromNodeId);

    <T extends Model> ProcessInstance<T> createProcessInstance(Process<T> process, String businessKey, T model,
            String startFromNodeId,
            String trigger,
            String kogitoReferenceId);

    <T extends MappableToModel<R>, R> List<R> getProcessInstanceOutput(Process<T> process);

    <T extends MappableToModel<R>, R> Optional<R> findById(Process<T> process, String id);

    <T extends MappableToModel<R>, R> Optional<R> delete(Process<T> process, String id);

    <T extends MappableToModel<R>, R> Optional<R> update(Process<T> process, String id, T resource);

    <T extends Model> Optional<List<WorkItem>> getTasks(Process<T> process, String id, SecurityPolicy policy);

    <T extends Model> Optional<WorkItem> signalTask(Process<T> process, String id, String taskNodeName);

    <T extends Model, R extends MapOutput> Optional<R> saveTask(Process<T> process,
            String id,
            String taskId,
            SecurityPolicy policy,
            MapOutput model,
            Function<Map<String, Object>, R> mapper);

    <T extends MappableToModel<R>, R> Optional<R> taskTransition(
            Process<T> process,
            String id,
            String taskId,
            String phase,
            SecurityPolicy policy,
            MapOutput model);

    <T extends MappableToModel<?>, R> Optional<R> getTask(Process<T> process,
            String id,
            String taskId,
            SecurityPolicy policy,
            Function<WorkItem, R> mapper);

    <T extends Model> Optional<Comment> addComment(Process<T> process,
            String id,
            String taskId,
            SecurityPolicy policy,
            String commentInfo);

    <T extends Model> Optional<Comment> updateComment(Process<T> process,
            String id,
            String taskId,
            String commentId,
            SecurityPolicy policy,
            String commentInfo);

    <T extends Model> Optional<Boolean> deleteComment(Process<T> process,
            String id,
            String taskId,
            String commentId,
            SecurityPolicy policy);

    <T extends Model> Optional<Attachment> addAttachment(Process<T> process,
            String id,
            String taskId,
            SecurityPolicy policy,
            AttachmentInfo attachmentInfo);

    <T extends Model> Optional<Attachment> updateAttachment(Process<T> process,
            String id,
            String taskId,
            String attachmentId,
            SecurityPolicy policy,
            AttachmentInfo attachment);

    <T extends Model> Optional<Boolean> deleteAttachment(Process<T> process,
            String id,
            String taskId,
            String attachmentId,
            SecurityPolicy policy);

    <T extends Model> Optional<Attachment> getAttachment(Process<T> process,
            String id,
            String taskId,
            String attachmentId,
            SecurityPolicy policy);

    <T extends Model> Optional<Collection<Attachment>> getAttachments(Process<T> process,
            String id,
            String taskId,
            SecurityPolicy policy);

    <T extends Model> Optional<Comment> getComment(Process<T> process,
            String id,
            String taskId,
            String commentId,
            SecurityPolicy policy);

    <T extends Model> Optional<Collection<Comment>> getComments(Process<T> process,
            String id,
            String taskId,
            SecurityPolicy policy);

    <T extends MappableToModel<R>, R> Optional<R> signalProcessInstance(Process<T> process, String id, Object data, String signalName);

    //Schema
    <T extends Model> Map<String, Object> getSchemaAndPhases(Process<T> process,
            String id,
            String taskId,
            String taskName,
            SecurityPolicy policy);
}
