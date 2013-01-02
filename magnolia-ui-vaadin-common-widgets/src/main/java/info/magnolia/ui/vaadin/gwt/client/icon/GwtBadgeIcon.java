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
package info.magnolia.ui.vaadin.gwt.client.icon;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * The GwtBadgeIcon widget.
 */
public class GwtBadgeIcon extends Widget {

    private static final String CLASSNAME = "badge-icon";

    private static final int SIZE_DEFAULT = 16;

    private final Element root = DOM.createSpan();

    private final Element text = DOM.createSpan();

    public GwtBadgeIcon() {
        setElement(root);
        setStylePrimaryName(CLASSNAME);
        text.addClassName("text");
        root.appendChild(text);
    }

    public void updateValue(int value) {
        String s = String.valueOf(value);
        if (value == 0) {
            setVisible(false);
        } else {
            setVisible(true);
        }
        if (value > 99) {
            s = "99+";
        }
        text.setInnerHTML(s);
    }

    public void updateSize(int value) {
        root.getStyle().setFontSize(value, Unit.PX);
    }

    public void updateFillColor(String value) {
        root.getStyle().setBackgroundColor(value);
    }

    public void updateStrokeColor(String value) {
        root.getStyle().setColor(value);
        root.getStyle().setBorderColor(value);
    }

    public void updateOutline(boolean outline) {
        if (outline) {
            root.getStyle().setBorderWidth(0.13, Unit.EM);
        } else {
            root.getStyle().clearBorderWidth();
        }
    }

}