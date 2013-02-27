/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.workbench.container;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.model.ModelConstants;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.definition.NodeTypeDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.ContainerHelpers;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Vaadin container that reads its items from a JCR repository. Implements a simple mechanism for lazy loading items
 * from a JCR repository and a cache for items and item ids.
 */
public abstract class AbstractJcrContainer extends AbstractContainer implements Container.Sortable, Container.Indexed, Container.ItemSetChangeNotifier {

    private static final Logger log = LoggerFactory.getLogger(AbstractJcrContainer.class);

    public static final int DEFAULT_PAGE_LENGTH = 30;

    public static final int DEFAULT_CACHE_RATIO = 2;

    private static final Long LONG_ZERO = Long.valueOf(0);

    /**
     * Node type to use if none is configured.
     */
    public static final String DEFAULT_NODE_TYPE = NodeTypes.Content.NAME;

    private static final String QUERY_LANGUAGE = Query.JCR_JQOM;

    protected static final String SELECTOR_NAME = "t";

    protected static final String SELECT_TEMPLATE = "select * from [%s] as " + SELECTOR_NAME;

    protected static final String WHERE_TEMPLATE_FOR_PATH = " ISDESCENDANTNODE('%s')";

    protected static final String ORDER_BY = " order by ";

    protected static final String ASCENDING_KEYWORD = " asc";

    protected static final String DESCENDING_KEYWORD = " desc";

    protected static final String JCR_NAME_FUNCTION = "lower(name(" + SELECTOR_NAME + "))";

    /**
     * Item and index caches.
     */
    private final Map<Long, String> itemIndexes = new HashMap<Long, String>();

    private final List<String> sortableProperties = new ArrayList<String>();

    private final List<OrderBy> sorters = new ArrayList<OrderBy>();

    private final WorkbenchDefinition workbenchDefinition;

    private int size = Integer.MIN_VALUE;

    /**
     * Page length = number of items contained in one page.
     */
    private int pageLength = DEFAULT_PAGE_LENGTH;

    /**
     * Number of items to cache = cacheRatio x pageLength.
     */
    private int cacheRatio = DEFAULT_CACHE_RATIO;

    private Set<ItemSetChangeListener> itemSetChangeListeners;

    /**
     * Starting row number of the currently fetched page.
     */
    private int currentOffset;


    public AbstractJcrContainer(WorkbenchDefinition workbenchDefinition) {
        this.workbenchDefinition = workbenchDefinition;

        for (ColumnDefinition columnDefinition : workbenchDefinition.getColumns()) {
            if (columnDefinition.isSortable()) {
                log.debug("Configuring column [{}] as sortable", columnDefinition.getName());

                String propertyName = columnDefinition.getPropertyName();
                log.debug("propertyName is {}", propertyName);

                if (StringUtils.isBlank(propertyName)) {
                    propertyName = columnDefinition.getName();
                    log.debug("Column {} is sortable but no propertyName has been defined. Defaulting to column name (sorting may not work as expected).", columnDefinition.getName());
                }

                sortableProperties.add(propertyName);
            }
        }
    }

    protected WorkbenchDefinition getWorkbenchDefinition() {
        return workbenchDefinition;
    }

    @Override
    public void addItemSetChangeListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners == null) {
            itemSetChangeListeners = new LinkedHashSet<ItemSetChangeListener>();
        }
        itemSetChangeListeners.add(listener);
    }

    @Override
    public void addListener(ItemSetChangeListener listener) {
        addItemSetChangeListener(listener);
    }

    @Override
    public void removeItemSetChangeListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners != null) {
            itemSetChangeListeners.remove(listener);
            if (itemSetChangeListeners.isEmpty()) {
                itemSetChangeListeners = null;
            }
        }
    }

    @Override
    public void removeListener(ItemSetChangeListener listener) {
        removeItemSetChangeListener(listener);
    }

    public void fireItemSetChange() {
        log.debug("Firing item set changed");
        if (itemSetChangeListeners != null && !itemSetChangeListeners.isEmpty()) {
            final Container.ItemSetChangeEvent event = new AbstractContainer.ItemSetChangeEvent();
            Object[] array = itemSetChangeListeners.toArray();
            for (Object anArray : array) {
                ItemSetChangeListener listener = (ItemSetChangeListener) anArray;
                listener.containerItemSetChange(event);
            }
        }
    }

    protected Map<Long, String> getItemIndexes() {
        return itemIndexes;
    }

    public int getPageLength() {
        return pageLength;
    }

    public void setPageLength(int pageLength) {
        this.pageLength = pageLength;
    }

    public int getCacheRatio() {
        return cacheRatio;
    }

    public void setCacheRatio(int cacheRatio) {
        this.cacheRatio = cacheRatio;
    }

    /**************************************/
    /** Methods from interface Container **/
    /**
     * *********************************
     */

    @Override
    public Item getItem(Object itemId) {
        if (itemId == null) {
            return null;
        }
        try {
            final Session jcrSession = MgnlContext.getJCRSession(getWorkspace());
            if (!jcrSession.itemExists((String) itemId)) {
                return null;
            }
            javax.jcr.Item item = jcrSession.getItem((String) itemId);
            return item.isNode() ? new JcrNodeAdapter((Node) item) : new JcrPropertyAdapter((javax.jcr.Property) item);
        } catch (RepositoryException e) {
            log.error("", e);
            return null;
        }
    }

    @Override
    public Collection<String> getItemIds() {
        throw new UnsupportedOperationException(getClass().getName() + " does not support this method.");
    }

    @Override
    public Property<?> getContainerProperty(Object itemId, Object propertyId) {
        final Item item = getItem(itemId);
        if (item != null) {
            return item.getItemProperty(propertyId);
        }

        log.error("Couldn't find item {} so property {} can't be retrieved!", itemId, propertyId);
        return null;
    }

    /**
     * Gets the number of visible Items in the Container.
     */
    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean containsId(Object itemId) {
        if (itemId == null) {
            return false;
        }

        try {
            final Session jcrSession = MgnlContext.getJCRSession(getWorkspace());
            return jcrSession.itemExists((String) itemId);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        fireItemSetChange();
        return getItem(itemId);
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**********************************************/
    /** Methods from interface Container.Indexed **/
    /**
     * *****************************************
     */

    @Override
    public int indexOfId(Object itemId) {

        if (!containsId(itemId)) {
            return -1;
        }
        int size = size();
        boolean wrappedAround = false;
        while (!wrappedAround) {
            for (Long i : itemIndexes.keySet()) {
                if (itemIndexes.get(i).equals(itemId)) {
                    return i.intValue();
                }
            }
            // load in the next page.
            int nextIndex = (currentOffset / (pageLength * cacheRatio) + 1) * (pageLength * cacheRatio);
            if (nextIndex >= size) {
                // Container wrapped around, start from index 0.
                wrappedAround = true;
                nextIndex = 0;
            }
            updateOffsetAndCache(nextIndex);
        }
        return -1;
    }

    @Override
    public Object getIdByIndex(int index) {
        if (index < 0 || index > size - 1) {
            return null;
        }
        final Long idx = Long.valueOf(index);
        if (itemIndexes.containsKey(idx)) {
            return itemIndexes.get(idx);
        }
        log.debug("item id {} not found in cache. Need to update offset, fetch new item ids from jcr repo and put them in cache.", index);
        updateOffsetAndCache(index);
        return itemIndexes.get(idx);
    }

    /**********************************************/
    /** Methods from interface Container.Ordered **/
    /**
     * *****************************************
     */

    @Override
    public Object nextItemId(Object itemId) {
        return getIdByIndex(indexOfId(itemId) + 1);
    }

    @Override
    public Object prevItemId(Object itemId) {
        return getIdByIndex(indexOfId(itemId) - 1);
    }

    @Override
    public Object firstItemId() {
        if (size == 0) {
            return null;
        }
        if (!itemIndexes.containsKey(LONG_ZERO)) {
            updateOffsetAndCache(0);
        }
        return itemIndexes.get(LONG_ZERO);
    }

    @Override
    public Object lastItemId() {
        final Long lastIx = Long.valueOf(size() - 1);
        if (!itemIndexes.containsKey(lastIx)) {
            updateOffsetAndCache(size - 1);
        }
        return itemIndexes.get(lastIx);
    }

    @Override
    public boolean isFirstId(Object itemId) {
        return firstItemId().equals(itemId);
    }

    @Override
    public boolean isLastId(Object itemId) {
        return lastItemId().equals(itemId);
    }

    /***********************************************/
    /** Methods from interface Container.Sortable **/
    /**
     * ******************************************
     */
    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        resetOffset();
        sorters.clear();
        for (int i = 0; i < propertyId.length; i++) {
            if (sortableProperties.contains(propertyId[i])) {
                OrderBy orderBy = new OrderBy((String) propertyId[i], ascending[i]);
                sorters.add(orderBy);
            }
        }
        getPage();
    }

    @Override
    public List<String> getSortableContainerPropertyIds() {
        return Collections.unmodifiableList(sortableProperties);
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        fireItemSetChange();
        return true;
    }

    /************************************/
    /** UNSUPPORTED CONTAINER FEATURES **/
    /**
     * *******************************
     */

    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Determines a new offset for updating the row cache. The offset is calculated from the given index, and will be
     * fixed to match the start of a page, based on the value of pageLength.
     *
     * @param index Index of the item that was requested, but not found in cache
     */
    private void updateOffsetAndCache(int index) {
        if (itemIndexes.containsKey(Long.valueOf(index))) {
            return;
        }
        currentOffset = (index / (pageLength * cacheRatio)) * (pageLength * cacheRatio);
        if (currentOffset < 0) {
            resetOffset();
        }
        getPage();
    }

    /**
     * Triggers a refresh if the current row count has changed.
     */
    private void updateCount(long newSize) {
        if (newSize != size) {
            setSize((int) newSize);
        }
    }

    /**
     * Fetches a page from the data source based on the values of pageLength, cacheRatio and currentOffset.
     */
    private final void getPage() {

        final String stmt = constructJCRQuery(true);
        if (StringUtils.isEmpty(stmt)) {
            return;
        }

        try {
            final QueryResult queryResult = executeQuery(stmt, QUERY_LANGUAGE, pageLength * cacheRatio, currentOffset);

            updateItems(queryResult);

        } catch (RepositoryException re) {
            throw new RuntimeRepositoryException(re);
        }
    }

    /**
     * Updates this container by storing the items found in the query result passed as argument.
     *
     * @see #getPage()
     */
    private void updateItems(final QueryResult queryResult) throws RepositoryException {
        long start = System.currentTimeMillis();
        log.debug("Starting iterating over QueryResult");
        final RowIterator iterator = queryResult.getRows();
        long rowCount = currentOffset;
        while (iterator.hasNext()) {
            final Node node = iterator.nextRow().getNode(SELECTOR_NAME);
            final String id = node.getPath();
            log.debug("Adding node {} to cached items.", id);
            itemIndexes.put(rowCount++, id);
        }

        log.debug("Done in {} ms", System.currentTimeMillis() - start);
    }

    /**
     * @param considerSorting an optional <code>ORDER BY</code> is added if this parameter is <code>true</code>. Sorting options can be configured in the {@link WorkbenchDefinition}.
     * @return a string representing a JCR statement to retrieve this container's items.
     *         It creates a JCR query in the form {@code select * from [nodeType] as selector [WHERE] [ORDER BY]"}.
     *         <p>
     *         Subclasses can customize the optional <code>WHERE</code> clause by overriding {@link #getQueryWhereClause()}.
     *         <p>
     *         The main item type (as configured in the {@link WorkbenchDefinition}) in the <code>SELECT</code> statement can be changed to something different by calling {@link #getQuerySelectStatement()}
     */
    protected final String constructJCRQuery(final boolean considerSorting) {
        final String select = getQuerySelectStatement();
        final StringBuilder stmt = new StringBuilder(select);
        // Return results only within the node configured in workbench/path
        stmt.append(getQueryWhereClause());

        if (considerSorting) {
            if (sorters.isEmpty()) {
                // no sorters set - use defaultOrder (always ascending)
                String defaultOrder = workbenchDefinition.getDefaultOrder();
                String[] defaultOrders = defaultOrder.split(",");
                for (String current : defaultOrders) {
                    sorters.add(new OrderBy(current, true));
                }
            }
            stmt.append(ORDER_BY);
            String sortOrder;
            for (OrderBy orderBy : sorters) {
                String propertyName = orderBy.getProperty();
                sortOrder = orderBy.isAscending() ? ASCENDING_KEYWORD : DESCENDING_KEYWORD;
                if (ModelConstants.JCR_NAME.equals(propertyName)) {
                    stmt.append(JCR_NAME_FUNCTION).append(sortOrder).append(", ");
                    continue;
                }
                stmt.append(SELECTOR_NAME);
                stmt.append(".[").append(propertyName).append("]").append(sortOrder).append(", ");
            }
            stmt.delete(stmt.lastIndexOf(","), stmt.length());
        }
        log.debug("Constructed JCR query is {}", stmt);
        return stmt.toString();
    }

    /**
     * @return the JCR query clause to select only nodes with the path configured in the workspace as String - in case
     *         it's not configured return a blank string so that all nodes are considered. Internally calls {@link #getQueryWhereClauseWorkspacePath()} to determine
     *         the path under which to perform the search.
     */
    protected String getQueryWhereClause() {
        String whereClause = "";
        String clauseWorkspacePath = getQueryWhereClauseWorkspacePath();
        if (!"".equals(clauseWorkspacePath)) {
            whereClause = " where " + clauseWorkspacePath;
        }
        log.debug("JCR query WHERE clause is {}", whereClause);
        return whereClause;
    }

    /**
     * @return if {@link WorkbenchDefinition#getPath()} is not null or root ("/"), an ISDESCENDATNODE constraint narrowing the scope of search under the configured path, else an empty string.
     *         Used by {@link #getQueryWhereClause()} to build a where clause.
     */
    protected final String getQueryWhereClauseWorkspacePath() {
        // By default, search the root and therefore do not need a query clause.
        String whereClauseWorkspacePath = "";
        if (StringUtils.isNotBlank(workbenchDefinition.getPath()) && !"/".equals(workbenchDefinition.getPath())) {
            whereClauseWorkspacePath = String.format(WHERE_TEMPLATE_FOR_PATH, workbenchDefinition.getPath());
        }
        log.debug("Workspace path where-clause is {}", whereClauseWorkspacePath);
        return whereClauseWorkspacePath;
    }

    /**
     * @return a <code>SELECT</code> statement with the main item type as configured in the {@link WorkbenchDefinition}. Can be customized by subclasses to utilize other item types, i.e. {@code select * from [my:fancytype]}. Used internally by {@link #constructJCRQuery(boolean)}.
     */
    protected String getQuerySelectStatement() {
        return String.format(SELECT_TEMPLATE, getMainNodeType());
    }

    /**
     * @return the main NodeType to be used with this container. This is the type that will be used for querying e.g. when populating the list view.
     */
    protected String getMainNodeType() {
        final List<NodeTypeDefinition> nodeTypes = workbenchDefinition.getNodeTypes();
        return nodeTypes.isEmpty() ? DEFAULT_NODE_TYPE : nodeTypes.get(0).getName();
    }

    /**
     * @see #getPage().
     */
    public final void updateSize() {
        try {
            final String stmt = constructJCRQuery(false);
            // query for all items in order to get the size
            final QueryResult queryResult = executeQuery(stmt, QUERY_LANGUAGE, 0, 0);

            final long pageSize = queryResult.getRows().getSize();
            log.debug("Query result set contains {} items", pageSize);

            updateCount((int) pageSize);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public List<?> getItemIds(int startIndex, int numberOfItems) {
        return ContainerHelpers.getItemIdsUsingGetIdByIndex(startIndex, numberOfItems, this);
    }

    public String getWorkspace() {
        return workbenchDefinition.getWorkspace();
    }

    /**
     * Refreshes the container - clears all caches and resets size and offset. Does NOT remove sorting or filtering
     * rules!
     */
    public void refresh() {
        resetOffset();
        itemIndexes.clear();
        updateSize();
    }

    protected void resetOffset() {
        currentOffset = 0;
    }

    protected int getCurrentOffset() {
        return currentOffset;
    }

    protected void setSize(int size) {
        this.size = size;
    }

    protected QueryResult executeQuery(String statement, String language, long limit, long offset) {
        try {
            final Session jcrSession = MgnlContext.getJCRSession(getWorkspace());
            final QueryManager jcrQueryManager = jcrSession.getWorkspace().getQueryManager();
            final Query query = jcrQueryManager.createQuery(statement, language);
            if (limit > 0) {
                query.setLimit(limit);
            }
            if (offset >= 0) {
                query.setOffset(offset);
            }
            log.debug("Executing query against workspace [{}] with statement [{}] and limit {} and offset {}...", new Object[]{getWorkspace(), statement, limit, offset});
            long start = System.currentTimeMillis();
            final QueryResult result = query.execute();
            log.debug("Query execution took {} ms", System.currentTimeMillis() - start);

            return result;

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
}
