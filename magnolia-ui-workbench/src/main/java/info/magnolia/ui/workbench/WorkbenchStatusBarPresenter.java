/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.workbench;

import info.magnolia.event.EventBus;
import info.magnolia.ui.statusbar.StatusBarView;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemsSelectedEvent;

import java.util.Set;

import javax.inject.Inject;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * The browser features a status bar at the bottom with selected path and item count information.
 */
public class WorkbenchStatusBarPresenter {

    private final Logger log = LoggerFactory.getLogger(WorkbenchStatusBarPresenter.class);

    private final StatusBarView view;

    private EventBus eventBus;

    private int selectionCount;
    private int itemCount;

    private String countPattern = "%d item(s), %d selected";

    private final Label selectionLabel = new Label();
    private final Label countLabel = new Label();

    private Set<JcrItemAdapter> selectedItems;

    private String workbenchRootPath;

    @Inject
    public WorkbenchStatusBarPresenter(StatusBarView view) {
        this.view = view;
        countLabel.setSizeUndefined();
    }

    private void bindHandlers() {
        eventBus.addHandler(ItemsSelectedEvent.class, new ItemsSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ItemsSelectedEvent event) {
                setSelectedItems(event.getItems());
                setSelectionCount(event.getItems().size());
            }
        });
    }

    public StatusBarView start(EventBus eventBus, WorkbenchDefinition workbenchDefinition) {
        workbenchRootPath = StringUtils.defaultIfEmpty(workbenchDefinition.getPath(), "/");

        this.eventBus = eventBus;

        view.addComponent(selectionLabel, Alignment.MIDDLE_LEFT);
        ((HorizontalLayout) view).setExpandRatio(selectionLabel, 1);
        view.addComponent(countLabel, Alignment.MIDDLE_RIGHT);
        ((HorizontalLayout) view).setExpandRatio(countLabel, 0);

        bindHandlers();

        return view;
    }

    public void setSelectedItems(Set<JcrItemAdapter> items) {
        this.selectedItems = items;
        setSelectedItem(items.iterator().next());
    }

    public void setSelectedItem(JcrItemAdapter item) {
        String newValue = "";
        String newDescription = null;
        if (item != null) {
            javax.jcr.Item jcrItem = item.getJcrItem();
            try {
                newValue = jcrItem.getPath();

                if (!workbenchRootPath.equals("/")) {
                    newValue = StringUtils.removeStart(newValue, workbenchRootPath);
                }

                newDescription = newValue;
            } catch (RepositoryException e) {
                log.warn("Could not retrieve path from item with id " + item.getItemId(), e);
            }
        }
        selectionLabel.setValue(newValue);
        selectionLabel.setDescription(newDescription);
    }

    public void setSelectionCount(int selectionCount) {
        countLabel.setValue(String.format(countPattern, itemCount, selectionCount));
        this.selectionCount = selectionCount;
    }

    public void setItemCount(int itemCount) {
        countLabel.setValue(String.format(countPattern, itemCount, selectionCount));
        this.itemCount = itemCount;
    }
}
