/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.list.ListPresenterDefinition;
import info.magnolia.ui.workbench.search.SearchPresenterDefinition;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;
import info.magnolia.ui.workbench.tree.TreeView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Implementation of the workbench view.
 */
public class WorkbenchViewImpl extends VerticalLayout implements WorkbenchView, Serializable {

    private final CssLayout toolBar = new CssLayout();

    private final CssLayout viewModes = new CssLayout();

    protected final Panel keyboardEventPanel;

    private StatusBarView statusBar;

    private Map<String, ContentView> contentViews = new HashMap<String, ContentView>();

    private Map<String, Button> contentViewsButton = new HashMap<String, Button>();

    private String currentViewType = TreePresenterDefinition.VIEW_TYPE;

    /**
     * for going back from search view if search expression is empty.
     */
    private String previousViewType = currentViewType;

    private WorkbenchView.Listener listener;

    private final SimpleTranslator i18n;

    @Inject
    public WorkbenchViewImpl(SimpleTranslator i18n) {
        this.i18n = i18n;

        setSizeFull();
        setMargin(new MarginInfo(true, false, false, true));
        addStyleName("workbench");

        viewModes.setStyleName("view-modes");

        toolBar.addStyleName("toolbar");
        toolBar.setWidth(100.0F, Sizeable.Unit.PERCENTAGE);
        toolBar.addComponent(viewModes);

        addComponent(toolBar);
        setExpandRatio(toolBar, 0.0F);

        this.keyboardEventPanel = new Panel();
        this.keyboardEventPanel.setSizeFull();
        this.keyboardEventPanel.addStyleName("keyboard-panel");
        addComponent(keyboardEventPanel, 1);
        setExpandRatio(keyboardEventPanel, 1.0F);

        bindKeyboardHandlers();
    }

    public void bindKeyboardHandlers() {

        final ShortcutListener enterShortcut = new ShortcutListener("Enter shortcut", ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                getSelectedView().onShortcutKey(ShortcutAction.KeyCode.ENTER, null);
            }
        };
        keyboardEventPanel.addShortcutListener(enterShortcut);

        final ShortcutListener deleteShortcut = new ShortcutListener("Delete shortcut", ShortcutAction.KeyCode.DELETE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                getSelectedView().onShortcutKey(ShortcutAction.KeyCode.DELETE, null);
            }
        };
        // MGNLUI-2106 disable the delete shortcut until we apply it without disrupting inplace-editing
        // keyboardEventPanel.addShortcutListener(deleteShortcut);
    }

    @Override
    public void setSearchQuery(String query) {
        ((WorkbenchView.Listener) listener).onSetSearchQuery(query);
    }

    @Override
    public void addContentView(String viewType, ContentView view, ContentPresenterDefinition contentViewDefintion) {
        contentViews.put(viewType, view);

        if (view instanceof TreeView) {
            ((TreeView) view).setActionManager(keyboardEventPanel);
        }

        // display search-box only if both list and search content presenters are configured
        if (contentViews.containsKey(ListPresenterDefinition.VIEW_TYPE) && contentViews.containsKey(SearchPresenterDefinition.VIEW_TYPE)) {
            if (toolBar.getComponentCount() > 1) { // components > 1 because first component in the toolbar is switcher between tree/list view
                toolBar.getComponent(1).setVisible(true);
            }
        }

        if (contentViewDefintion instanceof SearchPresenterDefinition) {
            // do not add a view-type button for search
            return;
        }

        // set button
        Button button = buildButton(viewType, contentViewDefintion.getIcon(), contentViewDefintion.isActive());
        contentViewsButton.put(viewType, button);
        viewModes.addComponent(button);
        // set active
        if (contentViewDefintion.isActive()) {
            currentViewType = previousViewType = viewType;
        }
    }

    @Override
    public void setViewType(String type) {
        final Component c = contentViews.get(type).asVaadinComponent();

        keyboardEventPanel.setContent(c);

        if (!type.equals(SearchPresenterDefinition.VIEW_TYPE)) {
            previousViewType = type;
            setSearchQuery(null);
        }
        setViewTypeStyling(type);

        currentViewType = type;
    }

    private void fireViewTypeChangedEvent(String viewType) {
        this.listener.onViewTypeChanged(viewType);
    }

    @Override
    public void setStatusBarView(StatusBarView statusBar) {
        Component c = statusBar.asVaadinComponent();
        if (this.statusBar == null) {
            addComponent(c, getComponentCount()); // add last
        } else {
            replaceComponent(this.statusBar.asVaadinComponent(), c);
        }
        setExpandRatio(c, 0);
        this.statusBar = statusBar;
    }

    @Override
    public ContentView getSelectedView() {
        return contentViews.get(currentViewType);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void setListener(WorkbenchView.Listener listener) {
        this.listener = listener;
    }

    private Button buildButton(final String viewType, final String icon, final boolean active) {
        NativeButton button = new NativeButton(null, new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                fireViewTypeChangedEvent(viewType);
            }
        });
        button.setStyleName(BaseTheme.BUTTON_LINK);

        button.setHtmlContentAllowed(true);
        button.setCaption("<span class=\"" + icon + "\"></span><span class=\"view-type-arrow view-type-arrow-" + viewType + " icon-arrow2_n\"></span>");

        if (active) {
            button.addStyleName("active");
        }
        return button;
    }

    private void setViewTypeStyling(final String viewType) {
        for (Map.Entry<String, Button> entry : contentViewsButton.entrySet()) {
            entry.getValue().removeStyleName("active");
            if (entry.getKey().equals(viewType)) {
                entry.getValue().addStyleName("active");
            }
        }
        // search is a list view
        if (viewType.equals(SearchPresenterDefinition.VIEW_TYPE) && contentViews.containsKey(ListPresenterDefinition.VIEW_TYPE)) {
            contentViewsButton.get(ListPresenterDefinition.VIEW_TYPE).addStyleName("active");
        }
    }

    @Override
    public void setMultiselect(boolean multiselect) {
        for (String type : contentViews.keySet()) {
            contentViews.get(type).setMultiselect(multiselect);
        }
    }

    @Override
    public void addContentTool(View view) {
        toolBar.addComponent(view.asVaadinComponent(), toolBar.getComponentCount());
    }
}
