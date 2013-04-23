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
package info.magnolia.ui.form.field.builder;

import static org.junit.Assert.assertTrue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;
import info.magnolia.ui.form.field.upload.basic.BasicUploadField;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;

/**
 * Main testcase for {@link BasicUploadFieldBuilder}.
 */
public class BasicUploadFieldBuilderTest extends AbstractBuilderTest<BasicUploadFieldDefinition> {

    protected BasicUploadFieldBuilder basicUploadBuilder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        File directory = Files.createTempDir();
        directory.deleteOnExit();
        MagnoliaConfigurationProperties config = mock(MagnoliaConfigurationProperties.class);
        when(config.getProperty(SystemProperty.MAGNOLIA_UPLOAD_TMPDIR)).thenReturn(directory.getAbsolutePath());
        basicUploadBuilder = new BasicUploadFieldBuilder(definition, baseItem, config, new TestImageProvider());
        basicUploadBuilder.setI18nContentSupport(i18nContentSupport);
    }

    @Test
    public void testBasicUploadFieldBuilder() throws Exception {
        // GIVEN

        // WHEN
        Field field = basicUploadBuilder.getField();

        // THEN
        assertEquals(true, field instanceof BasicUploadField);
        assertEquals(0, ((AbstractJcrNodeAdapter) baseItem).getChildren().size());
    }

    @Test
    public void testBuildEmptyLayout() throws Exception {
        // GIVEN
        BasicUploadField field = (BasicUploadField) basicUploadBuilder.getField();
        Upload upload = new Upload();
        FailedEvent event = new FailedEvent(upload, "filename", "MIMEType", 0l);

        // WHEN
        field.uploadFinished(event);

        // THEN
        CssLayout layout = field.getCssLayout();
        assertEquals(2, layout.getComponentCount());
        assertTrue(layout.getComponent(0) instanceof Upload);
        assertTrue(layout.getComponent(1) instanceof Label);
        assertTrue(((Label) layout.getComponent(1)).getStyleName().contains("upload-text"));
    }

    @Test
    public void testBuildCompletedLayout() throws Exception {
        // GIVEN
        BasicUploadField field = (BasicUploadField) basicUploadBuilder.getField();
        Upload upload = new Upload();
        FinishedEvent event = new FinishedEvent(upload, "filename", "MIMEType", 0l);

        // WHEN
        field.uploadFinished(event);

        // THEN
        CssLayout layout = field.getCssLayout();
        assertEquals(3, layout.getComponentCount());
        assertTrue(layout.getComponent(0) instanceof Label);
        assertTrue(((Label) layout.getComponent(0)).getStyleName().contains("file-details"));
        assertTrue(layout.getComponent(1) instanceof HorizontalLayout);
        HorizontalLayout horizontalLayout = (HorizontalLayout) layout.getComponent(1);
        assertEquals(1, horizontalLayout.getComponentCount());
        assertTrue(horizontalLayout.getComponent(0) instanceof Upload);
        assertTrue(layout.getComponent(2) instanceof Label);
        assertTrue(((Label) layout.getComponent(2)).getStyleName().contains("preview-image"));
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        BasicUploadFieldDefinition fieldDefinition = new BasicUploadFieldDefinition();
        fieldDefinition.setName(propertyName);
        this.definition = fieldDefinition;
    }

    private class TestImageProvider implements ImageProvider {

        @Override
        public String getPortraitPath(String workspace, String path) {
            return null;
        }

        @Override
        public String getThumbnailPath(String workspace, String path) {
            return null;
        }

        @Override
        public String getPortraitPathByIdentifier(String workspace, String uuid) {
            return null;
        }

        @Override
        public String getThumbnailPathByIdentifier(String workspace, String uuid) {
            return null;
        }

        @Override
        public String resolveIconClassName(String mimeType) {
            return "none";
        }

        @Override
        public Object getThumbnailResourceByPath(String workspace, String path, String generator) {
            return null;
        }

        @Override
        public Object getThumbnailResourceById(String workspace, String identifier, String generator) {
            return null;
        }

    }
}
