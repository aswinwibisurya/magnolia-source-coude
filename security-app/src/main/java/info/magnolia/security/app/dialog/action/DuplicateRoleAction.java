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
package info.magnolia.security.app.dialog.action;

import info.magnolia.event.EventBus;
import info.magnolia.security.app.util.UsersWorkspaceUtil;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.framework.action.DuplicateNodeAction;
import info.magnolia.ui.framework.action.DuplicateNodeActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * {@link DuplicateNodeAction} extension which also updates ACL's on execution.
 */
public class DuplicateRoleAction extends DuplicateNodeAction {

    private JcrItemId duplicateNodeId;

    public DuplicateRoleAction(DuplicateNodeActionDefinition definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus) {
        super(definition, item, eventBus);
    }

    @Override
    protected void onExecute(JcrItemAdapter item) throws RepositoryException {
        final String oldPath = item.getJcrItem().getPath();
        super.onExecute(item);
        if (duplicateNodeId != null) {
            final Node duplicateNode = (Node) JcrItemUtil.getJcrItem(duplicateNodeId);
            UsersWorkspaceUtil.updateAcls(duplicateNode, oldPath);
        }
    }

    @Override
    protected void setItemIdOfChangedItem(JcrItemId itemIdOfChangedItem) {
        super.setItemIdOfChangedItem(itemIdOfChangedItem);
        this.duplicateNodeId = itemIdOfChangedItem;
    }
}
