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
package info.magnolia.ui.framework.availability;

import static org.junit.Assert.*;

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link IsVersionRule}.
 */
public class IsVersionRuleTest extends RepositoryTestCase {

    private VersionManager versionManager;
    private IsVersionRule rule;
    private Session webSiteSession;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        versionManager = VersionManager.getInstance();
        rule = new IsVersionRule();
    }

    @Test
    public void testIsAvailableForItemNoVersion() throws RepositoryException {
        // GIVEN
        Node node = webSiteSession.getRootNode().addNode("nodeWithNoVersion", NodeTypes.Page.NAME);

        // WHEN
        Object itemId = JcrItemUtil.getItemId(node);
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertFalse(isAvailable);
    }

    @Test
    public void testIsAvailableForItemVersion() throws RepositoryException {
        // GIVEN
        Node node = webSiteSession.getRootNode().addNode("nodeWithNoVersion", NodeTypes.Page.NAME);
        Version version = versionManager.addVersion(node);

        // WHEN
        Object itemId = JcrItemUtil.getItemId(version);
        boolean isAvailable = rule.isAvailableForItem(itemId);

        // THEN
        assertTrue(isAvailable);
    }

    @Test
    public void testIsAvailableForNullNode() throws RepositoryException {
        // GIVEN

        // WHEN
        boolean isAvailable = rule.isAvailableForItem(null);

        // THEN
        assertFalse(isAvailable);
    }

}
