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
package info.magnolia.about.app;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.init.MagnoliaConfigurationProperties;

import java.io.File;
import java.net.URL;

import org.junit.Test;

/**
 * Testing ability of the presenter to obtain connection info.
 */
public class AboutPresenterTest {

    @Test
    public void testRepoName() {
        // GIVEN
        MagnoliaConfigurationProperties properties = mock(MagnoliaConfigurationProperties.class);
        when(properties.getProperty("magnolia.repositories.config")).thenReturn("repositories.xml");
        when(properties.getProperty("magnolia.app.rootdir")).thenReturn(new File("target/test-classes").getAbsolutePath());
        AboutPresenter presenter = new AboutPresenter(mock(AboutView.class), mock(ServerConfiguration.class), properties, mock(SimpleTranslator.class));

        // WHEN
        String repoName = presenter.getRepoName();

        // THEN
        assertEquals("magnolia", repoName);
    }

    @Test
    public void testConnectionWithAbsolutePathForConfFile() {
        // GIVEN
        MagnoliaConfigurationProperties properties = mock(MagnoliaConfigurationProperties.class);
        // AboutPresenter expects a an absolute path (when it's not starting with WEB-INF)
        String configFilePathAbsPath = getAbsPathOfTestResource("jackrabbit-bundle-derby-search.xml");
        when(properties.getProperty("magnolia.repositories.jackrabbit.config")).thenReturn(configFilePathAbsPath);
        AboutPresenter presenter = new AboutPresenter(mock(AboutView.class), mock(ServerConfiguration.class), properties, mock(SimpleTranslator.class));

        // WHEN
        String[] connection = presenter.getConnectionString();

        // THEN
        assertTrue(connection.length > 0);
        assertEquals("jdbc:derby:${rep.home}/version/db;create=true", connection[0]);
    }

    @Test
    public void testConnectionWithRelativePathForConfFile() {
        // GIVEN
        MagnoliaConfigurationProperties properties = mock(MagnoliaConfigurationProperties.class);
        // AboutPresenter expects a relative path starting with "WEB-INF" (or an absolute path)
        String configFileRelPath = "WEB-INF/jackrabbit-bundle-derby-search.xml";
        when(properties.getProperty("magnolia.repositories.jackrabbit.config")).thenReturn(configFileRelPath);
        String fakedMagnoliaAppRootDir = getAbsPathOfTestResource(".");
        when(properties.getProperty("magnolia.app.rootdir")).thenReturn(fakedMagnoliaAppRootDir);
        AboutPresenter presenter = new AboutPresenter(mock(AboutView.class), mock(ServerConfiguration.class), properties, mock(SimpleTranslator.class));

        // WHEN
        String[] connection = presenter.getConnectionString();

        // THEN
        assertTrue(connection.length > 0);
        assertEquals("jdbc:derby:${rep.home}/version/db;create=true", connection[0]);
    }

    private String getAbsPathOfTestResource(String relPathInResources) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(relPathInResources);
        return url.getPath();
    }

}
