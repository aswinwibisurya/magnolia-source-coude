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
package info.magnolia.ui.widget.actionbar.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;


/**
 * The client-side widget for a section of the action bar.
 */
public class VActionbarSection extends FlowPanel {

    public static final String CLASSNAME = "v-actionbar-section";

    public static final String TITLE_TAGNAME = "h3";

    private final Element header = DOM.createElement("header");

    private final Element heading = DOM.createElement("h3");

    private final VActionbarSectionJSO data;

    private final Map<String, VActionbarGroup> groups = new HashMap<String, VActionbarGroup>();

    /**
     * Instantiates a new action bar section with given data.
     * 
     * @param data the data
     */
    public VActionbarSection(VActionbarSectionJSO data) {
        this.data = data;

        // construct DOM
        setStyleName(CLASSNAME);
        getElement().appendChild(header);
        heading.addClassName("v-actionbar-section-title");
        header.appendChild(heading);

        update();
    }

    public String getName() {
        return data.getName();
    }

    public Map<String, VActionbarGroup> getGroups() {
        return groups;
    }

    public void addGroup(VActionbarGroup group) {
        groups.put(group.getName(), group);
        add(group);
    }

    public void update() {
        heading.setInnerText(data.getName());
        heading.setInnerText(data.getCaption());
        setVisible(data.isVisible());
    }

}
