/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.ui.app.contacts.thumbnail;


import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.link.LinkException;
import info.magnolia.link.LinkUtil;
import info.magnolia.ui.admincentral.thumbnail.ThumbnailProvider;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.swing.ImageIcon;

import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Thumbnail provider operating on contacts.
 * Knows how to navigate a contact's jcr structure.
 */
public abstract class AbstractContactsThumbnailProvider implements ThumbnailProvider {
    private static final Logger log = LoggerFactory.getLogger(AbstractContactsThumbnailProvider.class);

    final static String PHOTO_NODE_NAME = "photo";
    final static String THUMBNAIL_NODE_NAME = "thumbnail";

    private String format;

    private float quality;

    @Override
    public String getPath(Node contactNode, int width, int height) {

        String path = null;
        try {
            if (createThumbnail(contactNode)) {

                Node photoNode = contactNode.getNode(PHOTO_NODE_NAME);
                final InputStream stream = photoNode.getProperty(JcrConstants.JCR_DATA).getBinary().getStream();

                BufferedImage thumbnail = null;
                try {
                    byte[] array = new byte[stream.available()];
                    stream.read(array);
                    stream.close();

                    Image contactImage = Toolkit.getDefaultToolkit().createImage(array);
                    contactImage = new ImageIcon(contactImage).getImage();
                    thumbnail = createThumbnail(contactImage, getFormat(), width, height, getQuality());

                    if(contactNode.hasNode(THUMBNAIL_NODE_NAME)) {
                        contactNode.getNode(THUMBNAIL_NODE_NAME).remove();
                    }
                    final Node thumbnailNode = contactNode.addNode(THUMBNAIL_NODE_NAME, MgnlNodeType.NT_RESOURCE);
                    thumbnailNode.setProperty(FileProperties.PROPERTY_FILENAME, photoNode.getProperty(FileProperties.PROPERTY_FILENAME).getString());
                    thumbnailNode.setProperty(FileProperties.PROPERTY_EXTENSION, getFormat());
                    thumbnailNode.setProperty(FileProperties.PROPERTY_MIMETYPE, photoNode.getProperty(JcrConstants.JCR_MIMETYPE).getString());
                    thumbnailNode.setProperty(FileProperties.PROPERTY_HEIGHT,  thumbnail.getHeight());
                    thumbnailNode.setProperty(FileProperties.PROPERTY_WIDTH,  thumbnail.getWidth());

                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(thumbnail, getFormat(), baos);
                    final int size = baos.size();
                    log.debug("thumbnail size is {} ", size);

                    final InputStream is = new ByteArrayInputStream(baos.toByteArray());
                    thumbnailNode.setProperty(JcrConstants.JCR_DATA, is);
                    thumbnailNode.setProperty(FileProperties.PROPERTY_SIZE, size);

                    contactNode.getSession().save();

                } catch (IOException e) {
                    log.warn("Error creating thumbnail image!", e);
                    return path;
                }
            }
        } catch (RepositoryException e) {
            log.warn("Could read image from contactNode:", e);
            return path;
        }

        try {
            path = LinkUtil.createLink(ContentUtil.asContent(contactNode).getNodeData(THUMBNAIL_NODE_NAME));
        } catch (LinkException e) {
            log.warn("Error creating Link", e);
        }

        return path;
    }

    private boolean createThumbnail(Node contactNode) throws RepositoryException {
        if(!contactNode.hasNode(THUMBNAIL_NODE_NAME)) {
            return true;
        }
        if(!contactNode.hasNode(PHOTO_NODE_NAME)) {
            log.warn("No [{}] node found for contact node [{}]. Cannot create thumbnail.", PHOTO_NODE_NAME, contactNode.getPath());
            return false;
        }
        final Node photoNode = contactNode.getNode(PHOTO_NODE_NAME);
        final Node thumbnailNode = contactNode.getNode(THUMBNAIL_NODE_NAME);
        if(photoNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate().compareTo(thumbnailNode.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate()) > 0) {
            log.info("Recreating thumbnail for node [{}]", contactNode.getPath());
            //photo node must have been updated as its last mod date is after thumbanail last mod date
            return true;
        }
        return false;
    }

    protected abstract BufferedImage createThumbnail(final Image contactImage, final String format, final int width, final int height, final float quality) throws IOException;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public float getQuality() {
        return quality;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }
}
