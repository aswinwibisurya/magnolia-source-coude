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

import com.vaadin.data.Item;
import com.vaadin.ui.ComponentContainer;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.app.content.ContentAppDescriptor;
import info.magnolia.ui.admincentral.event.ContentChangedEvent;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.LoginException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;


/**
 * The workbench is a core component of AdminCentral. It represents the main hub through which users
 * can interact with JCR data. It is compounded by three main sub-components:
 * <ul>
 * <li>a configurable data grid.
 * <li>a configurable function toolbar on top of the data grid, providing operations such as
 * switching from tree to list view or performing searches on data.
 * <li>a configurable action bar on the right hand side, showing the available operations for the
 * given workspace and the selected item.
 * </ul>
 * 
 * <p>
 * Its main configuration point is the {@link WorkbenchDefinition} through which one defines the JCR
 * workspace to connect to, the columns/properties to display, the available actions and so on.
 */
@SuppressWarnings("serial")
public class ContentWorkbench implements IsVaadinComponent, ContentWorkbenchView.Presenter {

    private static final Logger log = LoggerFactory.getLogger(ContentWorkbench.class);

    private WorkbenchDefinition workbenchDefinition;

    private final ContentWorkbenchView view;

    private final EventBus eventBus;

    private final Shell shell;

    private final WorkbenchActionFactory actionFactory;

    private final Map<String, ActionDefinition> actions = new HashMap<String, ActionDefinition>();

    private String selectedItemId;

    private final AppContext context;

    @Inject
    public ContentWorkbench(final AppContext context, final ContentWorkbenchView view, final EventBus eventbus, final Shell shell, final WorkbenchActionFactory actionFactory) {
        this.context = context;
        this.view = view;
        this.eventBus = eventbus;
        this.shell = shell;
        this.actionFactory = actionFactory;
        view.setPresenter(this);

        eventBus.addHandler(ContentChangedEvent.class, new ContentChangedEvent.Handler() {

            @Override
            public void onContentChanged(ContentChangedEvent event) {
                // this should go into the presenter of the treegrid
                view.refresh();
            }
        });
    }

    public void initWorkbench(final String id) {
        // load the workbench specific configuration if existing
        workbenchDefinition = ((ContentAppDescriptor) context.getAppDescriptor()).getWorkbench();
        view.initWorkbench(workbenchDefinition);
        view.initActionbar(workbenchDefinition.getActionbar());
    }

    @Override
    public ComponentContainer asVaadinComponent() {
        return view;
    }

    @Override
    public void onItemSelected(Item item) {
        if (item == null) {
            log.warn("Got null javax.jcr.Item. No ItemSelectedEvent will be fired.");
            return;
        }
        try {
            // FIXME this seems to be triggered twice both for click row event and tableValue
            // change even when no value has changed and only a click happened on table, see
            // info.magnolia.ui.admincentral.tree.view.TreeViewImpl.TreeViewImpl
            // and jcrBrowser internal obj registering for those events.
            selectedItemId = ((JcrItemAdapter) item).getItemId();
            log.debug("javax.jcr.Item at {} was selected. Firing ItemSelectedEvent...", selectedItemId);
            eventBus.fireEvent(new ItemSelectedEvent(workbenchDefinition.getWorkspace(), selectedItemId));
        } catch (Exception e) {
            shell.showError("An error occurred while selecting a row in the data grid", e);
        }
    }

    @Override
    public String getSelectedItemId() {
        return selectedItemId;
    }

    //
    // ACTIONBAR PRESENTER
    //

    @Override
    public Map<String, ActionDefinition> getActions() {
        return actions;
    }

    @Override
    public void addAction(String actionName, ActionDefinition actionDefinition) {
        if (StringUtils.isNotBlank(actionName)) {
            actions.put(actionName, actionDefinition);
        }
    }

    @Override
    public void onActionbarItemClicked(final String actionName) {
        ActionDefinition actionDefinition = getActions().get(actionName);
        if (actionDefinition != null) {
            try {
                Session session = MgnlContext.getJCRSession(workbenchDefinition.getWorkspace());
                if (selectedItemId == null || !session.itemExists(selectedItemId)) {
                    log.debug("{} does not exist anymore. Was it just deleted? Resetting path to root...", selectedItemId);
                    selectedItemId = "/";
                }
                final javax.jcr.Item item = session.getItem(selectedItemId);
                Action action = actionFactory.createAction(actionDefinition, item, this);
                action.execute();
            } catch (PathNotFoundException e) {
                shell.showError("Can't execute action.\n" + e.getMessage(), e);
            } catch (LoginException e) {
                shell.showError("Can't execute action.\n" + e.getMessage(), e);
            } catch (RepositoryException e) {
                shell.showError("Can't execute action.\n" + e.getMessage(), e);
            } catch (ActionExecutionException e) {
                shell.showError("Can't execute action.\n" + e.getMessage(), e);
            }
        }
    }

    public ContentWorkbenchView asView() {
        return view;
    }

}
