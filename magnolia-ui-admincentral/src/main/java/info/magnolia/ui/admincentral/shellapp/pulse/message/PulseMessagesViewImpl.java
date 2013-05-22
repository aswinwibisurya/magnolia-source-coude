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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import static info.magnolia.ui.admincentral.shellapp.pulse.message.PulseMessagesPresenter.*;

import info.magnolia.ui.admincentral.shellapp.pulse.message.PulseMessageCategoryNavigator.CategoryChangedEvent;
import info.magnolia.ui.admincentral.shellapp.pulse.message.PulseMessageCategoryNavigator.MessageCategoryChangedListener;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.vaadin.grid.MagnoliaTreeTable;
import info.magnolia.ui.workbench.column.DateColumnFormatter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.time.FastDateFormat;

import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.GeneratedRow;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;

/**
 * Implementation of {@link PulseMessagesView}.
 */
public class PulseMessagesViewImpl extends CustomComponent implements PulseMessagesView {

    private static final String[] headers = new String[]{"New", "Type", "Message text", "Sender", "Date", "Quick action"};

    private static final String[] order = new String[] { NEW_PROPERTY_ID, TYPE_PROPERTY_ID, TEXT_PROPERTY_ID, SENDER_PROPERTY_ID, DATE_PROPERTY_ID, QUICKDO_PROPERTY_ID };

    private final TreeTable messageTable = new MagnoliaTreeTable();

    private final VerticalLayout root = new VerticalLayout();

    private final PulseMessageCategoryNavigator navigator = new PulseMessageCategoryNavigator();

    private PulseMessagesView.Listener listener;

    private final FastDateFormat dateFormatter = FastDateFormat.getInstance();

    private boolean grouping = false;

    private Label emptyPlaceHolder = new Label("No messages to display.");

    @Inject
    public PulseMessagesViewImpl() {
        setSizeFull();
        root.setSizeFull();
        setCompositionRoot(root);
        construct();
    }

    @Override
    public void setListener(PulseMessagesView.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setDataSource(Container dataSource) {
        messageTable.setContainerDataSource(dataSource);
        messageTable.setVisibleColumns(order);
        messageTable.setSortContainerPropertyId(DATE_PROPERTY_ID);
        messageTable.setSortAscending(false);
        messageTable.setColumnHeaders(headers);

    }

    private void construct() {
        root.addComponent(navigator);
        navigator.addCategoryChangeListener(new MessageCategoryChangedListener() {

            @Override
            public void messageCategoryChanged(CategoryChangedEvent event) {
                listener.filterByMessageCategory(event.getCategory());
            }
        });
        constructTable();
        emptyPlaceHolder.addStyleName("emptyplaceholder");
        root.addComponent(emptyPlaceHolder);
        setComponentVisibility(messageTable.getContainerDataSource());
    }

    private void constructTable() {
        root.addComponent(messageTable);
        root.setExpandRatio(messageTable, 1f);
        messageTable.setSizeFull();
        messageTable.addGeneratedColumn(NEW_PROPERTY_ID, newMessageColumnGenerator);
        messageTable.addGeneratedColumn(TYPE_PROPERTY_ID, typeColumnGenerator);
        messageTable.addGeneratedColumn(DATE_PROPERTY_ID, new DateColumnFormatter(null));
        messageTable.setRowGenerator(groupingRowGenerator);
        messageTable.setCellStyleGenerator(cellStyleGenerator);

        navigator.addGroupingListener(groupingListener);



        messageTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                Object itemId = event.getItemId();
                listener.onMessageClicked((String) itemId);
            }
        });

        messageTable.addValueChangeListener(selectionListener);
        messageTable.addItemSetChangeListener(new Container.ItemSetChangeListener() {
            @Override
            public void containerItemSetChange(ItemSetChangeEvent event) {
                setComponentVisibility(event.getContainer());
            }
        });
    }

    private void setComponentVisibility(Container container) {
        boolean isEmptyList = container.getItemIds().size() == 0;
        if (isEmptyList) {
            root.setExpandRatio(emptyPlaceHolder, 1f);
            // Use expand ratio to hide message table.
            // setVisible() would cause rendering issues.
            root.setExpandRatio(messageTable, 0f);
        } else {
            root.setExpandRatio(emptyPlaceHolder, 0f);
            root.setExpandRatio(messageTable, 1f);
        }

        messageTable.setVisible(!isEmptyList);
        emptyPlaceHolder.setVisible(isEmptyList);
    }

    private Property.ValueChangeListener selectionListener = new Property.ValueChangeListener() {

        private Set<Object> prevSelected = new HashSet<Object>();

        @Override
        public void valueChange(ValueChangeEvent event) {
            /*
             * selecting/unselecting cause valueChange events and it is not
             * preferred that an event handler generates more events.
             */
            messageTable.removeValueChangeListener(this);

            @SuppressWarnings("unchecked")
            Set<Object> currSelected = new HashSet<Object>((Set<Object>) event.getProperty().getValue());
            Set<Object> added = new HashSet<Object>(currSelected);
            Set<Object> removed = new HashSet<Object>(prevSelected);

            added.removeAll(prevSelected);
            removed.removeAll(currSelected);
            // now know what has been added or removed

            prevSelected = currSelected;

            /*
             * if group line was added/removed then select/unselect all it's
             * children
             */
            selectChildren(added, true);
            selectChildren(removed, false);

            // Item deselection will always deselect group
            for (Object child : removed) {
                Object parent = listener.getParent(child);
                if (parent != null) {
                    messageTable.unselect(parent);
                    prevSelected.remove(parent);
                }
            }

            /*
             * Selecting item must check that all siblings are also selected
             */
            for (Object child : added) {
                Object parent = listener.getParent(child);
                if (isAllChildrenSelected(parent)) {
                    messageTable.select(parent);
                    prevSelected.add(parent);
                } else {
                    messageTable.unselect(parent);
                    prevSelected.remove(parent);
                }
            }

            messageTable.addValueChangeListener(this);
        }

        private boolean isAllChildrenSelected(Object parent) {
            if (parent == null) {
                return false;
            }

            Collection<?> siblings = listener.getGroup(parent);
            boolean allSelected = true;

            if (siblings != null) {
                for (Object sibling : siblings) {
                    if (!messageTable.isSelected(sibling)) {
                        allSelected = false;
                    }
                }
            } else {
                return false;
            }

            return allSelected;
        }

        private void selectChildren(Set<Object> parents, boolean check) {
            for (Object parent : parents) {
                Collection<?> group = listener.getGroup(parent);
                if (group != null) {
                    for (Object child : group) {
                        if (check) {
                            messageTable.select(child);
                            prevSelected.add(child);
                        } else {
                            messageTable.unselect(child);
                            prevSelected.remove(child);
                        }
                    }
                }
            }
        }
    };

    private ValueChangeListener groupingListener = new ValueChangeListener() {

        @Override
        public void valueChange(ValueChangeEvent event) {
            boolean checked = event.getProperty().getValue().equals(Boolean.TRUE);
            listener.setGrouping(checked);
            grouping = checked;

            if (checked) {
                for (Object itemId : messageTable.getItemIds()) {
                    messageTable.setCollapsed(itemId, false);
                }
            }
        }
    };

    private Table.CellStyleGenerator cellStyleGenerator = new Table.CellStyleGenerator() {
        @Override
        public String getStyle(Table source, Object itemId, Object propertyId) {
            if (grouping && propertyId != null && TYPE_PROPERTY_ID.equals(propertyId)) {
                return "v-cell-invisible";
            }


            return "";
        }
    };

    /*
     * Row generator draws grouping headers if such are present in container
     */
    private Table.RowGenerator groupingRowGenerator = new Table.RowGenerator() {

        @Override
        public GeneratedRow generateRow(Table table, Object itemId) {

            /*
             * When sorting by type special items are inserted into Container to
             * acts as a placeholder for grouping sub section. This row
             * generator must render those special items.
             */
            if (itemId.toString().startsWith(PulseMessagesPresenter.GROUP_PLACEHOLDER_ITEMID)) {
                Item item = table.getItem(itemId);
                Property<?> property = item.getItemProperty(TYPE_PROPERTY_ID);

                GeneratedRow generated = new GeneratedRow("", "", property.getValue().toString());
                generated.setSpanColumns(false);
                return generated;
            }

            return null;
        }
    };

    private Table.ColumnGenerator newMessageColumnGenerator = new Table.ColumnGenerator() {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {

            if (NEW_PROPERTY_ID.equals(columnId)) {
                final Property<Boolean> newProperty = source.getContainerProperty(itemId, columnId);
                final Boolean isNew = newProperty != null && newProperty.getValue();
                if (isNew) {
                    final Label newMessage = new Label();
                    newMessage.setSizeUndefined();
                    newMessage.addStyleName("icon-tick");
                    newMessage.addStyleName("new-message");
                    return newMessage;
                }
            }
            return null;
        }
    };
    private Table.ColumnGenerator typeColumnGenerator = new Table.ColumnGenerator() {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {

            if (TYPE_PROPERTY_ID.equals(columnId)) {
                final Property<MessageType> typeProperty = source.getContainerProperty(itemId, columnId);

                final MessageType messageType = typeProperty.getValue();
                final Label messageTypeIcon = new Label();
                messageTypeIcon.setSizeUndefined();
                messageTypeIcon.addStyleName("icon");
                messageTypeIcon.addStyleName("message-type");

                switch (messageType) {
                case ERROR:
                    messageTypeIcon.addStyleName("icon-error");
                    messageTypeIcon.addStyleName("error");
                    break;
                case INFO:
                    messageTypeIcon.addStyleName("icon-info");
                    messageTypeIcon.addStyleName("info");
                    break;
                case WARNING:
                    messageTypeIcon.addStyleName("icon-warning");
                    messageTypeIcon.addStyleName("warn");
                    break;
                }
                return messageTypeIcon;
            }
            return null;
        }
    };

    @Override
    public HasComponents asVaadinComponent() {
        return this;
    }

    @Override
    public void update(List<String> params) {
        if (params != null && !params.isEmpty()) {
            final Set<Object> values = new HashSet<Object>();
            String messageId = params.get(0);
            values.add(messageId);
            /**
             * This is a multi-select table, calling select would just add this
             * message to the current set of selected rows so setting the value
             * explicitly this way makes only this one selected.
             */
            messageTable.setValue(values);
        }
    }
}
