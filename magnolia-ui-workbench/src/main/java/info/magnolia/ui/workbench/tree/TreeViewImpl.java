/**
 * This file Copyright (c) 2011 Magnolia International
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

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.vaadin.grid.MagnoliaTreeTable;
import info.magnolia.ui.workbench.ContentView;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.column.definition.ColumnFormatter;
import info.magnolia.ui.workbench.container.AbstractJcrContainer;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemEditedEvent;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;
import info.magnolia.ui.workbench.tree.drop.TreeViewDropHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TreeTable;

/**
 * Vaadin UI component that displays a tree.
 */
public class TreeViewImpl implements TreeView {

    private static final Logger log = LoggerFactory.getLogger(TreeViewImpl.class);

    private final TreeTable treeTable;

    private final HierarchicalJcrContainer container;

    private ContentView.Listener listener;

    private Set<?> defaultValue = null;

    /**
     * Instantiates a new content tree view.
     *
     * @param workbench the workbench definition
     * @param componentProvider the component provider
     * @param container the container data source
     */
    public TreeViewImpl(WorkbenchDefinition workbench, ComponentProvider componentProvider) {
        this.container = new HierarchicalJcrContainer(workbench);

        treeTable = buildTreeTable(container, workbench, componentProvider);

        // Set Drop Handler
        Class<? extends DropConstraint> dropContainerClass = workbench.getDropConstraintClass();
        if (dropContainerClass != null) {
            DropConstraint constraint = componentProvider.newInstance(dropContainerClass);
            DropHandler dropHandler = new TreeViewDropHandler(treeTable, constraint);
            treeTable.setDropHandler(dropHandler);
            treeTable.setDragMode(TableDragMode.ROW);
            log.debug("Set following drop container {} to the treeTable", dropContainerClass.getName());
        }

        treeTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {

            private Object previousSelection;

            @Override
            public void itemClick(ItemClickEvent event) {
                Object currentSelection = event.getItemId();

                if (event.getButton()==MouseButton.RIGHT){
                    presenterOnRightClick(String.valueOf(event.getItemId()));

                } else if (event.isDoubleClick()) {
                    presenterOnDoubleClick(String.valueOf(event.getItemId()));

                } else {
                    // toggle will deselect
                    if (previousSelection == currentSelection) {
                        treeTable.setValue(null);
                    }
                }

                previousSelection = currentSelection;
            }
        });

        treeTable.addValueChangeListener(new TreeTable.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                if (defaultValue == null && event.getProperty().getValue() instanceof Set) {
                    defaultValue = (Set<?>) event.getProperty().getValue();
                }
                final Object value = event.getProperty().getValue();
                if (value instanceof String) {
                    presenterOnItemSelection(String.valueOf(value));
                } else if (value instanceof Set) {
                    final Set<?> set = new HashSet<Object>((Set<?>) value);
                    set.removeAll(defaultValue);
                    if (set.size() == 1) {
                        presenterOnItemSelection(String.valueOf(set.iterator().next()));
                    } else if (set.size() == 0) {
                        presenterOnItemSelection(null);
                        treeTable.setValue(null);
                    }
                }
            }
        });

    }

    // CONFIGURE TREE TABLE

    private TreeTable buildTreeTable(final Container container, WorkbenchDefinition workbench, ComponentProvider componentProvider) {

        TreeTable treeTable = (workbench.isEditable()) ? new InplaceEditingTreeTable() : new MagnoliaTreeTable();

        // basic widget configuration
        treeTable.setNullSelectionAllowed(true);
        treeTable.setColumnCollapsingAllowed(false);
        treeTable.setColumnReorderingAllowed(false);
        treeTable.setCollapsed(treeTable.firstItemId(), false);
        treeTable.setSizeFull();

        // data model
        treeTable.setContainerDataSource(container);
        buildColumns(treeTable, container, workbench.getColumns(), componentProvider);

        // listeners
        if (workbench.isEditable()) {
            ((InplaceEditingTreeTable) treeTable).addListener(new ItemEditedEvent.Handler() {

                @Override
                public void onItemEdited(ItemEditedEvent event) {
                    presenterOnEditItem(event);
                }
            });
        }

        treeTable.setCellStyleGenerator(new Table.CellStyleGenerator() {

            @Override
            public String getStyle(Table source, Object itemId, Object propertyId) {
                return presenterGetIcon(itemId, propertyId);
            }
        });

        return treeTable;
    }

    /**
     * Sets the columns for the vaadin TreeTable, based on workbench columns configuration.
     *
     * @param treeTable the TreeTable vaadin component
     * @param container the container data source
     * @param columns the list of ColumnDefinitions
     * @param componentProvider the component provider
     */
    protected void buildColumns(TreeTable treeTable, Container container, List<ColumnDefinition> columns, ComponentProvider componentProvider) {
        final Iterator<ColumnDefinition> iterator = columns.iterator();
        final List<String> visibleColumns = new ArrayList<String>();
        final List<String> editableColumns = new ArrayList<String>();

        while (iterator.hasNext()) {
            final ColumnDefinition column = iterator.next();
            final String columnProperty = column.getPropertyName() != null ? column.getPropertyName() : column.getName();

            // Add data column
            container.addContainerProperty(columnProperty, column.getType(), "");
            visibleColumns.add(columnProperty);

            // Set appearance
            treeTable.setColumnHeader(columnProperty, column.getLabel());
            if (column.getWidth() > 0) {
                treeTable.setColumnWidth(columnProperty, column.getWidth());
            } else if (column.getExpandRatio() > 0) {
                treeTable.setColumnExpandRatio(columnProperty, column.getExpandRatio());
            }

            // Generated columns
            Class<? extends ColumnFormatter> formatterClass = column.getFormatterClass();
            if (formatterClass != null) {
                ColumnFormatter formatter = componentProvider.newInstance(formatterClass, column);
                treeTable.addGeneratedColumn(columnProperty, formatter);
            }

            // Inplace editing
            if (column.isEditable()) {
                editableColumns.add(columnProperty);
            }
        }

        treeTable.setVisibleColumns(visibleColumns.toArray());
        if (treeTable instanceof InplaceEditingTreeTable) {
            ((InplaceEditingTreeTable) treeTable).setEditableColumns(editableColumns.toArray());
        }
    }

    // CONTENT VIEW IMPL

    @Override
    public void select(String path) {
        if (!"/".equals(path)) {
            expandTreeToNode(path);
            treeTable.setCurrentPageFirstItemId(path);
        }
        treeTable.select(path);

    }

    /**
     * Expand the parent nodes of the node specified in the path.
     */
    public void expandTreeToNode(String path){
        String[] segments = path.split("/");
        String segmentPath = "";
        // Expand each parent node in turn.
        for (int s = 0; s < segments.length - 1; s++) {
            if (!"".equals(segments[s])) {
                segmentPath += "/" + segments[s];
                treeTable.setCollapsed(segmentPath, false);
            }
        }
    }

    @Override
    public void refresh() {
        container.refresh();
        container.fireItemSetChange();
    }

    @Override
    public AbstractJcrContainer getContainer() {
        return container;
    }

    @Override
    public ViewType getViewType() {
        return ViewType.TREE;
    }

    @Override
    public void setListener(ContentView.Listener listener) {
        this.listener = listener;
    }

    private void presenterOnItemSelection(String id) {
        if (listener != null) {
            listener.onItemSelection(treeTable.getItem(id));
        }
    }

    private void presenterOnDoubleClick(String id) {
        if (listener != null) {
            listener.onDoubleClick(treeTable.getItem(id));
        }
    }

    private void presenterOnRightClick(String id) {
        if (listener != null) {
            listener.onRightClick(treeTable.getItem(id));
        }
    }

    private void presenterOnEditItem(ItemEditedEvent event) {
        if (listener != null) {
            listener.onItemEdited(event.getItem());

            // Clear preOrder cache of itemIds in case node was renamed
            TreeViewImpl.this.container.fireItemSetChange();
        }
    }

    @Override
    public Component asVaadinComponent() {
        return treeTable;
    }

    private String presenterGetIcon(Object itemId, Object propertyId) {
        Container container = treeTable.getContainerDataSource();
        if (listener != null && propertyId == null) {
            return listener.getItemIcon(container.getItem(itemId));
        }

        return null;
    }

    @Override
    public void deactivateDragAndDrop() {
        treeTable.setDragMode(TableDragMode.NONE);
    }
}
