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
package info.magnolia.ui.widget.dialog.gwt.client;

import info.magnolia.ui.vaadin.integration.widget.client.tabsheet.VShellTabSheet;
import info.magnolia.ui.vaadin.integration.widget.client.tabsheet.VShellTabSheetView;
import info.magnolia.ui.widget.dialog.gwt.client.dialoglayout.HelpAccessibilityEvent;
import info.magnolia.ui.widget.dialog.gwt.client.dialoglayout.VHelpAccessibilityNotifier;

import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * VTabDialog.
 */
public class VTabDialog extends VShellTabSheet implements VTabDialogView.Presenter, VHelpAccessibilityNotifier {

    private boolean isHelpAccessible = false;
    
    private VHelpAccessibilityNotifier.Delegate delegate = new Delegate();
    
    @Override
    protected VTabDialogView getView() {
        return (VTabDialogView)super.getView();
    }
    
    @Override
    protected ClientSideProxy createProxy() {
        final ClientSideProxy proxy = super.createProxy();
        proxy.register("addAction", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                final String name = String.valueOf(params[0]);
                final String label = String.valueOf(params[1]);
                getView().addAction(name, label);
            }
        });
        proxy.register("setDescription", new Method() {
            @Override
            public void invoke(String methodName, Object[] params) {
                final String description = String.valueOf(params[0]);
                getView().setDescription(description);
            }
        });
        return proxy; 
    }

    @Override
    protected VShellTabSheetView createView() {
        return new VTabDialogViewImpl(getEventBus(), this);
    }

    @Override
    public void fireAction(String action) {
        getProxy().call("fireAction", action);
    }

    @Override
    public void closeDialog() {
        getProxy().call("closeDialog");
    }
    
    @Override
    public HandlerRegistration addHelpAccessibilityHandler(HelpAccessibilityEvent.Handler handler) {
        return delegate.addHelpAccessibilityHandler(handler);
    }


    @Override
    public void changeHelpAccessibility(boolean isEnabled) {
        delegate.changeHelpAccessibility(isHelpAccessible);
    }


    @Override
    public void notifyOfHelpAccessibilityChange(boolean isAccessible) {
        isHelpAccessible = !isHelpAccessible;
        changeHelpAccessibility(isHelpAccessible);
    }
}
