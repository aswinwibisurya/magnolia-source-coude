/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
package info.magnolia.ui.contentapp.browser;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.AvailabilityRule;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.contentapp.ContentSubAppView;
import info.magnolia.ui.contentapp.dsmanager.DataSourceManagerProvider;
import info.magnolia.ui.vaadin.integration.dsmanager.DataSourceManager;
import info.magnolia.ui.framework.app.BaseSubApp;
import info.magnolia.ui.vaadin.actionbar.ActionPopup;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemRightClickedEvent;
import info.magnolia.ui.workbench.event.SearchEvent;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;
import info.magnolia.ui.workbench.event.ViewTypeChangedEvent;
import info.magnolia.ui.workbench.search.SearchPresenterDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.peter.contextmenu.ContextMenu;

import com.vaadin.data.Item;
import com.vaadin.server.ExternalResource;

/**
 * Base implementation of a content subApp. A content subApp displays a collection of data represented inside a {@link info.magnolia.ui.workbench.ContentView} created by the {@link info.magnolia.ui.workbench.WorkbenchPresenter}.
 * 
 * <pre>
 *  <p>
 *      This class Provides sensible implementation for services shared by all content subApps.
 *      Out-of-the-box it will handle the following:
 *  </p>
 * 
 *  <ul>
 *      <li>location updates when switching views, selecting items or performing searches: see {@link #locationChanged(Location)}
 *      <li>restoring the browser app status when i.e. coming from a bookmark: see {@link #start(Location)}
 *  </ul>
 * In order to perform those tasks this class registers non-overridable handlers for the following events:
 *  <ul>
 *      <li> {@link SelectionChangedEvent}
 *      <li> {@link ViewTypeChangedEvent}
 *      <li> {@link SearchEvent}
 *  </ul>
 * Subclasses can augment the default behavior and perform additional tasks by overriding the following methods:
 *  <ul>
 *      <li>{@link #onSubAppStart()}
 *      <li>{@link #locationChanged(Location)}
 *      <li>{@link #updateActionbar(ActionbarPresenter)}
 *  </ul>
 * </pre>
 * 
 * @see BrowserPresenter
 * @see info.magnolia.ui.contentapp.ContentSubAppView
 * @see info.magnolia.ui.contentapp.ContentApp
 * @see BrowserLocation
 */
public abstract class BrowserSubAppBase extends BaseSubApp<ContentSubAppView> {

    private static final Logger log = LoggerFactory.getLogger(BrowserSubApp.class);

    private final BrowserPresenterBase browser;
    private final EventBus subAppEventBus;
    private ActionExecutor actionExecutor;
    private ComponentProvider componentProvider;
    protected String workbenchRoot;
    protected DataSourceManager dsManager;

    @Inject
    public BrowserSubAppBase(ActionExecutor actionExecutor, final SubAppContext subAppContext, final ContentSubAppView view, final BrowserPresenterBase browser, final @Named(SubAppEventBus.NAME) EventBus subAppEventBus, final ComponentProvider componentProvider, DataSourceManagerProvider dsManagerProvider) {
        super(subAppContext, view);
        if (subAppContext == null || view == null || browser == null || subAppEventBus == null) {
            throw new IllegalArgumentException("Constructor does not allow for null args. Found SubAppContext = " + subAppContext + ", ContentSubAppView = " + view + ", BrowserPresenter = " + browser + ", EventBus = " + subAppEventBus);
        }
        this.browser = browser;
        this.subAppEventBus = subAppEventBus;
        this.actionExecutor = actionExecutor;
        this.componentProvider = componentProvider;
        this.workbenchRoot = getWorkbench().getPath();
        this.dsManager = dsManagerProvider.getDSManager(getSubAppContext().getAppContext());
    }

    @Override
    protected void onSubAppStart() {
        super.onSubAppStart();

    }

    protected Object ensureLocationUpToDate(String urlFragmentPath) {
        Object actualItemId = dsManager.deserializeItemId(urlFragmentPath);

        // MGNLUI-1475: item might have not been found if path doesn't exist
        if (dsManager.itemExists(actualItemId)) {
            actualItemId = getRootItemId();
            BrowserLocation newLocation = getCurrentLocation();
            newLocation.updateNodePath("/");
            getAppContext().updateSubAppLocation(getSubAppContext(), newLocation);
        }

        return actualItemId;
    }

    protected Object getRootItemId() {
        return browser.getWorkbenchPresenter().resolveWorkbenchRoot();
    }


    //TODO JCRFREE - consider Actionbar availability interface that would encapsulate JCR/Non-JCR availability logic
    protected boolean verifyAvailability(Item item, AvailabilityDefinition availability) {
        // If this is the root item we display the section only if the root property is set
        if (item == null) {
            return availability.isRoot();
        }

        return true;
    }


    protected void applySelectionToLocation(BrowserLocation location, Object selectedId) {
        location.updateNodePath("");
        Item selected = dsManager.getItemById(selectedId);
        if (selected == null) {
            // nothing is selected at the moment
        } else {
            location.updateNodePath(dsManager.serializeItemId(selectedId));
        }
    }

    /**
     * Performs some routine tasks needed by all content subapps before the view is displayed.
     * The tasks are:
     * <ul>
     * <li>setting the current location
     * <li>setting the browser view
     * <li>restoring the browser status: see {@link #restoreBrowser(BrowserLocation)}
     * <li>calling {@link #onSubAppStart()} a hook-up method subclasses can override to perform additional work.
     * </ul>
     */
    @Override
    public ContentSubAppView start(final Location location) {
        BrowserLocation l = BrowserLocation.wrap(location);
        super.start(l);
        getView().setContentView(browser.start());
        restoreBrowser(l);
        registerSubAppEventsHandlers(subAppEventBus, this);

        return getView();
    }

    /**
     * Restores the browser status based on the information available in the location object. This is used e.g. when starting a subapp based on a
     * bookmark. I.e. given a bookmark containing the following URI fragment
     * <p>
     * {@code
     * #app:myapp:browser;/foo/bar:list
     * }
     * <p>
     * this method will select the path <code>/foo/bar</code> in the workspace used by the app, set the view type as <code>list</code> and finally update the available actions.
     * <p>
     * In case of a search view the URI fragment will look similar to the following one {@code
     * #app:myapp:browser;/:search:qux
     * }
     * <p>
     * then this method will select the root path, set the view type as <code>search</code>, perform a search for "qux" in the workspace used by the app and finally update the available actions.
     * 
     * @see BrowserSubApp#updateActionbar(ActionbarPresenter)
     * @see BrowserSubApp#start(Location)
     * @see Location
     */
    protected void restoreBrowser(final BrowserLocation location) {
        String path = ("/".equals(workbenchRoot) ? "" : workbenchRoot) + location.getNodePath();
        String viewType = location.getViewType();

        if (!getBrowser().hasViewType(viewType)) {
            if (!StringUtils.isBlank(viewType)) {
                log.warn("Unknown view type [{}], returning to default view type.", viewType);
            }
            viewType = getBrowser().getDefaultViewType();
            location.updateViewType(viewType);
            getAppContext().updateSubAppLocation(getSubAppContext(), location);
        }
        String query = location.getQuery();

        Object itemId = ensureLocationUpToDate(path);
        if (itemId != null) {
            getBrowser().resync(Arrays.asList(itemId), viewType, query);
            updateActionbar(getBrowser().getActionbarPresenter());
        }
    }


    /**
     * Show the actionPopup for the specified item at the specified coordinates.
     */
    public void showActionPopup(Object itemId, int x, int y) {

        // If there's no actionbar configured we don't want to show an empty action popup
        BrowserSubAppDescriptor subAppDescriptor = (BrowserSubAppDescriptor) getSubAppContext().getSubAppDescriptor();
        ActionbarDefinition actionbarDefinition = subAppDescriptor.getActionbar();
        if (actionbarDefinition == null) {
            return;
        }

        ActionPopup actionPopup = browser.getView().getActionPopup();

        updateActionPopup(actionPopup);
        actionPopup.open(x, y);
    }

    /**
     * Update the items in the actionPopup based on the selected item and the ActionPopup availability configuration.
     * This method can be overriden to implement custom conditions diverging from {@link #updateActionbar(info.magnolia.ui.actionbar.ActionbarPresenter)}.
     */
    private void updateActionPopup(ActionPopup actionPopup) {

        actionPopup.removeAllItems();

        BrowserSubAppDescriptor subAppDescriptor = (BrowserSubAppDescriptor) getSubAppContext().getSubAppDescriptor();
        ActionbarDefinition actionbarDefinition = subAppDescriptor.getActionbar();
        if (actionbarDefinition == null) {
            return;
        }
        List<ActionbarSectionDefinition> sections = actionbarDefinition.getSections();
        List<Item> items = getSelectedItems();

        // Figure out which section to show, only one
        ActionbarSectionDefinition sectionDefinition = getVisibleSection(sections, items);

        // If there no section matched the selection we just hide everything
        if (sectionDefinition == null) {
            return;
        }

        // Evaluate availability of each action within the section
        ContextMenu.ContextMenuItem menuItem = null;
        for (ActionbarGroupDefinition groupDefinition : sectionDefinition.getGroups()) {
            for (ActionbarItemDefinition itemDefinition : groupDefinition.getItems()) {

                String actionName = itemDefinition.getName();
                menuItem = addActionPopupItem(subAppDescriptor, actionPopup, itemDefinition, items);
                menuItem.setEnabled(actionExecutor.isAvailable(actionName, items.toArray(new Item[items.size()])));
            }

            // Add group separator.
            if (menuItem != null) {
                menuItem.setSeparatorVisible(true);
            }
        }
        if (menuItem != null) {
            menuItem.setSeparatorVisible(false);
        }
    }

    protected List<Item> getSelectedItems() {
        return browser.getSelectedItems();
    }

    /**
     * Add an additional menu item on the actionPopup.
     */
    private ContextMenu.ContextMenuItem addActionPopupItem(BrowserSubAppDescriptor subAppDescriptor, ActionPopup actionPopup, ActionbarItemDefinition itemDefinition, List<Item> items) {
        String actionName = itemDefinition.getName();

        ActionDefinition action = subAppDescriptor.getActions().get(actionName);
        String label = action.getLabel();
        String iconFontCode = ActionPopup.ICON_FONT_CODE + action.getIcon();
        ExternalResource iconFontResource = new ExternalResource(iconFontCode);
        ContextMenu.ContextMenuItem menuItem = actionPopup.addItem(label, iconFontResource);
        // Set data variable so that the event handler can determine which action to launch.
        menuItem.setData(actionName);

        return menuItem;
    }

    /**
     * Update the items in the actionbar based on the selected item and the action availability configuration.
     * This method can be overriden to implement custom conditions diverging from {@link #updateActionPopup(info.magnolia.ui.vaadin.actionbar.ActionPopup)}.
     * 
     * @see #restoreBrowser(BrowserLocation)
     * @see #locationChanged(Location)
     * @see ActionbarPresenter
     */
    public void updateActionbar(ActionbarPresenter actionbar) {

        BrowserSubAppDescriptor subAppDescriptor = (BrowserSubAppDescriptor) getSubAppContext().getSubAppDescriptor();
        ActionbarDefinition actionbarDefinition = subAppDescriptor.getActionbar();
        if (actionbarDefinition == null) {
            return;
        }
        List<ActionbarSectionDefinition> sections = actionbarDefinition.getSections();

        List<Item> items = getSelectedItems();

        // Figure out which section to show, only one
        ActionbarSectionDefinition sectionDefinition = getVisibleSection(sections, items);

        // If there no section matched the selection we just hide everything
        if (sectionDefinition == null) {
            for (ActionbarSectionDefinition section : sections) {
                actionbar.hideSection(section.getName());
            }
            return;
        }

        // Hide all other sections
        for (ActionbarSectionDefinition section : sections) {
            if (section != sectionDefinition) {
                actionbar.hideSection(section.getName());
            }
        }

        // Show our section
        actionbar.showSection(sectionDefinition.getName());

        // Evaluate availability of each action within the section
        for (ActionbarGroupDefinition groupDefinition : sectionDefinition.getGroups()) {
            for (ActionbarItemDefinition itemDefinition : groupDefinition.getItems()) {

                String actionName = itemDefinition.getName();
                if (actionExecutor.isAvailable(actionName, items.toArray(new Item[items.size()]))) {
                    actionbar.enable(actionName);
                } else {
                    actionbar.disable(actionName);
                }
            }
        }

    }

    private ActionbarSectionDefinition getVisibleSection(List<ActionbarSectionDefinition> sections, List<Item> items) {
        for (ActionbarSectionDefinition section : sections) {
            if (isSectionVisible(section, items))
                return section;
        }
        return null;
    }

    private boolean isSectionVisible(ActionbarSectionDefinition section, List<Item> items) {
        AvailabilityDefinition availability = section.getAvailability();

        // Validate that the user has all required roles - only once
        if (!availability.getAccess().hasAccess(MgnlContext.getUser())) {
            return false;
        }

        if (items != null) {
            // section must be visible for all items
            for (Item item : items) {
                if (!isSectionVisible(section, item)) {
                    return false;
                }
            }
        }

        return true;
    }

    protected boolean isSectionVisible(ActionbarSectionDefinition section, Item item) {
        AvailabilityDefinition availability = section.getAvailability();

        // if a rule class is set, verify it first
        if ((availability.getRuleClass() != null)) {
            // if the rule class cannot be instantiated, or the rule returns false
            AvailabilityRule rule = componentProvider.newInstance(availability.getRuleClass());
            if (rule == null || !rule.isAvailable(item)) {
                return false;
            }
        }

        return verifyAvailability(item, availability);
    }


    protected final BrowserPresenterBase getBrowser() {
        return browser;
    }

    /**
     * The default implementation selects the path in the current workspace and updates the available actions in the actionbar.
     */
    @Override
    public void locationChanged(final Location location) {
        super.locationChanged(location);
        restoreBrowser(BrowserLocation.wrap(location));
    }

    /**
     * Wraps the current DefaultLocation in a {@link BrowserLocation}. Providing getter and setters for used parameters.
     */
    @Override
    public BrowserLocation getCurrentLocation() {
        return BrowserLocation.wrap(super.getCurrentLocation());
    }

    /*
     * Registers general purpose handlers for the following events:
     * <ul>
     * <li> {@link ItemSelectedEvent}
     * <li> {@link ViewTypeChangedEvent}
     * <li> {@link SearchEvent}
     * </ul>
     */
    private void registerSubAppEventsHandlers(final EventBus subAppEventBus, final BrowserSubAppBase subApp) {
        final ActionbarPresenter actionbar = subApp.getBrowser().getActionbarPresenter();
        subAppEventBus.addHandler(SelectionChangedEvent.class, new SelectionChangedEvent.Handler() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                handleSelectionChange(event.getItemIds(), actionbar);
            }
        });

        subAppEventBus.addHandler(ItemRightClickedEvent.class, new ItemRightClickedEvent.Handler() {

            @Override
            public void onItemRightClicked(ItemRightClickedEvent event) {
                showActionPopup(event.getItemId(), event.getClickX(), event.getClickY());
            }
        });

        subAppEventBus.addHandler(ViewTypeChangedEvent.class, new ViewTypeChangedEvent.Handler() {

            @Override
            public void onViewChanged(ViewTypeChangedEvent event) {
                BrowserLocation location = getCurrentLocation();
                // remove search term from fragment when switching back
                if (location.getViewType().equals(SearchPresenterDefinition.VIEW_TYPE)
                        && !event.getViewType().equals(SearchPresenterDefinition.VIEW_TYPE)) {
                    location.updateQuery("");
                }
                location.updateViewType(event.getViewType());
                getAppContext().updateSubAppLocation(getSubAppContext(), location);
                updateActionbar(actionbar);
            }
        });

        subAppEventBus.addHandler(SearchEvent.class, new SearchEvent.Handler() {

            @Override
            public void onSearch(SearchEvent event) {
                BrowserLocation location = getCurrentLocation();
                if (StringUtils.isNotBlank(event.getSearchExpression())) {
                    location.updateViewType(SearchPresenterDefinition.VIEW_TYPE);
                }
                location.updateQuery(event.getSearchExpression());
                getAppContext().updateSubAppLocation(getSubAppContext(), location);
                updateActionbar(actionbar);
            }
        });
    }

    /**
     * TODO call applySelectionToLocation with proper parameters (convert id to string value)
     * @param selectionIds
     * @param actionbar
     */
    private void handleSelectionChange(Set<Object> selectionIds, ActionbarPresenter actionbar) {
        BrowserLocation location = getCurrentLocation();
        applySelectionToLocation(location, selectionIds.isEmpty() ? "" : selectionIds.iterator().next());
        getAppContext().updateSubAppLocation(getSubAppContext(), location);
        updateActionbar(actionbar);

    }


    protected WorkbenchDefinition getWorkbench() {
        return ((BrowserSubAppDescriptor) getSubAppContext().getSubAppDescriptor()).getWorkbench();
    }

}
