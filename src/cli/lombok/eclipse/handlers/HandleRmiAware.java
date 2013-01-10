/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lombok.eclipse.handlers;

import griffon.plugins.rmi.RmiAware;
import lombok.core.AnnotationValues;
import lombok.core.handlers.RmiAwareConstants;
import lombok.core.handlers.RmiAwareHandler;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.ast.EclipseType;
import org.eclipse.jdt.internal.compiler.ast.Annotation;

import static lombok.core.util.ErrorMessages.canBeUsedOnClassAndEnumOnly;

/**
 * @author Andres Almiray
 */
public class HandleRmiAware extends EclipseAnnotationHandler<RmiAware> {
    private final EclipseRmiAwareHandler handler = new EclipseRmiAwareHandler();

    @Override
    public void handle(AnnotationValues<RmiAware> annotation, Annotation source, EclipseNode annotationNode) {
        EclipseType type = EclipseType.typeOf(annotationNode, source);
        if (type.isAnnotation() || type.isInterface()) {
            annotationNode.addError(canBeUsedOnClassAndEnumOnly(RmiAware.class));
            return;
        }

        EclipseUtil.addInterface(type.get(), RmiAwareConstants.RMI_CONTRIBUTION_HANDLER_TYPE, source);
        handler.addRmiProviderField(type);
        handler.addRmiProviderAccessors(type);
        handler.addRmiContributionMethods(type);
        type.editor().rebuild();
    }

    private static class EclipseRmiAwareHandler extends RmiAwareHandler<EclipseType> {
    }
}
