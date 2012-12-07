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
package info.magnolia.ui.vaadin.integration.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;


public class AbstractJcrNodeAdapterTest {

    private final String workspaceName = "workspace";

    private MockSession session;

    @Before
    public void setUp() {
        session = new MockSession(workspaceName);
        MockContext ctx = new MockContext();
        ctx.addSession(workspaceName, session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testSetCommonAttributes() throws Exception {
        // GIVEN
        String nodeName = "nodeName";
        Node testNode = session.getRootNode().addNode(nodeName);

        // WHEN
        DummyJcrNodeAdapter adapter = new DummyJcrNodeAdapter(testNode);

        // THEN
        assertEquals(testNode.getIdentifier(), adapter.getNodeIdentifier());
        assertEquals(testNode.getIdentifier(), ((Node) adapter.getJcrItem()).getIdentifier());
        assertEquals(testNode.getPrimaryNodeType().getName(), adapter.getPrimaryNodeTypeName());
    }

    // assertEquals(testNode.getIdentifier(), adapter.getNodeIdentifier());

    @Test
    public void testGetItemProperty_ExistingProperty() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final String propertyValue = "value";
        underlyingNode.setProperty(propertyName, propertyValue);
        final DummyJcrNodeAdapter item = new DummyJcrNodeAdapter(underlyingNode);

        // WHEN
        final Property prop = item.getItemProperty(propertyName);

        // THEN
        assertEquals(propertyValue, prop.getValue());

    }

    @Test
    public void testGetItemProperty_NewProperty() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final String propertyValue = "value";
        underlyingNode.setProperty(propertyName, propertyValue);
        final DummyJcrNodeAdapter item = new DummyJcrNodeAdapter(underlyingNode);

        // WHEN
        final Property prop = item.getItemProperty(propertyName + "_1");

        // THEN
        assertEquals(true, prop == null);
    }

    @Test
    public void testGetItemProperty_NewProperty_Add() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final String propertyValue = "value";
        final DummyJcrNodeAdapter item = new DummyJcrNodeAdapter(underlyingNode);
        Property property = DefaultPropertyUtil.newDefaultProperty(propertyName, PropertyType.TYPENAME_STRING, propertyValue);
        item.addItemProperty(propertyName, property);

        // WHEN
        final Property prop = item.getItemProperty(propertyName);

        // THEN
        assertEquals(propertyValue, prop.getValue().toString());
    }

    @Test
    public void testValueChangeEvent_PropertyExist() throws Exception {
        // GIVEN
        Node underlyingNode = session.getRootNode().addNode("underlying");
        String propertyName = "TEST";
        String propertyValue = "value";
        javax.jcr.Property jcrProperty = underlyingNode.setProperty(propertyName, propertyValue);
        DummyJcrNodeAdapter item = new DummyJcrNodeAdapter(underlyingNode);

        // WHEN
        Property nodeProperty = item.getItemProperty(propertyName);
        nodeProperty.setValue("newValue");

        // THEN
        assertFalse(item.getChangedProperties().isEmpty());
        assertTrue(item.getChangedProperties().containsKey(propertyName));
        assertEquals(nodeProperty, item.getChangedProperties().get(propertyName));
        assertEquals("newValue", item.getChangedProperties().get(propertyName).getValue());
    }

    @Test
    public void testValueChangeEvent_PropertyDoNotExist() throws Exception {
        // GIVEN
        Node underlyingNode = session.getRootNode().addNode("underlying");
        String propertyName = "TEST";
        DummyJcrNodeAdapter item = new DummyJcrNodeAdapter(underlyingNode);

        // WHEN
        Property itemProperty = DefaultPropertyUtil.newDefaultProperty(propertyName, PropertyType.TYPENAME_STRING, propertyName);
        item.addItemProperty(propertyName, itemProperty);
        itemProperty.setValue("newValue");

        // THEN
        assertFalse(item.getChangedProperties().isEmpty());
        assertTrue(item.getChangedProperties().containsKey(propertyName));
        assertEquals(itemProperty, item.getChangedProperties().get(propertyName));
        assertEquals("newValue", item.getChangedProperties().get(propertyName).getValue());
    }

    @Test
    public void testUpdateProperties() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final String propertyValue = "value";
        final DummyJcrNodeAdapter item = new DummyJcrNodeAdapter(underlyingNode);
        Property property = DefaultPropertyUtil.newDefaultProperty(propertyName, PropertyType.TYPENAME_STRING, propertyValue);
        item.getChangedProperties().put(propertyName, property);

        // WHEN
        item.updateProperties(underlyingNode);

        // THEN
        assertEquals(propertyValue, underlyingNode.getProperty(propertyName).getString());
    }

    /**
     * Dummy implementation of the Abstract class.
     */
    public class DummyJcrNodeAdapter extends AbstractJcrNodeAdapter {

        public DummyJcrNodeAdapter(Node jcrNode) {
            super(jcrNode);
        }

        @Override
        public Collection< ? > getItemPropertyIds() {
            return null;
        }

        @Override
        public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
            return false;
        }

    }

}
