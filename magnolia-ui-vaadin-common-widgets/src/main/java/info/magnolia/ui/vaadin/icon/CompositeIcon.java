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
package info.magnolia.ui.vaadin.icon;

import info.magnolia.ui.vaadin.gwt.client.icon.VCompositeIcon;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;


/**
 * The CompositeIcon allows for layered icons, such as a warning sign, with optional outer shape
 * outline. It mostly serves as the base class for preset composite icons such as InfoIcon,
 * WarningIcon, etc.
 */
@ClientWidget(value = VCompositeIcon.class, loadStyle = LoadStyle.EAGER)
public abstract class CompositeIcon extends AbstractComponent {

    private final List<Icon> icons = new LinkedList<Icon>();

    protected CompositeIcon(Icon... icons) {
        this.icons.addAll(Arrays.asList(icons));
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        if (!icons.isEmpty()) {
            for (Icon icon : icons) {
                icon.paint(target);
            }
        }
    }

}
