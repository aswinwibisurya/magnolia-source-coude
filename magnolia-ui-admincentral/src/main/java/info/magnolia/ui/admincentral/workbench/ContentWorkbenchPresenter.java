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
package info.magnolia.ui.admincentral.workbench;


import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.app.content.ContentAppDescriptor;
import info.magnolia.ui.admincentral.content.view.ContentPresenter;
import info.magnolia.ui.admincentral.content.view.ContentView.ViewType;
import info.magnolia.ui.admincentral.event.ActionbarItemClickedEvent;
import info.magnolia.ui.admincentral.event.ContentChangedEvent;
import info.magnolia.ui.admincentral.event.ItemDoubleClickedEvent;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.ui.admincentral.event.SearchEvent;
import info.magnolia.ui.admincentral.event.ViewTypeChangedEvent;
import info.magnolia.ui.admincentral.search.view.SearchView;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.thumbnail.ImageProvider;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.widget.actionbar.ActionbarView;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The workbench is a core component of AdminCentral. It represents the main hub through which users
 * can interact with JCR data. It is compounded by three main sub-components:
 * <ul>
 * <li>a configurable data grid.
 * <li>a configurable function toolbar on top of the data grid, providing operations such as
 * switching from tree to list view or thumbnail view or performing searches on data.
 * <li>a configurable action bar on the right hand side, showing the available operations for the
 * given workspace and the selected item.
 * </ul>
 *
 * <p>
 * Its main configuration point is the {@link WorkbenchDefinition} through which one defines the JCR
 * workspace to connect to, the columns/properties to display, the available actions and so on.
 */
@SuppressWarnings("serial")
public class ContentWorkbenchPresenter implements ContentWorkbenchView.Listener {

    private static final Logger log = LoggerFactory.getLogger(ContentWorkbenchPresenter.class);

    private final WorkbenchDefinition workbenchDefinition;

    private final ContentWorkbenchView view;

    private final EventBus admincentralEventBus;

    private final EventBus subAppEventBus;

    private final Shell shell;

    private final WorkbenchActionFactory actionFactory;

    private final ContentPresenter contentPresenter;

    private final ActionbarPresenter actionbarPresenter;

    private final ImageProvider imageProvider;
    
    @Inject
    public ContentWorkbenchPresenter(final AppContext appContext, final ContentWorkbenchView view, @Named("admincentral") final EventBus admincentralEventBus, 
            final @Named("subapp") EventBus subAppEventBus, final Shell shell, final WorkbenchActionFactory actionFactory, final ContentPresenter contentPresenter, 
            final ActionbarPresenter actionbarPresenter) {
        this.view = view;
        this.admincentralEventBus = admincentralEventBus;
        this.subAppEventBus = subAppEventBus;
        this.shell = shell;
        this.actionFactory = actionFactory;
        this.contentPresenter = contentPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.workbenchDefinition = ((ContentAppDescriptor) appContext.getAppDescriptor()).getWorkbench();
        this.imageProvider = workbenchDefinition.getImageProvider();
    }

    public ContentWorkbenchView start() {
        view.setListener(this);
        contentPresenter.initContentView(view);
        ActionbarView actionbar = actionbarPresenter.start(workbenchDefinition.getActionbar(), actionFactory);
        view.setActionbarView(actionbar);

        bindHandlers();
        return view;
    }

    private void bindHandlers() {
        admincentralEventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                refreshActionbarPreviewImage(event.getPath(), event.getWorkspace());
                view.refresh();
            }
        });

        subAppEventBus.addHandler(ActionbarItemClickedEvent.class, new ActionbarItemClickedEvent.Handler() {

            @Override
            public void onActionbarItemClicked(ActionbarItemClickedEvent event) {
                try {
                    final ActionDefinition actionDefinition = event.getActionDefinition();
                    actionbarPresenter.createAndExecuteAction(actionDefinition, workbenchDefinition.getWorkspace(), getSelectedItemId());
                } catch (ActionExecutionException e) {
                    log.error("An error occurred while executing an action.", e);
                }
            }
        });

        subAppEventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                refreshActionbarPreviewImage(event.getPath(), event.getWorkspace());
            }
        });

        subAppEventBus.addHandler(ItemDoubleClickedEvent.class, new ItemDoubleClickedEvent.Handler() {

            @Override
            public void onItemDoubleClicked(ItemDoubleClickedEvent event) {
                executeDefaultAction();
            }
        });

        subAppEventBus.addHandler(SearchEvent.class, new SearchEvent.Handler() {

            @Override
            public void onSearch(SearchEvent event) {
                doSearch(event);
            }
        });
    }

    /**
     * @see ContentPresenter#getSelectedItemId()
     */
    public String getSelectedItemId() {
        return contentPresenter.getSelectedItemId();
    }

    public ContentWorkbenchView getView() {
        return view;
    }

    public ActionbarPresenter getActionbarPresenter() {
        return actionbarPresenter;
    }

    /**
     * Executes the workbench's default action, as configured in the defaultAction property.
     */
    public void executeDefaultAction() {
        ActionDefinition defaultActionDef = actionbarPresenter.getDefaultActionDefinition();
        try {
            actionbarPresenter.createAndExecuteAction(defaultActionDef, workbenchDefinition.getWorkspace(), getSelectedItemId());
        } catch (ActionExecutionException e) {
            log.error("An error occurred while executing an action.", e);
        }
    }


    @Override
    public void onSearch(final String searchExpression) {
        subAppEventBus.fireEvent(new SearchEvent(searchExpression));
    }

    @Override
    public void onViewTypeChanged(final ViewType viewType) {
        subAppEventBus.fireEvent(new ViewTypeChangedEvent(viewType));
    }

    public void selectPath(final String path) {
        this.view.selectPath(path);
    }

    /**
     * Synchronizes the underlying view to reflect the status extracted from the Location token, i.e. selected path, view type and optional query (in case of a search view).
     */
    public void resynch(final String path, final ViewType viewType, final String query) {
        boolean itemExists = itemExists(path);
        if(!itemExists) {
            log.warn("Trying to resynch workbench with no longer existing path {} at workspace {}. Will reset path to root.", path, workbenchDefinition.getWorkspace());
        }
        this.view.resynch(itemExists? path : "/", viewType, query);
    }

    private void refreshActionbarPreviewImage(final String path, final String workspace) {
        if (StringUtils.isBlank(path)) {
            actionbarPresenter.setPreview(null);
        } else {
            String imagePath = imageProvider.getPortraitPath(workspace, path);
            actionbarPresenter.setPreview(imagePath);
        }
    }

    private void doSearch(SearchEvent event) {
        if(view.getSelectedView().getViewType() != ViewType.SEARCH) {
            log.warn("Expected view type {} but is {} instead.", ViewType.SEARCH.name(), view.getSelectedView().getViewType().name());
            return;
        }
        final SearchView searchView = (SearchView) view.getSelectedView();
        final String searchExpression = event.getSearchExpression();

        if(StringUtils.isBlank(searchExpression)) {
            //clear last search results
            searchView.clear();
        } else {
            searchView.search(searchExpression);
        }
    }

    private boolean itemExists(String path) {
        try {
            return StringUtils.isNotBlank(path) && MgnlContext.getJCRSession(workbenchDefinition.getWorkspace()).itemExists(path);
        } catch (RepositoryException e) {
            log.warn("", e);
        }
        return false;
    }

}
