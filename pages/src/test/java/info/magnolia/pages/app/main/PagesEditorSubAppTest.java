/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.pages.app.main;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.rendering.template.TemplateAvailability;
import info.magnolia.rendering.template.configured.ConfiguredTemplateAvailability;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.pages.app.editor.PagesEditorSubApp;
import info.magnolia.pages.app.editor.PagesEditorSubAppView;
import info.magnolia.pages.app.editor.event.NodeSelectedEvent;
import info.magnolia.ui.contentapp.definition.ConfiguredEditorDefinition;
import info.magnolia.ui.contentapp.detail.ConfiguredDetailSubAppDescriptor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppContextImpl;
import info.magnolia.ui.vaadin.editor.PageEditorListener;
import info.magnolia.ui.vaadin.editor.pagebar.PageBarView;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link PagesEditorSubApp}.
 */
public class PagesEditorSubAppTest {

    private PageEditorPresenter pageEditorPresenter;
    private ActionExecutor actionExecutor;
    private SubAppContext subAppContext;
    private PagesEditorSubAppView view;
    private EventBus eventBus;
    private EventBus adminCentralEventBus;
    private ActionbarPresenter actionbarPresenter;
    private PageBarView pageBarView;
    private I18NAuthoringSupport i18NAuthoringSupport;
    private I18nContentSupport i18nContentSupport;
    private AbstractElement element;
    private ConfiguredTemplateDefinition definition = null;

    @Before
    public void setUp() throws Exception {

        // GIVEN
        MockWebContext ctx = new MockWebContext();
        Session session = mock(Session.class);
        MockNode component = new MockNode();
        component.setProperty("mgnl:template", "someTemplate");
        when(session.getNode(anyString())).thenReturn(component);
        ctx.addSession(null, session);
        User user = mock(User.class);
        Collection<String> groups = new ArrayList<String>();
        groups.add("this-user-group");
        when(user.getAllGroups()).thenReturn(groups);
        ctx.setUser(user);
        MgnlContext.setInstance(ctx);

        actionExecutor = mock(ActionExecutor.class);
        ConfiguredDetailSubAppDescriptor descriptor = new ConfiguredDetailSubAppDescriptor();
        descriptor.setEditor(new ConfiguredEditorDefinition());
        subAppContext = new SubAppContextImpl(descriptor, null);
        view = mock(PagesEditorSubAppView.class);
        eventBus = new SimpleEventBus();
        adminCentralEventBus = new SimpleEventBus();
        pageEditorPresenter = mock(PageEditorPresenter.class);
        TemplateDefinitionRegistry registry = mock(TemplateDefinitionRegistry.class);
        when(registry.getTemplateDefinition(anyString())).thenReturn(definition);
        actionbarPresenter = mock(ActionbarPresenter.class);
        i18NAuthoringSupport = mock(I18NAuthoringSupport.class);
        i18nContentSupport = mock(I18nContentSupport.class);
        when(i18nContentSupport.getLocale()).thenReturn(new Locale("en"));
        pageBarView = mock(PageBarView.class);

        ComponentsTestUtil.setImplementation(TemplateAvailability.class, ConfiguredTemplateAvailability.class);
        definition = new ConfiguredTemplateDefinition();
    }

    @Test
    public void testButtonsVisibilityIsNotChangedForOtherThenComponentElement() {
        // GIVEN
        element = new AreaElement(null, null, null, null);
        when(pageEditorPresenter.getSelectedElement()).thenReturn(element);
        PagesEditorSubApp editor = new PagesEditorSubApp(actionExecutor, subAppContext, view, adminCentralEventBus, eventBus, pageEditorPresenter, actionbarPresenter, pageBarView, i18NAuthoringSupport, i18nContentSupport);

        // WHEN
        eventBus.fireEvent(new NodeSelectedEvent(element));

        // THEN
        verify(actionbarPresenter).hideSection("pagePreviewActions", "pageActions", "areaActions", "optionalAreaActions", "editableAreaActions", "optionalEditableAreaActions", "componentActions", "pageDeleteActions");
        verify(actionbarPresenter).showSection("areaActions");
        verify(actionbarPresenter).disable("cancelMoveComponent", "copyComponent", "pasteComponent", "undo", "redo");

        verify(actionbarPresenter).enable(PageEditorListener.ACTION_ADD_COMPONENT);

        verifyNoMoreInteractions(actionbarPresenter);
    }

    @Test
    public void testHidingButtonsBasedOnOperationPermissionsForComponent() {
        // GIVEN
        ComponentElement element = new ComponentElement(null, null, null);
        element.setMoveable(true);
        element.setDeletable(false);
        when(pageEditorPresenter.getSelectedElement()).thenReturn(element);
        PagesEditorSubApp editor = new PagesEditorSubApp(actionExecutor, subAppContext, view, adminCentralEventBus, eventBus, pageEditorPresenter, actionbarPresenter, pageBarView, i18NAuthoringSupport, i18nContentSupport);

        // WHEN
        eventBus.fireEvent(new NodeSelectedEvent(element));

        // THEN
        verify(actionbarPresenter).hideSection("pagePreviewActions", "pageActions", "areaActions", "optionalAreaActions", "editableAreaActions", "optionalEditableAreaActions", "componentActions", "pageDeleteActions");
        verify(actionbarPresenter).showSection("componentActions");
        verify(actionbarPresenter).disable("cancelMoveComponent", "copyComponent", "pasteComponent", "undo", "redo");

        verify(actionbarPresenter).disable(PageEditorListener.ACTION_DELETE_COMPONENT);
        verify(actionbarPresenter).enable(PageEditorListener.ACTION_START_MOVE_COMPONENT);
        verify(actionbarPresenter).enable(PageEditorListener.ACTION_EDIT_ELEMENT);

        verifyNoMoreInteractions(actionbarPresenter);
    }

    @Test
    public void testHidingButtonsBasedOnOperationPermissionsForArea() {
        // GIVEN
        AreaElement element = new AreaElement(null, null, null, null);
        element.setAddible(false);
        when(pageEditorPresenter.getSelectedElement()).thenReturn(element);
        PagesEditorSubApp editor = new PagesEditorSubApp(actionExecutor, subAppContext, view, adminCentralEventBus, eventBus, pageEditorPresenter, actionbarPresenter, pageBarView, i18NAuthoringSupport, i18nContentSupport);

        // WHEN
        eventBus.fireEvent(new NodeSelectedEvent(element));

        // THEN
        verify(actionbarPresenter).hideSection("pagePreviewActions", "pageActions", "areaActions", "optionalAreaActions", "editableAreaActions", "optionalEditableAreaActions", "componentActions", "pageDeleteActions");
        verify(actionbarPresenter).showSection("areaActions");
        verify(actionbarPresenter).disable("cancelMoveComponent", "copyComponent", "pasteComponent", "undo", "redo");

        verify(actionbarPresenter).disable(PageEditorListener.ACTION_ADD_COMPONENT);

        verifyNoMoreInteractions(actionbarPresenter);
    }
}
