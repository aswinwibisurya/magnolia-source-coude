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
package info.magnolia.ui.contentapp.field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.ui.admincentral.field.builder.LinkFieldBuilder;
import info.magnolia.ui.form.field.builder.AbstractBuilderTest;
import info.magnolia.ui.form.field.builder.AbstractFieldBuilderTest;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemSelectedEvent;

import javax.jcr.RepositoryException;

import org.junit.Test;

import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

/**
 * Tests.
 */
public class LinkFieldSelectionBuilderTest extends AbstractBuilderTest<LinkFieldSelectionDefinition> {

    private LinkFieldSelectionBuilder builder;

    private WorkbenchPresenter workbenchPresenter;

    private EventBus eventBus;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        workbenchPresenter = mock(WorkbenchPresenter.class);
        eventBus = new SimpleEventBus();
        // make sure that workbench view registers a content view so that restore selection doesn't fail.
        WorkbenchView workbenchView = mock(WorkbenchView.class);
        doReturn(mock(Component.class)).when(workbenchView).asVaadinComponent();
        doReturn(workbenchView).when(workbenchPresenter).start(any(WorkbenchDefinition.class), any(ImageProviderDefinition.class), any(EventBus.class));
    }

    @Test
    public void buildFieldSimpleTest() {
        // GIVEN
        baseItem.addItemProperty(LinkFieldBuilder.PATH_PROPERTY_NAME, DefaultPropertyUtil.newDefaultProperty(null, null));
        builder = new LinkFieldSelectionBuilder(definition, baseItem, workbenchPresenter, eventBus);
        builder.setI18nContentSupport(i18nContentSupport);

        // WHEN
        Field field = builder.getField();

        // THEN
        assertEquals(true, field instanceof TextAndContentViewField);
        assertEquals(true, ((TextAndContentViewField) field).getTextField().isVisible());
    }

    @Test
    public void fieldEventTest() throws RepositoryException {
        // GIVEN
        baseItem.addItemProperty(LinkFieldBuilder.PATH_PROPERTY_NAME, DefaultPropertyUtil.newDefaultProperty(null, null));
        builder = new LinkFieldSelectionBuilder(definition, baseItem, workbenchPresenter, eventBus);
        builder.setI18nContentSupport(i18nContentSupport);
        Field field = builder.getField();

        // WHEN
        eventBus.fireEvent(new ItemSelectedEvent(baseNode.getSession().getWorkspace().getName(), (JcrItemAdapter) baseItem));

        // THEN
        // as No columnName defined return the Item path as Value property
        assertEquals(baseNode.getPath(), field.getValue());
    }

    @Test
    public void fieldEventCustomPropertyTest() throws RepositoryException {
        // GIVEN
        baseNode.setProperty("newProperty", "initial");
        baseItem = new JcrNodeAdapter(baseNode);
        baseItem.addItemProperty("newProperty", DefaultPropertyUtil.newDefaultProperty(null, "initial"));
        builder = new LinkFieldSelectionBuilder(definition, baseItem, workbenchPresenter, eventBus);
        builder.setI18nContentSupport(i18nContentSupport);
        Field field = builder.getField();

        // WHEN
        eventBus.fireEvent(new ItemSelectedEvent(baseNode.getSession().getWorkspace().getName(), (JcrItemAdapter) baseItem));

        // THEN
        assertEquals("initial", field.getValue());
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        LinkFieldSelectionDefinition fieldDefinition = new LinkFieldSelectionDefinition();
        fieldDefinition = (LinkFieldSelectionDefinition) AbstractFieldBuilderTest.createConfiguredFieldDefinition(fieldDefinition, propertyName);
        this.definition = fieldDefinition;
    }

}
