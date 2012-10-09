/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.vaadin.widget.tabsheet.client;


import info.magnolia.ui.vaadin.integration.widget.client.loading.LoadingPane;
import info.magnolia.ui.vaadin.widget.tabsheet.client.event.ActiveTabChangedEvent;
import info.magnolia.ui.vaadin.widget.tabsheet.client.util.CollectionUtil;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.Util;

/**
 * VShellTabSheetViewImpl.
 *
 * Contains the tabs at the top (VMagnoliaTabNavigator), and the tabs themselves. The tabs are all contained in a ScrollPanel,
 * this enables a single showing tab to be scrolled - or the contents of all the tabs to be scrolled together when they are stacked in the
 * 'ShowAllTabs' mode.
 */
public class VMagnoliaTabSheetViewImpl extends FlowPanel implements VMagnoliaTabSheetView {

    private ScrollPanel scroller = new ScrollPanel();

    private FlowPanel tabPanel = new FlowPanel();

    private VMagnoliaTabNavigator tabContainer;

    private VMagnoliaTab activeTab = null;

    private Presenter presenter;

    private boolean isActiveTabFullscreen = false;

    private final List<VMagnoliaTab> tabs = new LinkedList<VMagnoliaTab>();

    private final LoadingPane loadingPane = new LoadingPane();

    public VMagnoliaTabSheetViewImpl(EventBus eventBus, Presenter presenter) {
        super();
        this.presenter = presenter;
        this.tabContainer =  new VMagnoliaTabNavigator(eventBus);
        addStyleName("v-shell-tabsheet");
        scroller.addStyleName("v-shell-tabsheet-scroller");
        tabPanel.addStyleName("v-shell-tabsheet-tab-wrapper");
        add(tabContainer);
        add(scroller);
        scroller.setWidget(tabPanel);

        loadingPane.appendTo(tabPanel);
        loadingPane.hide();
    }





    @Override
    public VMagnoliaTabNavigator getTabContainer() {
        return tabContainer;
    }

    @Override
    public void removeTab(VMagnoliaTab tabToOrphan) {
        if (activeTab == tabToOrphan) {
            final VMagnoliaTab nextTab = CollectionUtil.getNext(getTabs(), tabToOrphan);
            if (nextTab != null) {
                setActiveTab(nextTab);
            }
        }
        getTabs().remove(tabToOrphan);
        tabPanel.remove(tabToOrphan);
    }

    //private int count = 0;

    @Override
    public void setActiveTab(final VMagnoliaTab tab) {
        loadingPane.show();

        // Hide all tabs
        showAllTabContents(false);

        tab.getElement().getStyle().setDisplay(Display.BLOCK);
        activeTab = tab;

        // updateLayout in a 10ms Timer so that the browser has a chance to show the indicator before the processing begins.
        new Timer() {
            @Override
            public void run() {
                presenter.updateLayoutOfActiveTab();
                loadingPane.hide();
                fireEvent(new ActiveTabChangedEvent(tab));
            }
        }.schedule(10);



    }



    @Override
    public VMagnoliaTab getTabById(String tabId) {
        for (final VMagnoliaTab tab : getTabs()) {
            if (tab.getTabId().equals(tabId)) {
                return tab;
            }
        }
        return null;
    }

    @Override
    public List<VMagnoliaTab> getTabs() {
        return tabs;
    }

    @Override
    public void showAllTabContents(boolean visible) {
        Display display = (visible) ? Display.BLOCK : Display.NONE;
        for (VMagnoliaTab tab : getTabs()) {
            tab.getElement().getStyle().setDisplay(display);
        }
        if (visible) {
            fireEvent(new ActiveTabChangedEvent(true, false));
        }
    }

    @Override
    public void updateTab(VMagnoliaTab tab) {
        if (!tabs.contains(tab)) {
            getTabs().add(tab);
            tabPanel.add((Widget) tab);
            fireEvent(new TabSetChangedEvent((VMagnoliaTabSheet)presenter));
        }
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        if (!isActiveTabFullscreen && !height.isEmpty()) {
            int heightPx = JQueryWrapper.parseInt(height);
            int scrollerHeight = Math.max(heightPx - tabContainer.getOffsetHeight(), 0);
            scroller.setHeight(scrollerHeight + "px");
        } else {
            scroller.setHeight(RootPanel.get().getOffsetHeight() + "px");
        }
        Util.runWebkitOverflowAutoFix(scroller.getElement());
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        fireEvent(new TabSetChangedEvent((VMagnoliaTabSheet)presenter));
    }

    @Override
    public HandlerRegistration addScrollHandler(ScrollHandler handler) {
        return scroller.addScrollHandler(handler);
    }

    @Override
    public VMagnoliaTab getActiveTab() {
        return activeTab;
    }

    @Override
    public Widget getScroller() {
        return scroller;
    }

    @Override
    public void setShowActiveTabFullscreen(boolean isFullscreen) {
        this.isActiveTabFullscreen = isFullscreen;
        // apply fullscreen style to top of dom tree so that all elements can react to it. ie main-launcher.
        if (isFullscreen) {
            RootPanel.get().addStyleName("fullscreen");
            scroller.setHeight(RootPanel.get().getOffsetHeight() + "px");
        } else {
            RootPanel.get().removeStyleName("fullscreen");
            int scrollerHeight = getOffsetHeight() - tabContainer.getOffsetHeight();
            scroller.setHeight(scrollerHeight + "px");
        }
    }

    @Override
    public int getTabHeight(VMagnoliaTab tab) {
        if (!isActiveTabFullscreen || tab != getActiveTab()) {
            return getOffsetHeight() - tabContainer.getOffsetHeight();
        } else {
            return RootPanel.get().getOffsetHeight();
        }
    }

}
