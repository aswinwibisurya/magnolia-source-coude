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
package info.magnolia.ui.api.availability;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link IsDeletedRule}.
 */
public class IsDeletedRuleTest extends RepositoryTestCase {

    private IsDeletedRule rule;
    private Session webSiteSession;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        rule = new IsDeletedRule();
    }

    @Test
    public void testIsAvailableForItemThatIsNotDeleted() throws RepositoryException {
        // GIVEN
        Node node = webSiteSession.getRootNode().addNode("node1", NodeTypes.Page.NAME);

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(node);

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void testIsAvailableForItemThatIsMarkedAsDeleted() throws RepositoryException {
        // GIVEN
        Node node = webSiteSession.getRootNode().addNode("node2", NodeTypes.Page.NAME);
        node.addMixin(NodeTypes.Deleted.NAME);

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(node);

        // THEN
        assertTrue(isAvailable);
    }

    @Test
    public void testIsAvailableForItemThatIsDeleted() throws RepositoryException {
        // GIVEN
        Node node = webSiteSession.getRootNode().addNode("node3", NodeTypes.Page.NAME);
        node.remove();

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(node);

        // THEN
        assertFalse(isAvailable);
    }

}
