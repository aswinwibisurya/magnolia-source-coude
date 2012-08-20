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
package info.magnolia.ui.widget.editor.gwt.client.widget.controlbar;


import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.ui.FlowPanel;
import info.magnolia.ui.widget.editor.gwt.client.dom.MgnlElement;
import info.magnolia.ui.widget.editor.gwt.client.model.Model;


/**
 * Area bar.
 */
public class AreaEndBar extends FlowPanel {

    private Model model;
    private MgnlElement mgnlElement;

    private final static String FOCUS_CLASSNAME = "focus";
    private final static String CHILD_FOCUS_CLASSNAME = "childFocus";

    public AreaEndBar(Model model, MgnlElement mgnlElement) {
        this.model = model;

        this.mgnlElement = mgnlElement;
        this.setStyleName("mgnlEditor mgnlEditorBar");
        this.addStyleName("area");
        this.addStyleName("end");

        setVisible(false);

        attach();

    }

    public void attach() {
        if (mgnlElement.getFirstElement() != null && mgnlElement.getFirstElement() == mgnlElement.getLastElement()) {
            attach(mgnlElement);
        }
        else {
            attach(mgnlElement.getEndComment().getElement());
        }
    }

    public void attach(MgnlElement mgnlElement) {
        Element element = mgnlElement.getFirstElement();
        if (element != null) {
            element.appendChild(getElement());
        }
        onAttach();
    }

    public void attach(Element element) {
        final Node parentNode = element.getParentNode();
        parentNode.insertBefore(getElement(), element);
        onAttach();
    }

    @Override
    protected void onAttach() {
        getModel().addElements(mgnlElement, getElement());
        getModel().addAreaEndBar(mgnlElement, this);
        super.onAttach();
    }

    public Model getModel() {
        return model;
    }

    public void setFocus(boolean focus, boolean child) {
        String className = (child) ? "childFocus" : "focus";
        if (focus) {
            addStyleName(className);
        }
        else {
            removeStyleName(className);
        }
    }

    public void removeFocus() {
        removeStyleName(FOCUS_CLASSNAME);
        removeStyleName(CHILD_FOCUS_CLASSNAME);
    }

    public void setFocus(boolean child) {
        String className = (child) ? CHILD_FOCUS_CLASSNAME : FOCUS_CLASSNAME;
        addStyleName(className);
    }
}
