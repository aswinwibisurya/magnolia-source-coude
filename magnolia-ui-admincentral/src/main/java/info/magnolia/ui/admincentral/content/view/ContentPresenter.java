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
package info.magnolia.ui.admincentral.content.view;

import info.magnolia.ui.admincentral.app.content.ContentSubAppDescriptor;
import info.magnolia.ui.admincentral.content.view.ContentView.ViewType;
import info.magnolia.ui.admincentral.content.view.builder.ContentViewBuilder;
import info.magnolia.ui.admincentral.event.ItemDoubleClickedEvent;
import info.magnolia.ui.admincentral.event.ItemEditedEvent;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchView;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.event.EventBus;
import info.magnolia.ui.framework.app.SubAppEventBusConfigurer;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.workbench.definition.NodeTypeDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Presenter for ContentView.
 */
public class ContentPresenter implements ContentView.Listener {

    private static final String ICON_PROPERTY = "icon-node-data";

    private static final Logger log = LoggerFactory.getLogger(ContentPresenter.class);

    private final EventBus subAppEventBus;

    private final Shell shell;

    private final String workspaceName;

    private final ContentViewBuilder contentViewBuilder;

    protected WorkbenchDefinition workbenchDefinition;

    private String selectedItemPath;

    public ContentPresenter(final AppContext appContext, final ContentViewBuilder contentViewBuilder, @Named(SubAppEventBusConfigurer.EVENT_BUS_NAME) final EventBus subAppEventBus, final Shell shell) {
        this.contentViewBuilder = contentViewBuilder;
        this.subAppEventBus = subAppEventBus;
        this.shell = shell;

        final ContentSubAppDescriptor subAppDescriptor = (ContentSubAppDescriptor) appContext.getDefaultSubAppDescriptor();
        this.workbenchDefinition = subAppDescriptor.getWorkbench();
        this.workspaceName = subAppDescriptor.getWorkbench().getWorkspace();
    }

    @Inject
    public ContentPresenter(final SubAppContext subAppContext, final ContentViewBuilder contentViewBuilder, @Named(SubAppEventBusConfigurer.EVENT_BUS_NAME) final EventBus subAppEventBus, final Shell shell) {
        this.contentViewBuilder = contentViewBuilder;
        this.subAppEventBus = subAppEventBus;
        this.shell = shell;

        final ContentSubAppDescriptor subAppDescriptor = (ContentSubAppDescriptor) subAppContext.getSubAppDescriptor();
        this.workbenchDefinition = subAppDescriptor.getWorkbench();
        this.workspaceName = subAppDescriptor.getWorkbench().getWorkspace();
    }

    public void initContentView(ContentWorkbenchView parentView) {
        if (workbenchDefinition == null) {
            throw new IllegalArgumentException("Trying to init a workbench but got null definition.");
        }
        log.debug("Initializing workbench {}...", workbenchDefinition.getName());

        for (final ViewType type : ViewType.values()) {
            final ContentView contentView = contentViewBuilder.build(workbenchDefinition, type);
            contentView.setListener(this);
            // contentView.select(StringUtils.defaultIfEmpty(workbenchDefinition.getPath(), "/"));
            contentView.select("/");
            parentView.addContentView(type, contentView);
        }

        if (StringUtils.isBlank(workbenchDefinition.getWorkspace())) {
            throw new IllegalStateException(workbenchDefinition.getName() + " workbench definition must specify a workspace to connect to. Please, check your configuration.");
        }

        selectedItemPath = StringUtils.defaultIfEmpty(workbenchDefinition.getPath(), "/");
        parentView.setViewType(ViewType.TREE);
    }

    @Override
    public void onItemSelection(Item item) {
        if (item == null) {
            log.debug("Got null com.vaadin.data.Item. ItemSelectedEvent will be fired with null path.");
            selectedItemPath = workbenchDefinition.getPath();
            subAppEventBus.fireEvent(new ItemSelectedEvent(workspaceName, null));
            return;
        }
        try {
            selectedItemPath = ((JcrItemAdapter) item).getPath();
            log.debug("com.vaadin.data.Item at {} was selected. Firing ItemSelectedEvent...", selectedItemPath);
            subAppEventBus.fireEvent(new ItemSelectedEvent(workspaceName, (JcrItemAdapter) item));
        } catch (Exception e) {
            shell.showError("An error occurred while selecting a row in the data grid", e);
        }
    }

    /**
     * @return the path of the vaadin item currently selected in the currently active {@link ContentView}. It is
     *         equivalent to javax.jcr.Item#getPath().
     * @see JcrItemAdapter#getPath()
     */
    public String getSelectedItemPath() {
        return selectedItemPath;
    }

    @Override
    public void onDoubleClick(Item item) {
        if (item != null) {
            try {
                selectedItemPath = ((JcrItemAdapter) item).getPath();
                log.debug("com.vaadin.data.Item at {} was double clicked. Firing ItemDoubleClickedEvent...", selectedItemPath);
                subAppEventBus.fireEvent(new ItemDoubleClickedEvent(workspaceName, selectedItemPath));
            } catch (Exception e) {
                shell.showError("An error occurred while double clicking on a row in the data grid", e);
            }
        } else {
            log.warn("Got null com.vaadin.data.Item. No event will be fired.");
        }
    }

    @Override
    public void onItemEdited(Item item) {
        try {
            if (item != null) {
                log.debug("com.vaadin.data.Item edited. Firing ItemEditedEvent...");
                subAppEventBus.fireEvent(new ItemEditedEvent(item));
            } else {
                log.warn("Null item edited");
            }
        } catch (Exception e) {
            shell.showError("An error occured while editing an item in data grid", e);
        }
    }

    @Override
    public String getItemIcon(Item item) {
        if (item instanceof JcrPropertyAdapter && workbenchDefinition.includeProperties()) {
            return ICON_PROPERTY;
        }

        if (item instanceof JcrNodeAdapter) {
            JcrNodeAdapter node = (JcrNodeAdapter) item;
            String typeName = node.getPrimaryNodeTypeName();
            List<NodeTypeDefinition> nodeTypes = workbenchDefinition.getNodeTypes();
            for (NodeTypeDefinition currentNodeType: nodeTypes) {
                if (currentNodeType.getName().equals(typeName)) {
                    return currentNodeType.getIcon();
                }
            }
        }

        return null;
    }
}
