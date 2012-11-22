/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.model.thumbnail;

import com.vaadin.terminal.Resource;


/**
 * Defines a provider for Thumbnail images.
 */
public interface ImageProvider {

    static final String ORIGINAL_IMAGE_NODE_NAME = "originalImage";
    static final String IMAGING_SERVLET_PATH = ".imaging";
    static final String IMAGE_EXTENSION = "png";
    static final String PORTRAIT_GENERATOR = "portrait";
    static final String THUMBNAIL_GENERATOR = "thumbnail";

    String getLargePath(String workspace, String path);

    String getPortraitPath(String workspace, String path);

    String getThumbnailPath(String workspace, String path);

    String getLargePathByIdentifier(String workspace, String uuid);

    String getPortraitPathByIdentifier(String workspace, String uuid);

    String getThumbnailPathByIdentifier(String workspace, String uuid);

    /**
     * Defaults to {@value #ORIGINAL_IMAGE_NODE_NAME}.
     */
    String getOriginalImageNodeName();

    void setOriginalImageNodeName(String originalImageNodeName);

    /**
     * Defaults to {@value #IMAGING_SERVLET_PATH}.
     */
    String getImagingServletPath();

    void setImagingServletPath(String imagingServletPath);

    /**
     * Defaults to {@value #IMAGE_EXTENSION}.
     */
    String getImageExtension();

    void setImageExtension(String imageExtension);

    /**
     * Get a Preview Resource.
     * This preview is an image or an icon representing the Document type.
     */
    Resource getThumbnailResourceByPath(String workspace, String path, String generator);

    Resource getThumbnailResourceById(String workspace, String identifier, String generator);
}
