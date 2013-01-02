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
package info.magnolia.ui.app.contacts.field;

import info.magnolia.ui.admincentral.field.ThumbnailField;
import info.magnolia.ui.model.imageprovider.definition.ImageProvider;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Specific Contact Thumbnail field that override the createFieldDetail(String nodePath)
 * of {@link ThumbnailField}.
 */
public class ContactThumbnailField extends ThumbnailField {

    public ContactThumbnailField(ImageProvider imageThumbnailProvider, String workspace) {
        super(imageThumbnailProvider, workspace);
    }

    @Override
    public String createFieldDetail(Node parentNode) throws RepositoryException {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul><li><span class=\"left\">Last name: </span><span class=\"center\">" + parentNode.getProperty("lastName").getString() + "</span></li>");
        sb.append("<li><span class=\"left\">First name: </span><span class=\"center\">" + parentNode.getProperty("firstName").getString() + "</span></li>");
        sb.append("<li><span class=\"left\">Organization: </span><span class=\"center\">" + parentNode.getProperty("organizationName").getString() + "</span></li>");
        sb.append("<li><span class=\"left\">Email: </span><span class=\"center\">" + parentNode.getProperty("email").getString() + "</span></li></ul>");

        return sb.toString();
    }
}
