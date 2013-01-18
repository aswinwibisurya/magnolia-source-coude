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
package info.magnolia.ui.app.showcase.main;

import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.vaadin.tabsheet.MagnoliaTabSheet;

import javax.inject.Inject;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

/**
 * View implementation of the main tab in showcase app.
 */
public class ShowcaseMainViewImpl implements ShowcaseMainView {

    @SuppressWarnings("unused")
    private Listener listener;

    private final ComponentContainer tabForms;

    private final ComponentContainer tabVaadin;

    private final ComponentContainer tabUnsupported;

    private final Layout root;

    @Inject
    public ShowcaseMainViewImpl() {
        root = new CssLayout();
        root.setSizeFull();
        root.setWidth(900, Unit.PIXELS);
        root.setStyleName("small-app");

        MagnoliaTabSheet tabsheet = new MagnoliaTabSheet();
        tabsheet.setSizeFull();
        tabForms = tabsheet.addTabStub("Form Fields");
        tabVaadin = tabsheet.addTabStub("Vaadin Fields");
        tabUnsupported = tabsheet.addTabStub("Unsupported Vaadin Fields");

        tabsheet.addStyleName("small-app-panel");

        Label explanation = new Label("Showcase app shows what components there are available for app developers");
        root.addComponent(explanation);
        root.addComponent(tabsheet);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        return root;
    }

    @Override
    public void setFormFieldView(View forms) {
        tabForms.addComponent(forms.asVaadinComponent());
    }

    @Override
    public void setVaadinView(View vaadincomponents) {
        tabVaadin.addComponent(vaadincomponents.asVaadinComponent());
    }

    @Override
    public void setUnsupportedVaadinView(View unsupported) {
        tabUnsupported.addComponent(unsupported.asVaadinComponent());
    }
}
