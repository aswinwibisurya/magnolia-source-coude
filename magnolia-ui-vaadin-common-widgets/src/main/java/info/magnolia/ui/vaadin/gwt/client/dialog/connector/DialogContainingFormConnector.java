/**
 * This file Copyright (c) 2010-2013 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.dialog.connector;

import info.magnolia.ui.vaadin.form.DialogContainingForm;
import info.magnolia.ui.vaadin.gwt.client.dialog.widget.BaseDialogView;
import info.magnolia.ui.vaadin.gwt.client.form.widget.FormViewImpl;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;

/**
 * Dialog that contains a form.
 */
@Connect(DialogContainingForm.class)
public class DialogContainingFormConnector extends BaseDialogConnector {

    private BaseDialogView view;

    @Override
    protected BaseDialogView createView() {
        this.view = super.createView();
        return this.view;
    }

    @Override
    protected void init() {
        super.init();
        getLayoutManager().addElementResizeListener(getWidget().getElement(), listener);

        Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                updateSize();
            }
        });
    }

    private final ElementResizeListener listener = new ElementResizeListener() {
        public void onElementResize(ElementResizeEvent e) {
            updateSize();
        }
    };

    @Override
    public void onUnregister() {
        getLayoutManager().removeElementResizeListener(getWidget().getElement(), listener);
    }
    
    private void updateSize() {
        Widget content = getContent().getWidget();
        if (content instanceof FormViewImpl) {
            FormViewImpl formview = (FormViewImpl) content;
            Element element = view.asWidget().getElement();
            NodeList<Node> childNodes = element.getChildNodes();
            int footerHeight = 0;
            int headerHeight = 0;

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.getItem(i);

                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    Element child = (Element) item;
                    if (child.getClassName().equalsIgnoreCase("dialog-footer")) {
                        footerHeight = child.getOffsetHeight();
                    } else if (child.getClassName().isEmpty()) {
                        headerHeight = child.getOffsetHeight();
                    }
                }
            }

            formview.setTabSheetMaxHeight(view.asWidget().getElement().getOffsetHeight() - footerHeight - headerHeight);
        }
    }
}
