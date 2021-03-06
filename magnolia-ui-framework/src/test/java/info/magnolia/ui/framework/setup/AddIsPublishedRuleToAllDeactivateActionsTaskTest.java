/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.framework.setup;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link AddIsPublishedRuleToAllDeactivateActionsTask}.
 */
public class AddIsPublishedRuleToAllDeactivateActionsTaskTest extends RepositoryTestCase {

    private AddIsPublishedRuleToAllDeactivateActionsTask queryTask;
    private InstallContextImpl installContext;
    private Node appRootNode;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        final ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        installContext = new InstallContextImpl(moduleRegistry);
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        appRootNode = session.getRootNode().addNode("appRootNode");
        queryTask = new AddIsPublishedRuleToAllDeactivateActionsTask("description", appRootNode.getPath());
    }

    @Test
    public void testDeactivateNodeWithoutActionsParren() throws TaskExecutionException, RepositoryException {
        // GIVEN
        Node node = appRootNode.addNode("notActions").addNode("deactivate");

        // WHEN
        queryTask.execute(installContext);

        // THEN
        assertFalse(node.hasNode("availability"));
    }

    @Test
    public void testDeactivateNodeWithoutAvailailitySubnode() throws TaskExecutionException, RepositoryException {
        // GIVEN
        Node node = appRootNode.addNode("actions").addNode("deactivate");

        // WHEN
        queryTask.execute(installContext);

        // THEN
        assertTrue(node.getNode("availability").getNode("rules").getNode("IsPublishedRule").getProperty("implementationClass").getValue().getString().equals("info.magnolia.ui.framework.availability.IsPublishedRule"));
    }

    @Test
    public void testDeactivateNodeWithAvailabilitySubnodeWithoutRulesSubnode() throws TaskExecutionException, RepositoryException {
        // GIVEN
        Node node = appRootNode.addNode("actions").addNode("deactivate");
        node.addNode("availability");

        // WHEN
        queryTask.execute(installContext);

        // THEN
        assertTrue(node.getNode("availability").getNode("rules").getNode("IsPublishedRule").getProperty("implementationClass").getValue().getString().equals("info.magnolia.ui.framework.availability.IsPublishedRule"));
    }

    @Test
    public void testDeactivateNodeWithAvailabilitySubnodeWithRulesSubnode() throws TaskExecutionException, RepositoryException {
        // GIVEN
        Node node = appRootNode.addNode("actions").addNode("deactivate");
        node.addNode("availability").addNode("rules");

        // WHEN
        queryTask.execute(installContext);

        // THEN
        assertTrue(node.getNode("availability").getNode("rules").getNode("IsPublishedRule").getProperty("implementationClass").getValue().getString().equals("info.magnolia.ui.framework.availability.IsPublishedRule"));
    }

    @Test
    public void testDeactivateNodeExtendingAnotherDeactivate() throws TaskExecutionException, RepositoryException {
        // GIVEN
        Node node = appRootNode.addNode("actions").addNode("deactivate");
        node.setProperty("extends", "../deactivate");

        // WHEN
        queryTask.execute(installContext);

        // THEN
        assertFalse(node.hasNode("availability"));
    }
}
