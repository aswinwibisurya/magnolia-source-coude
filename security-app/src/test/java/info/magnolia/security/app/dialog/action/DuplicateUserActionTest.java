/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.security.app.dialog.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DuplicateUserAction}.
 */
public class DuplicateUserActionTest extends RepositoryTestCase{

    private SecuritySupport securitySupport;
    private Session session;
    private MgnlUserManager userManager;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        userManager = new MgnlUserManager();
        securitySupport = mock(SecuritySupport.class);
        when(securitySupport.getUserManager()).thenReturn(userManager);
        ComponentsTestUtil.setInstance(SecuritySupport.class, securitySupport);

        session = MgnlContext.getJCRSession(RepositoryConstants.USERS);
    }

    @Test
    public void testUserALCPropertyPathChangeWhileDuplicateNode() throws Exception {
        // GIVEN
        Node adminNode = session.getRootNode().addNode("admin");
        User user = userManager.createUser(adminNode.getPath(),"testUser","testPw");

        // WHEN
        final JcrNodeAdapter userItem = new JcrNodeAdapter(adminNode.getNode(user.getName()));
        final DuplicateUserAction action = new DuplicateUserAction(
                new DuplicateRoleActionDefinition(), userItem, mock(EventBus.class));
        action.execute();

        // THEN
        assertEquals(adminNode.getNode(user.getName()+"0"+"/acl_users/0").getProperty("path").getString(), adminNode.getNode(user.getName()+"0").getPath());
    }
}
