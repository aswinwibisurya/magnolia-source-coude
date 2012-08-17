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
package info.magnolia.ui.widget.magnoliashell.gwt.client.viewport;

import info.magnolia.ui.widget.magnoliashell.gwt.client.event.ViewportCloseEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;


/**
 * Client side implementation of Apps viewport.
 */
public class VAppsViewport extends VShellViewport {

    private final VAppPreloader preloader = new VAppPreloader();

    private final Element closeWrapper = DOM.createDiv();

    public VAppsViewport() {
        super();
        setForceContentAlign(false);
        setContentAnimationDelegate(ContentAnimationDelegate.FadingDelegate);
        final Element closeButton = DOM.createButton();
        closeWrapper.setClassName("close");
        closeButton.setClassName("action-close");
        closeWrapper.appendChild(closeButton);
        addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (closeWrapper.isOrHasChild((Element) event.getNativeEvent().getEventTarget().cast())) {
                    getEventBus().fireEvent(new ViewportCloseEvent(VAppsViewport.this));
                }
            }
        }, ClickEvent.getType());
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        preloader.getElement().getStyle().setZIndex(299);
        super.updateFromUIDL(uidl, client);
        if (RootPanel.get().getWidgetIndex(preloader) >= 0) {
            new Timer() {

                @Override
                public void run() {
                    RootPanel.get().remove(preloader);
                }
            }.schedule(1000);
        }
    }

    /**
     * Called when the transition of preloader is finished.
     */
    public interface PreloaderCallback {

        void onPreloaderShown(String appName);
    }

    public void showAppPreloader(final String appName, final PreloaderCallback callback) {
        hideEntireContents();
        preloader.setCaption(appName);
        RootPanel.get().add(preloader);
        preloader.addStyleName("zoom-in");
        new Timer() {

            @Override
            public void run() {
                callback.onPreloaderShown(appName);
            }
        }.schedule(500);
    }

    @Override
    protected void setWidgetVisible(Widget w) {
        super.setWidgetVisible(w);
        w.getElement().appendChild(closeWrapper);
    }

    /**
     * Preloader of the apps.
     */
    class VAppPreloader extends Widget {

        private final Element root = DOM.createDiv();

        private final Element navigator = DOM.createElement("ul");

        private final Element tab = DOM.createElement("li");

        private final Element captionSpan = DOM.createSpan();

        public VAppPreloader() {
            super();
            setElement(root);
            setStyleName("v-shell-vieport v-shell-tabsheet");
            navigator.addClassName("nav nav-tabs single-tab");
            tab.addClassName("clearfix active");
            captionSpan.setClassName("tab-title");

            tab.appendChild(captionSpan);
            navigator.appendChild(tab);
            root.appendChild(navigator);

            Element preloadingScreen = DOM.createDiv();
            preloadingScreen.addClassName("loading-screen");
            preloadingScreen.setInnerHTML("<div class=\"loading-message-wrapper\"> " +
                "<div class=\"loading-message\"><div class=\"spinner\"></div> Loading </div></div>");
            root.appendChild(preloadingScreen);
        }

        @Override
        protected void onLoad() {
            super.onLoad();
            getElement().getStyle().setZIndex(301);
        }

        public void setCaption(String caption) {
            captionSpan.setInnerHTML(caption);
        }
    }
}
