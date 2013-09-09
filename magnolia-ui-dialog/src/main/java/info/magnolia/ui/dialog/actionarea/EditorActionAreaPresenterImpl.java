/**
 * This file Copyright (c) 2010-2013 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.dialog.actionarea;

import com.vaadin.event.ShortcutListener;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionarea.definition.ActionRendererDefinition;
import info.magnolia.ui.dialog.actionarea.definition.EditorActionAreaDefinition;
import info.magnolia.ui.dialog.actionarea.renderer.ActionRenderer;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaView;
import info.magnolia.ui.dialog.definition.SecondaryActionDefinition;

import javax.inject.Inject;

/**
 * Implementation of {@link EditorActionAreaPresenter}.
 */
public class EditorActionAreaPresenterImpl implements EditorActionAreaPresenter {

    private final EditorActionAreaView view;

    private final ComponentProvider componentProvider;

    private final EditorActionExecutor actionExecutor;

    private ActionParameterProvider actionParameterProvider;

    @Inject
    public EditorActionAreaPresenterImpl(EditorActionAreaView view, ComponentProvider componentProvider, EditorActionExecutor actionExecutor) {
        this.view = view;
        this.componentProvider = componentProvider;
        this.actionExecutor = actionExecutor;
    }

    @Override
    public EditorActionAreaView start(Iterable<ActionDefinition> actions, EditorActionAreaDefinition definition, final ActionParameterProvider parameterProvider, UiContext uiContext) {
        this.actionParameterProvider = parameterProvider;
        actionExecutor.setActions(actions);
        for (ActionDefinition action : actions) {
            ActionRendererDefinition actionPresenterDef = definition.getActionRenderers().get(action.getName());
            ActionRenderer actionRenderer = actionPresenterDef == null ?
                    componentProvider.getComponent(ActionRenderer.class):
                    componentProvider.newInstance(actionPresenterDef.getPresenterClass(), action, actionPresenterDef, uiContext);
            final View actionView = actionRenderer.start(action, new ActionListener() {
                @Override
                public void onActionFired(String actionName, Object... actionContextParams) {
                    Object[] providedParameters = parameterProvider.getActionParameters(actionName);
                    Object[] combinedParameters = new Object[providedParameters.length + actionContextParams.length];
                    System.arraycopy(providedParameters, 0, combinedParameters, 0, providedParameters.length);
                    System.arraycopy(actionContextParams, 0, combinedParameters, providedParameters.length, actionContextParams.length);
                    executeAction(actionName, combinedParameters);
                }
            });
            if (definition.getSecondaryActions().contains(new SecondaryActionDefinition(action.getName()))) {
                view.addSecondaryAction(actionView, action.getName());
            } else {
                view.addPrimaryAction(actionView, action.getName());
            }
        }
        return view;
    }

    @Override
    public ShortcutListener bindShortcut(final String actionName, int keyCode, int... modifiers) {
        return new ShortcutListener("", keyCode, modifiers) {
            @Override
            public void handleAction(Object sender, Object target) {
                executeAction(actionName, actionParameterProvider.getActionParameters(actionName));
            }
        };
    }

    protected void executeAction(String actionName, Object[] combinedParameters) {
        try {
            actionExecutor.execute(actionName, combinedParameters);
        } catch (ActionExecutionException e) {
            handleActionExecutionException(actionName, e);
        }
    }

    protected EditorActionAreaView getView() {
        return view;
    }

    protected void handleActionExecutionException(String actionName, ActionExecutionException e) {
        throw new RuntimeException("Could not execute action: " + actionName, e);
    }
}
