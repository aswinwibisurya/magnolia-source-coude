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
package info.magnolia.ui.admincentral.field.builder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.field.MultiLinkField;
import info.magnolia.ui.form.field.builder.AbstractBuilderTest;
import info.magnolia.ui.form.field.definition.MultiLinkFieldDefinition;
import info.magnolia.ui.form.field.property.SingleValueHandler;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.List;

import org.junit.Test;

import com.vaadin.ui.Field;

/**
 * Main testcase for {@link MultiLinkFieldBuilder}.
 */
public class MultiLinkFieldBuilderTest extends AbstractBuilderTest<MultiLinkFieldDefinition> {

    private MultiLinkFieldBuilder multiLinkFieldBuilder;
    private ComponentProvider componentProvider;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        componentProvider = mock(ComponentProvider.class);
    }

    @Test
    public void simpleMultiLinkFieldBuilderTest() throws Exception {
        // GIVEN
        when(componentProvider.newInstance(SingleValueHandler.class, baseItem, definition.getName())).thenReturn(new SingleValueHandler((JcrNodeAdapter) baseItem, propertyName));
        multiLinkFieldBuilder = new MultiLinkFieldBuilder(definition, baseItem, null, null, componentProvider);
        multiLinkFieldBuilder.setI18nContentSupport(i18nContentSupport);
        // WHEN
        Field field = multiLinkFieldBuilder.getField();

        // THEN
        assertEquals(true, field instanceof MultiLinkField);
    }

    @Test
    public void simpleMultiLinkFieldBuilderdIdentifierTest() throws Exception {
        // GIVEN
        definition.setIdentifier(true);
        definition.setName(propertyName);
        definition.setWorkspace(workspaceName);
        baseNode.setProperty(propertyName, baseNode.getIdentifier());
        baseItem = new JcrNodeAdapter(baseNode);
        when(componentProvider.newInstance(SingleValueHandler.class, baseItem, definition.getName())).thenReturn(new SingleValueHandler((JcrNodeAdapter) baseItem, propertyName));
        multiLinkFieldBuilder = new MultiLinkFieldBuilder(definition, baseItem, null, null, componentProvider);
        multiLinkFieldBuilder.setI18nContentSupport(i18nContentSupport);
        // WHEN
        Field field = multiLinkFieldBuilder.getField();

        // THEN
        assertEquals(true, field instanceof MultiLinkField);
        // Propert way set to the identifier baseNode.getIdentifier() and we display the path
        assertEquals(baseNode.getIdentifier(), ((List) field.getValue()).get(0));
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        MultiLinkFieldDefinition fieldDefinition = new MultiLinkFieldDefinition();
        fieldDefinition.setName(propertyName);
        fieldDefinition.setDialogName("dialogName");
        this.definition = fieldDefinition;
    }

}
