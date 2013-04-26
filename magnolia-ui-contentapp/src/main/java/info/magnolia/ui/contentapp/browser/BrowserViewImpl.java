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
package info.magnolia.ui.contentapp.browser;

import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.workbench.WorkbenchView;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;

/**
 * Implementation of {@link BrowserView}.
 */
public class BrowserViewImpl extends HorizontalLayout implements BrowserView {

    private ActionbarView actionBar;

    private final CssLayout actionBarWrapper = new CssLayout();

    private WorkbenchView workbench;

    public BrowserViewImpl() {
        setSizeFull();
        setMargin(false);
        setSpacing(true);
        addStyleName("browser");

        actionBarWrapper.setHeight(100, Unit.PERCENTAGE);
        actionBarWrapper.addStyleName("actionbar");
        addComponent(actionBarWrapper);
        setExpandRatio(actionBarWrapper, 0);
    }

    @Override
    public void setActionbarView(final ActionbarView actionBar) {
        Component c = actionBar.asVaadinComponent();
        Component old = actionBarWrapper.getComponentCount() != 0 ? actionBarWrapper.getComponent(0) : null;
        if (old == null) {
            actionBarWrapper.addComponent(c);
        } else {
            actionBarWrapper.replaceComponent(old, c);
        }
        this.actionBar = actionBar;
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void setWorkbenchView(WorkbenchView workbench) {
        if (this.workbench == null) {
            addComponent(workbench.asVaadinComponent(), 0); // add as first
        } else {
            replaceComponent(this.workbench.asVaadinComponent(), workbench.asVaadinComponent());
        }
        setExpandRatio(workbench.asVaadinComponent(), 1);
        this.workbench = workbench;
    }

    @Override
    public WorkbenchView getWorkbenchView() {
        return workbench;
    }
}
