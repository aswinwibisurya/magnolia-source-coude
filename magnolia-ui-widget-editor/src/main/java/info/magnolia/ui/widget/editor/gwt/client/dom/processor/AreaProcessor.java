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
package info.magnolia.ui.widget.editor.gwt.client.dom.processor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.ui.widget.editor.gwt.client.dom.MgnlElement;
import info.magnolia.ui.widget.editor.gwt.client.model.Model;
import info.magnolia.ui.widget.editor.gwt.client.widget.controlbar.AreaBar;
import info.magnolia.ui.widget.editor.gwt.client.widget.controlbar.AreaEndBar;
import info.magnolia.ui.widget.editor.gwt.client.widget.placeholder.ComponentPlaceHolder;

import java.util.Map;

/**
 * Factory Class for MgnlElement processors.
 */
public class AreaProcessor extends AbstractMgnlElementProcessor {

    public AreaProcessor(Model model, EventBus eventBus, MgnlElement mgnlElement) {
        super(model, eventBus, mgnlElement);
    }

    @Override
    public void process() {
        AreaBar areaBar = null;

        if (hasControlBar(getMgnlElement().getAttributes())) {
            areaBar = new AreaBar(getModel(), getEventBus(), getMgnlElement());
            setEditBar(areaBar);
            attachWidget();

            if (hasComponentPlaceHolder(getMgnlElement().getAttributes())) {
                new ComponentPlaceHolder(getModel(), getEventBus(), getMgnlElement());
            }

            new AreaEndBar(getModel(), getMgnlElement());

        }

        else {

            GWT.log("Not creating areabar and area endbar for this element. Missing parameters. Will be deleted.");

            // if the area has no controls we, don't want it in the structure.

            // delete the element from the tree
            // set all child parents to parent
            getMgnlElement().delete();


            // remove it from the Model
            getModel().removeMgnlElement(getMgnlElement());
        }
    }

    private boolean hasComponentPlaceHolder(Map<String, String> attributes) {

        String type = attributes.get("type");


        String availableComponents = "";
        if(AreaDefinition.TYPE_NO_COMPONENT.equals(type)) {
            availableComponents = "";
        }
        else {
            availableComponents = attributes.get("availableComponents");
        }

        if (availableComponents.equals("")) {
            return false;
        }

        if (type.equals(AreaDefinition.TYPE_SINGLE) && !getMgnlElement().getComponents().isEmpty()) {
            return false;
        }
        return true;

    }

    private boolean hasControlBar(Map<String, String> attributes) {

        String type = attributes.get("type");
        String dialog = attributes.get("dialog");

        boolean editable = true;
        if (attributes.containsKey("editable")) {
            editable = Boolean.parseBoolean(attributes.get("editable"));
        }

        String availableComponents = "";
        if (!AreaDefinition.TYPE_NO_COMPONENT.equals(type)) {
            availableComponents = attributes.get("availableComponents");
        }

        boolean showAddButton = Boolean.parseBoolean(attributes.get("showAddButton"));
        boolean optional = Boolean.parseBoolean(attributes.get("optional"));

        // break no matter what follows
        if (!editable) {
            return false;
        }

        // area can be deleted or created
        if (optional) {
            return true;
        }

        else if (type.equals(AreaDefinition.TYPE_SINGLE)) {
            return true;
        }

        // can add components to area
        else if (showAddButton && !availableComponents.isEmpty()) {
            return true;
        }

        // area can be edited
        else if (dialog != null && !dialog.isEmpty()) {
            return true;
        }

        return false;
    }

}
