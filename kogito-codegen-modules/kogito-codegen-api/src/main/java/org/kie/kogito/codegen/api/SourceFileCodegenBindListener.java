/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.codegen.api;

/**
 * Interface to be notified when a codegen is bound to a source file.
 */
public interface SourceFileCodegenBindListener<T extends SourceFileCodegenBindEvent> {

    /**
     * Called when a codegen is bound to a source file.
     * 
     * @param event the event
     */
    void onSourceFileCodegenBind(T event);

    Class<T> getEventType();
}
