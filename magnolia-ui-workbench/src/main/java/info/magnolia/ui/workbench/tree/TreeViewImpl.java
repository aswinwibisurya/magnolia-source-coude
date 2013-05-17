/**
 * This file Copyright (c) 2011-2013 Magnolia International
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
package info.magnolia.ui.workbench.tree;

import info.magnolia.ui.workbench.event.ItemEditedEvent;
import info.magnolia.ui.workbench.list.ListViewImpl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.dd.DropHandler;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TreeTable;

/**
 * Vaadin UI component that displays a tree.
 */
public class TreeViewImpl extends ListViewImpl implements TreeView {

    private static final Logger log = LoggerFactory.getLogger(TreeViewImpl.class);

    private final TreeTable treeTable;

    private final ItemEditedEvent.Handler itemEditedListener = new ItemEditedEvent.Handler() {

        @Override
        public void onItemEdited(ItemEditedEvent event) {
            if (getListener() != null) {
                getListener().onItemEdited(event.getItem());
            }
        }
    };

    public TreeViewImpl() {
        this(new InplaceEditingTreeTable());
    }

    public TreeViewImpl(TreeTable tree) {
        super(tree);
        tree.setSortEnabled(false);

        tree.setCollapsed(tree.firstItemId(), false);

        this.treeTable = tree;
    }

    @Override
    public void setEditable(boolean editable) {
        treeTable.setEditable(editable);
        if (editable && treeTable instanceof InplaceEditingTreeTable) {
            ((InplaceEditingTreeTable) treeTable).addItemEditedListener(itemEditedListener);
        } else {
            ((InplaceEditingTreeTable) treeTable).removeItemEditedListener(itemEditedListener);
        }
    }

    @Override
    public void setEditableColumns(Object... propertyIds) {
        ((InplaceEditingTreeTable) treeTable).setEditableColumns(propertyIds);
    }

    @Override
    public void setDragAndDropHandler(DropHandler dropHandler) {
        if (dropHandler != null) {
            treeTable.setDragMode(TableDragMode.ROW);
            treeTable.setDropHandler(dropHandler);
        } else {
            treeTable.setDragMode(TableDragMode.NONE);
            treeTable.setDropHandler(null);
        }
    }

    @Override
    public void select(String itemId) {
        if (!treeTable.isRoot(itemId)) {
            // properties cannot be expanded so use the nodeId only
            expandTreeToNode(itemId);
            treeTable.setCurrentPageFirstItemId(itemId);
        }
        treeTable.setValue(null);
        treeTable.select(itemId);
    }

    private void expandTreeToNode(String nodeId) {
        final List<String> uuidsToExpand = new ArrayList<String>();
        uuidsToExpand.add(0, nodeId);

        final HierarchicalJcrContainer container = (HierarchicalJcrContainer) treeTable.getContainerDataSource();
        try {
            Node parent = container.getJcrItem(nodeId).getParent();
            while (parent != null) {
                uuidsToExpand.add(0, parent.getIdentifier());
                parent = parent.getParent();
                if (StringUtils.equals(parent.getPath(), container.getWorkbenchDefinition().getPath()) || !parent.getPath().contains(container.getWorkbenchDefinition().getPath())) {
                    // parent is outside the scope of the workbench so ignore it
                    parent = null;
                }

            }
        } catch (RepositoryException e) {
            log.warn("Could not collect the parent hierarchy of node {}", nodeId, e);
        }
        for (String uuid : uuidsToExpand) {
            treeTable.setCollapsed(uuid, false);
        }
    }

    @Override
    protected TreeView.Listener getListener() {
        return (TreeView.Listener) super.getListener();
    }

    @Override
    public ViewType getViewType() {
        return ViewType.TREE;
    }

    @Override
    public TreeTable asVaadinComponent() {
        return treeTable;
    }

}
