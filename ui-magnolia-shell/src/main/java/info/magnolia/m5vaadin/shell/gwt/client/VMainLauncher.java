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
package info.magnolia.m5vaadin.shell.gwt.client;

import info.magnolia.m5vaadin.shell.gwt.client.event.AppActivatedEvent;
import info.magnolia.m5vaadin.shell.gwt.client.event.ShellAppNavigationEvent;
import info.magnolia.m5vaadin.shell.gwt.client.event.handler.ShellNavigationAdapter;
import info.magnolia.m5vaadin.shell.gwt.client.event.handler.ShellNavigationHandler;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.addon.jquerywrapper.client.ui.AnimationSettings;
import org.vaadin.addon.jquerywrapper.client.ui.JQueryWrapper;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Navigation bar.
 * @author apchelintcev
 *
 */
public class VMainLauncher extends FlowPanel {
    
    private final static int DIVET_ANIMATION_SPEED = 200;
    
    private final static String ID = "main-launcher";
    
    private HandlerRegistration activationHandlerRegistration;
    
    /**
     * Type of the "shell app" to be loaded.
     */
    public static enum ShellAppType {
        APPLAUNCHER("btn-appslauncher", "Apps"),
        PULSE("btn-pulse", "Pulse"),
        FAVORITE("btn-favorites", "Favorites");
        
        private String classId;
        
        private String caption;
        
        public String getCaption() {
            return caption;
        }
        
        public String getId() {
            return classId;
        }
        
        private ShellAppType(final String styleName, final String caption) {
            this.classId = styleName;
            this.caption = caption;
        }
        
        public static String getTypeByFragmentId(final String id) {
            for (final ShellAppType type : ShellAppType.values()) {
                if (type.name().equalsIgnoreCase(id)) {
                    return type.name();
                }
            }
            return ShellAppType.APPLAUNCHER.name();
        }

        public static ShellAppType resolveType(String id) {
            ShellAppType result = null;
            try {
                result = ShellAppType.valueOf(id);
            } catch (Exception e) {
                return ShellAppType.APPLAUNCHER;
            }
            return result;
        }
    };
  
    private int expandedHeight = 0;
    
    private final Element logoWrapper = DOM.createDiv();
    
    private final Element divetWrapper = DOM.createDiv();
    
    private Image logo = new Image(VShellImageBundle.BUNDLE.getLogo());
    
    private Image divet = new Image(VShellImageBundle.BUNDLE.getDivetGreen());
    
    private Map<ShellAppType, Widget> controlsMap = new EnumMap<ShellAppType, Widget>(ShellAppType.class);

    private final EventBus eventBus;
    
    public VMainLauncher(final EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        getElement().setId(ID);
        construct();
    }
    
    private void navigateToShellApp(final ShellAppType type) {
        eventBus.fireEvent(new ShellAppNavigationEvent(type, ""));
    }
    
    private void construct() {
        divetWrapper.setId("divet");
        logoWrapper.setId("logo");
        getElement().appendChild(logoWrapper);
        getElement().appendChild(divetWrapper);
        add(logo, logoWrapper);
        add(divet, divetWrapper);
        for (final ShellAppType appType : ShellAppType.values()) {
            final Widget w = new NavigatorButton(appType);
            controlsMap.put(appType, w);
            add(w);
        }
        divet.setVisible(false);
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        activationHandlerRegistration = eventBus.addHandler(AppActivatedEvent.TYPE, navHandler);
        expandedHeight = getOffsetHeight();
        getElement().getStyle().setDisplay(Display.NONE);
        JQueryWrapper.select(getElement()).slideDown(500, null);
    } 
    
    @Override
    protected void onUnload() {
        super.onUnload();
        if (activationHandlerRegistration != null) {
            activationHandlerRegistration.removeHandler();
        }
    } 
    
    final void updateDivet() {
        final ShellAppType type = getActiveShellType();
        if (type != null) {
            doUpdateDivetPosition(type, false);
        }  
    }

    private ShellAppType getActiveShellType() {
        final Iterator<Entry<ShellAppType, Widget>> it = controlsMap.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<ShellAppType, Widget> entry = it.next();
            if (entry.getValue().getStyleName().contains("active")) {
                return entry.getKey();
            }
        }
        return null;
    }

    private class NavigatorButton extends Widget {
        
        private Element buttonWrapper = DOM.createDiv();
        
        public NavigatorButton(final ShellAppType type) {
            super();
            setElement(buttonWrapper);
            buttonWrapper.setInnerHTML("<span>" + type.getCaption() + "</span>");
            getElement().setId(type.getId());
            addStyleName("btn-shell");
            DOM.sinkEvents(getElement(), Event.MOUSEEVENTS);
            addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {                    
                    navigateToShellApp(type);
                }
            }, ClickEvent.getType());
        }
    };
    
    protected void activateControl(final ShellAppType type) {
        final ShellAppType currentActive = getActiveShellType();
        if (currentActive != null) {
            controlsMap.get(currentActive).removeStyleName("active");
        }
        doUpdateDivetPosition(type, currentActive != null);
        final Widget w = controlsMap.get(type);
        w.addStyleName("active");
    }

    private void doUpdateDivetPosition(final ShellAppType type, boolean animated) {
        final Widget w = controlsMap.get(type);
        int divetPos = w.getAbsoluteLeft() + (w.getOffsetWidth() / 2) - divetWrapper.getOffsetWidth() / 2;
        divet.setVisible(true);
        switch (type) {
        case APPLAUNCHER:
            divet.setResource(VShellImageBundle.BUNDLE.getDivetGreen());
            break;
        default:
            divet.setResource(VShellImageBundle.BUNDLE.getDivetWhite());
            break;
        }
        if (animated) {
            final AnimationSettings settings = new AnimationSettings();
            settings.setProperty("left", divetPos);
            JQueryWrapper.select(divetWrapper).animate(DIVET_ANIMATION_SPEED, settings);            
        } else {
            divetWrapper.getStyle().setLeft(divetPos, Unit.PX);
        }

    }

    void deactivateControls() {
        divet.setVisible(false);
        for (final ShellAppType appType : ShellAppType.values()) {
            controlsMap.get(appType).removeStyleName("active");
        }
    }

    public int getExpandedHeight() {
        return expandedHeight;
    }
    
    private ShellNavigationHandler navHandler = new ShellNavigationAdapter() {
        @Override
        public void onAppActivated(AppActivatedEvent event) {
            if (event.isShellApp()) {
                activateControl(ShellAppType.valueOf(event.getAppTitle()));
            }
        }
    };   
}
