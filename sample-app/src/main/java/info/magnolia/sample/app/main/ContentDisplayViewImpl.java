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
package info.magnolia.sample.app.main;

import info.magnolia.cms.i18n.MessagesUtil;

import javax.inject.Inject;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * View implementation for the content display.
 */
public class ContentDisplayViewImpl implements ContentDisplayView {

    private ContentDisplayView.Listener listener;
    private VerticalLayout layout;

    @Inject
    public ContentDisplayViewImpl() {
        layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(new Label(MessagesUtil.get("sample.app.contentDisplay.label.title", "mgnl-i18n.app-sample-messages")));
        layout.addComponent(new Label(MessagesUtil.get("sample.app.contentDisplay.label.description", "mgnl-i18n.app-sample-messages")));
    }

    @Override
    public void setListener(ContentDisplayView.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setResource(final String name) {
        layout.removeAllComponents();
        layout.addComponent(new Label(MessagesUtil.get("sample.app.contentDisplay.label.title", "mgnl-i18n.app-sample-messages")));
        layout.addComponent(new Label(MessagesUtil.get("sample.app.contentDisplay.label.displaying", "mgnl-i18n.app-sample-messages") + " " + name));
        layout.addComponent(new Button(MessagesUtil.get("sample.app.contentDisplay.button.newEditor", "mgnl-i18n.app-sample-messages"), new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                listener.onOpenInNewEditor();
            }
        }));
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }
}
