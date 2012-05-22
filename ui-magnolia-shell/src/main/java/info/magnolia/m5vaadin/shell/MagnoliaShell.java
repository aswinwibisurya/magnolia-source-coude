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
package info.magnolia.m5vaadin.shell;

import info.magnolia.m5vaadin.IsVaadinComponent;
import info.magnolia.ui.framework.event.HandlerRegistration;
import info.magnolia.ui.framework.shell.ConfirmationHandler;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;
import info.magnolia.ui.framework.shell.Shell;

import javax.inject.Singleton;

import com.vaadin.terminal.ExternalResource;

/**
 * Actual implementation of MagnoliaShell that can serve actual apps and shell apps.
 * @author apchelintcev
 *
 */
@SuppressWarnings("serial")
@Singleton
public class MagnoliaShell extends BaseMagnoliaShell implements Shell {

    @Override
    public void askForConfirmation(String message, ConfirmationHandler listener) {
    }

    @Override
    public void showNotification(String message) {
        showWarning(message);
    }

    @Override
    public void showError(String message, Exception e) {
        showError(message);
    }

    @Override
    public void openWindow(String uri, String windowName) {
        getWindow().open(new ExternalResource(uri), windowName);
    }

    @Override
    public String getFragment() {
        final ShellViewport activeViewport = getActiveViewport(); 
        return activeViewport == null ? "" : activeViewport.getCurrentShellFragment();
    }

    @Override
    public void setFragment(String fragment) {
        final ShellViewport activeViewport = getActiveViewport();
        if (activeViewport.isShellAppViewport()) {
            proxy.call("shellAppActivated", fragment, activeViewport.getViewName());
        } else {
            proxy.call("appActivated", fragment, activeViewport.getViewName());
        }
    }

    @Override
    public HandlerRegistration addFragmentChangedHandler(final FragmentChangedHandler handler) {
        super.addFragmentChangedHanlder(handler);
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                removeFragmentChangedHanlder(handler);
            }
        };
    }
    
    @Override
    public Shell createSubShell(String id) {
        throw new UnsupportedOperationException("MagnoliaShell is not capable of opening the subshells.");
    }

    public void openDialog(IsVaadinComponent dialog) {
        addDialog(dialog.asVaadinComponent());
    }
    
    public void removeDialog(IsVaadinComponent dialog) {
        removeDialog(dialog.asVaadinComponent());
    }
}
