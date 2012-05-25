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
package info.magnolia.m5vaadin.shell.gwt.client;

import info.magnolia.ui.widget.jquerywrapper.client.ui.Callbacks;
import info.magnolia.ui.widget.jquerywrapper.client.ui.JQueryWrapper;
import info.magnolia.ui.widget.jquerywrapper.client.ui.JQueryCallback;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.HTML;

/**
 * Simple notification object that pops up when warnings/errors occur.
 * @author apchelintcev
 *
 */
public class VShellMessage extends HTML {
    
    /**
     * Enumeration of possible message types.
     * @author apchelintcev
     *
     */
    public enum MessageType {
        WARNING,
        ERROR;
    }
    
    private static final String STYLE_NAME = "v-shell-notification";
    
    private  HandlerRegistration eventPreviewReg = null;
    
    private final MessageType type;
    
    private final String text;
    
    
    public VShellMessage(MessageType type, String text) {
        super();
        this.type = type;
        this.text = text;
        setStyleName(STYLE_NAME);
        construct();
    }
    
    private void construct() {
        final Element caption = DOM.createElement("b");
        switch (type) {
        case WARNING:
            addStyleName("warning");
            caption.setInnerHTML("Warning: ");
            break;
        case ERROR:
            addStyleName("error");
            caption.setInnerHTML("Error: ");
            break;
        }
        Element textWrapper = DOM.createSpan();
        textWrapper.setInnerText(text);
        getElement().appendChild(caption);
        getElement().appendChild(textWrapper);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        sinkEvents(Event.MOUSEEVENTS);
        eventPreviewReg = Event.addNativePreviewHandler(new NativePreviewHandler() {        
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                if (event.getTypeInt() == Event.ONCLICK) {
                    final Element targetEl = event.getNativeEvent().getEventTarget().cast();
                    if (getElement().isOrHasChild(targetEl)/* && type == MessageType.WARNING*/) {
                        hide();
                    }
                }
            }
        });
    }
    
    public void show() {
        getElement().getStyle().setDisplay(Display.NONE);
        JQueryWrapper.select(getElement()).slideDown(300, null);
    }
    
    public void hide() {
        JQueryWrapper.select(getElement()).slideUp(300, Callbacks.create(new JQueryCallback() {
            @Override
            public void execute(JQueryWrapper query) {
                removeFromParent();   
            }
        }));
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        if (eventPreviewReg != null) {
            eventPreviewReg.removeHandler();
        }
    }
}
