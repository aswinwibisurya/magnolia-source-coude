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
package info.magnolia.ui.admincentral.setup;

import static org.junit.Assert.*;

import info.magnolia.cms.util.UnicodeNormalizer;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.reader.DependencyChecker;
import info.magnolia.repository.DefaultRepositoryManager;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.test.ComponentsTestUtil;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class.
 */
public class AdmincentralModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private Node dialog;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/ui-admincentral.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new AdmincentralModuleVersionHandler();
    }

    @Override
    protected void initDefaultImplementations() throws IOException {
        ComponentsTestUtil.setInstance(ModuleRegistry.class, new ModuleRegistryImpl());
        ComponentsTestUtil.setImplementation(UnicodeNormalizer.Normalizer.class, "info.magnolia.cms.util.UnicodeNormalizer$NonNormalizer");
        ComponentsTestUtil.setInstance(RepositoryManager.class, new DefaultRepositoryManager());
        // provide our own ModuleManager implementation that is able to read the required module
        // definitions e.g. from surefire classpath
        ComponentsTestUtil.setImplementation(DependencyChecker.class, NullDependencyChecker.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        dialog = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/dialogs", NodeTypes.ContentNode.NAME);
        dialog.getSession().save();

        // ComponentsTestUtil.setImplementation(DependencyChecker.class, NullDependencyChecker.)
    }

    @Test
    public void testUpdateTo5_0_1WithoutExistingLinkDefinition() throws ModuleManagementException, RepositoryException {
        // GIVEN
        dialog.addNode("link", NodeTypes.ContentNode.NAME);
        assertTrue(dialog.hasNode("link"));
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(dialog.hasNode("link"));

    }

    @Test
    public void testUpdateTo5_0_1WithoutNonExistingLinkDefinition() throws ModuleManagementException, RepositoryException {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(dialog.hasNode("link"));
    }
}
