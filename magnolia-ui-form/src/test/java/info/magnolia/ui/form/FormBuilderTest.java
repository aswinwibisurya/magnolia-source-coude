/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.form;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.field.builder.FieldFactory;
import info.magnolia.ui.form.field.builder.TextFieldBuilder;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.vaadin.form.FormView;
import info.magnolia.ui.vaadin.form.ItemFormView;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Locale;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * FormBuilderTest.
 */
public class FormBuilderTest {
    private final String workspaceName = "workspace";

    private MockSession session;

    @Before
    public void setUp() {

        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        SystemContext systemContext = mock(SystemContext.class);
        when(systemContext.getLocale()).thenReturn(new Locale("en"));
        ComponentsTestUtil.setInstance(SystemContext.class, systemContext);
        session = new MockSession(workspaceName);
        MockContext ctx = new MockContext();
        ctx.addSession(workspaceName, session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void testBuildingWithoutTabsAndActions() {
        // GIVEN
        final FormDefinition def = new ConfiguredFormDefinition();
        final FormView form = new ItemFormView();
        final FormBuilder builder = new FormBuilder(null, form, null);

        // WHEN
        final FormView result = builder.buildForm(def, null, null);

        // THEN
        assertEquals(result, form);
    }

    @Test
    public void testBuildingWithTabsAndActions() throws Exception {
        // GIVEN
        final String propertyName = "test";
        final ConfiguredFormDefinition formDef = new ConfiguredFormDefinition();
        final TextFieldDefinition fieldTypeDef = new TextFieldDefinition();
        fieldTypeDef.setName(propertyName);

        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyValue = "value";
        underlyingNode.setProperty(propertyName, propertyValue);
        final JcrNodeAdapter item = new JcrNodeAdapter(underlyingNode);

        final FormView form = new ItemFormView();


        final ConfiguredTabDefinition tabDef = new ConfiguredTabDefinition();
        final ConfiguredFieldDefinition fieldDef = new ConfiguredFieldDefinition();
        fieldDef.setName(propertyName);
        tabDef.addField(fieldDef);
        formDef.addTab(tabDef);

        final FieldFactory fieldFactory = mock(FieldFactory.class);
        TextFieldBuilder editField = new TextFieldBuilder(fieldTypeDef, item);
        DefaultI18nContentSupport i18nContentSupport = new DefaultI18nContentSupport();
        i18nContentSupport.setFallbackLocale(new Locale("en"));
        editField.setI18nContentSupport(i18nContentSupport);
        when(fieldFactory.create(same(fieldDef), same(item))).thenReturn(editField);

        final FormBuilder builder = new FormBuilder(fieldFactory, form, null);
        // WHEN
        final FormView result = builder.buildForm(formDef, item, null);

        // THEN
        assertEquals(result, form);
    }
}
