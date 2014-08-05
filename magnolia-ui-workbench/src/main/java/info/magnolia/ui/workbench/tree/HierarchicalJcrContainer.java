/**
 * This file Copyright (c) 2011-2014 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.NodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.workbench.container.AbstractJcrContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;

/**
 * Hierarchical implementation of {@link info.magnolia.ui.workbench.container.AbstractJcrContainer}.
 */
public class HierarchicalJcrContainer extends AbstractJcrContainer implements Container.Hierarchical {

    private static final Logger log = LoggerFactory.getLogger(HierarchicalJcrContainer.class);

    private static class ItemNameComparator implements Comparator<Item> {
        @Override
        public int compare(Item lhs, Item rhs) {
            try {
                return lhs.getName().compareTo(rhs.getName());
            } catch (RepositoryException e) {
                log.warn("Cannot compare item names: " + e);
                return 0;
            }
        }
    }

    public HierarchicalJcrContainer(JcrContentConnectorDefinition definition) {
        super(definition);
    }

    @Override
    public Collection<JcrItemId> getChildren(Object itemId) {
        long start = System.currentTimeMillis();
        Collection<Item> children = getChildren(getJcrItem(itemId));
        log.debug("Fetched {} children in {}ms", children.size(), System.currentTimeMillis() - start);
        return createContainerIds(children);
    }

    @Override
    public JcrItemId getParent(Object itemId) {
        try {
            Item item = getJcrItem(itemId);
            if (item.isNode() && item.getDepth() == 0) {
                return null;
            }
            return JcrItemUtil.getItemId(item.getParent());
        } catch (RepositoryException e) {
            handleRepositoryException(log, "Cannot determine parent for itemId: " + itemId, e);
            return null;
        }
    }

    @Override
    public Collection<JcrItemId> rootItemIds() {
        try {
            return createContainerIds(getRootItemIds());
        } catch (RepositoryException e) {
            handleRepositoryException(log, "Cannot retrieve root item id's", e);
            return Collections.emptySet();
        }
    }

    @Override
    public void refresh() {
        resetOffset();
        clearItemIndexes();
        fireItemSetChange();
    }

    @Override
    public boolean setParent(Object itemId, Object newParentId) throws UnsupportedOperationException {
        fireItemSetChange();
        return true;
    }

    @Override
    public boolean areChildrenAllowed(Object itemId) {
        final JcrItemAdapter item = ((JcrItemAdapter) getItem(itemId));
        if (item == null) {
            return false;
        }
        return item.isNode() && hasChildren(itemId);
    }

    @Override
    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRoot(Object itemId) {
        try {
            return isRoot(getJcrItem(itemId));
        } catch (RepositoryException e) {
            handleRepositoryException(log, "Cannot determine whether item is root - itemId: " + itemId, e);
            return true;
        }
    }

    @Override
    public boolean hasChildren(Object itemId) {
        final Item item = getJcrItem(itemId);
        return item.isNode() && !getChildren(item).isEmpty();
    }

    protected Collection<JcrItemId> createContainerIds(Collection<Item> children) {
        ArrayList<JcrItemId> ids = new ArrayList<JcrItemId>();
        for (Item child : children) {
            try {
                JcrItemId itemId = JcrItemUtil.getItemId(child);
                ids.add(itemId);
            } catch (RepositoryException e) {
                handleRepositoryException(log, "Cannot retrieve currentId", e);
            }
        }
        return ids;
    }

    @Override
    public List<String> getSortableContainerPropertyIds() {
        // at present tree view is not sortable
        return Collections.emptyList();
    }

    public Collection<Item> getChildren(Item item) {
        if (!item.isNode()) {
            return Collections.emptySet();
        }

        Node node = (Node) item;

        ArrayList<Item> items = new ArrayList<Item>();

        try {
            NodeIterator iterator = node.getNodes();
            while (iterator.hasNext()) {
                Node next = iterator.nextNode();
                if (isNodeVisible(next)) {
                    items.add(next);
                }
            }

            if (getConfiguration().isIncludeProperties()) {
                ArrayList<Property> properties = new ArrayList<Property>();
                PropertyIterator propertyIterator = node.getProperties();
                while (propertyIterator.hasNext()) {
                    final Property property = propertyIterator.nextProperty();
                    final String propertyName = property.getName();
                    if (!propertyName.startsWith(NodeTypes.JCR_PREFIX) && !propertyName.startsWith(NodeTypes.MGNL_PREFIX)) {
                        properties.add(property);
                    }
                }
                ItemNameComparator itemNameComparator = new ItemNameComparator();
                Collections.sort(properties, itemNameComparator);
                items.addAll(properties);
            }
        } catch (RepositoryException e) {
            handleRepositoryException(log, "Could not retrieve children", e);
        }

        return Collections.unmodifiableCollection(items);
    }

    protected boolean isNodeVisible(Node node) throws RepositoryException {

        if (!getConfiguration().isIncludeSystemNodes() && node.getName().startsWith("jcr:") || node.getName().startsWith("rep:")) {
            return false;
        }

        String primaryNodeTypeName = node.getPrimaryNodeType().getName();
        for (NodeTypeDefinition nodeTypeDefinition : getConfiguration().getNodeTypes()) {
            if (nodeTypeDefinition.isStrict()) {
                if (primaryNodeTypeName.equals(nodeTypeDefinition.getName())) {
                    return true;
                }
            } else if (NodeUtil.isNodeType(node, nodeTypeDefinition.getName())) {
                return true;
            }
        }
        return false;
    }

    public Collection<Item> getRootItemIds() throws RepositoryException {
        return getChildren(getRootNode());
    }

    /**
     * Checks if an item is a root. Since root node is never shown, we consider its child nodes and properties as roots
     * to remove unnecessary offset in trees.
     */
    public boolean isRoot(Item item) throws RepositoryException {
        if (item != null) {
            try {
                int rootDepth = getRootNode().getDepth();
                return item.getDepth() <= rootDepth + 1;
            } catch (RepositoryException e) {
                handleRepositoryException(log, "Cannot determine depth of jcr item", e);
            }
        }
        return true;
    }

    /**
     * Only used in tests.
     */
    String getPathInTree(Item item) throws RepositoryException {
        String base = getConfiguration().getRootPath();
        return "/".equals(base) ? item.getPath() : StringUtils.substringAfter(item.getPath(), base);
    }

    private Session getSession() throws RepositoryException {
        return MgnlContext.getJCRSession(getWorkspace());
    }

    private Node getRootNode() throws RepositoryException {
        return getSession().getNode(getConfiguration().getRootPath());
    }
}
