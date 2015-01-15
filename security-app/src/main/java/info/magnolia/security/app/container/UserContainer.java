/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.security.app.container;

import info.magnolia.security.app.util.UsersWorkspaceUtil;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.tree.HierarchicalJcrContainer;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for the users workspace that updates ACLs in user nodes as users or the folders they're contained in are
 * moved.
 */
public class UserContainer extends HierarchicalJcrContainer {

    private static final Logger log = LoggerFactory.getLogger(UserContainer.class);

    public UserContainer(WorkbenchDefinition workbenchDefinition) {
        super(workbenchDefinition);
    }

    @Override
    public boolean moveItem(Item source, Item target) {
        try {
            String pathBefore = source.getPath();
            if (super.moveItem(source, target)) {
                if (source.isNode()) {
                    UsersWorkspaceUtil.updateAcls((Node) source, pathBefore);
                    source.getSession().save();
                }
                return true;
            }
        } catch (RepositoryException e) {
            log.error("Error while moving node", e);
        }
        return false;
    }

    @Override
    public boolean moveItemBefore(Item source, Item target) {
        try {
            String pathBefore = source.getPath();
            if (super.moveItemBefore(source, target)) {
                if (source.isNode()) {
                    UsersWorkspaceUtil.updateAcls((Node) source, pathBefore);
                    source.getSession().save();
                }
                return true;
            }
        } catch (RepositoryException e) {
            log.error("Error while moving node", e);
        }
        return false;
    }

    @Override
    public boolean moveItemAfter(Item source, Item target) {
        try {
            String pathBefore = source.getPath();
            if (super.moveItemAfter(source, target)) {
                if (source.isNode()) {
                    UsersWorkspaceUtil.updateAcls((Node) source, pathBefore);
                    source.getSession().save();
                }
                return true;
            }
        } catch (RepositoryException e) {
            log.error("Error while moving node", e);
        }
        return false;
    }
}
