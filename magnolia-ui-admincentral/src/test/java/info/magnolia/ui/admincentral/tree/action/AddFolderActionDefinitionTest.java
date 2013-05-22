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
package info.magnolia.ui.admincentral.tree.action;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import info.magnolia.cms.security.DummyUser;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.RecordingEventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.action.ActionAvailabilityDefinition;
import info.magnolia.ui.api.action.ConfiguredActionAvailabilityDefinition;
import info.magnolia.ui.framework.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for AddFolderAction.
 */
public class AddFolderActionDefinitionTest extends MgnlTestCase {

    private final static String WORKSPACE = "workspace";

    private final static String NODE_NAME = "johnNode";

    private static AddFolderActionDefinition definition;

    private RecordingEventBus eventBus;

    private MockSession session;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(ActionAvailabilityDefinition.class, ConfiguredActionAvailabilityDefinition.class);
        definition = new AddFolderActionDefinition();
        session = new MockSession(WORKSPACE);
        MockContext ctx = new MockContext();
        ctx.addSession(WORKSPACE, session);
        ctx.setUser(new DummyUser());
        MgnlContext.setInstance(ctx);

        eventBus = new RecordingEventBus();
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testCanAddChildNode() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode(NODE_NAME);
        long nodeCount = node.getNodes().getSize();
        AddNodeAction action = new AddNodeAction(definition, new JcrNodeAdapter(node), eventBus);

        // WHEN
        action.execute();

        // THEN
        assertTrue(node.hasNode(RepositoryOperationAction.DEFAULT_NEW_ITEM_NAME));
        assertEquals(nodeCount + 1, node.getNodes().getSize());
        Node newNode = node.getNode(RepositoryOperationAction.DEFAULT_NEW_ITEM_NAME);
        assertEquals(NodeTypes.Folder.NAME, newNode.getPrimaryNodeType().getName());
        assertTrue(newNode.hasProperty(NodeTypes.Created.CREATED));
        assertTrue(newNode.hasProperty(NodeTypes.Created.CREATED_BY));
        assertTrue(newNode.hasProperty(NodeTypes.LastModified.LAST_MODIFIED));
        assertTrue(newNode.hasProperty(NodeTypes.LastModified.LAST_MODIFIED_BY));
        assertFalse(eventBus.isEmpty());
        assertTrue(((ContentChangedEvent) eventBus.getEvent()).getItemId().equals(JcrItemUtil.getItemId(newNode)));
    }
}
