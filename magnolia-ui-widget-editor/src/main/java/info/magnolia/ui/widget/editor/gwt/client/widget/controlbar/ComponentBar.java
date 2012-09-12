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

import static info.magnolia.ui.widget.editor.gwt.client.jsni.JavascriptUtils.getI18nMessage;
import info.magnolia.ui.widget.editor.gwt.client.dom.MgnlElement;
import info.magnolia.ui.widget.editor.gwt.client.event.DeleteComponentEvent;
import info.magnolia.ui.widget.editor.gwt.client.event.EditComponentEvent;
import info.magnolia.ui.widget.editor.gwt.client.model.Model;
import info.magnolia.ui.widget.editor.gwt.client.widget.dnd.DragAndDrop;
import info.magnolia.ui.widget.editor.gwt.client.widget.dnd.LegacyDragAndDrop;

import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;


/**
 * Edit bar.
 */
public class ComponentBar extends AbstractBar {

    private String dialog;

    private String nodeName;

    private boolean isInherited;

    private boolean editable = true;

    public ComponentBar(Model model, EventBus eventBus, MgnlElement mgnlElement) {

        super(model, eventBus, mgnlElement);

        setFields(mgnlElement.getAttributes());
        addStyleName("component");

/*        if (DragDropEventBase.isSupported()) {
            createDragAndDropHandlers();

        }*/
        if (!this.isInherited) {
            createControls();
            //createMouseEventsHandlers();
        }

        setVisible(false);

    }

    public void setDraggable(boolean draggable) {
        if (DragDropEventBase.isSupported()) {
            if (draggable) {
                this.getElement().setDraggable(Element.DRAGGABLE_TRUE);
                getStyle().setCursor(Cursor.MOVE);
            }
            else {
                this.getElement().setDraggable(Element.DRAGGABLE_FALSE);
                getStyle().setCursor(Cursor.DEFAULT);
            }
        }
    }

    private void setFields(Map<String, String> attributes) {
        String content = attributes.get("content");
        int i = content.indexOf(':');

        setWorkspace(content.substring(0, i));
        setPath(content.substring(i + 1));

        this.nodeName = getPath().substring(getPath().lastIndexOf("/") + 1);

        setId("__" + nodeName);

        this.dialog = attributes.get("dialog");

        this.isInherited = Boolean.parseBoolean(attributes.get("inherited"));

        if (attributes.containsKey("editable")) {
            this.editable = Boolean.parseBoolean(attributes.get("editable"));
        }

    }

    public String getNodeName() {
        return nodeName;
    }

    private void createDragAndDropHandlers() {
        DragAndDrop.dragAndDrop(getModel(), getEventBus(), this);
    }

    private void createMouseEventsHandlers() {

        addDomHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                LegacyDragAndDrop.moveComponentEnd(ComponentBar.this);
            }
        }, MouseDownEvent.getType());

        addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                LegacyDragAndDrop.moveComponentOver(ComponentBar.this);

            }
        }, MouseOverEvent.getType());

        addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                LegacyDragAndDrop.moveComponentOut(ComponentBar.this);
            }
        }, MouseOutEvent.getType());
    }

    private void createButtons() {

        final PushButton remove = new PushButton();
        remove.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                getEventBus().fireEvent(new DeleteComponentEvent(getWorkspace(), getPath()));
            }
        });
        remove.setTitle(getI18nMessage("buttons.component.delete.js"));
        remove.setStylePrimaryName("mgnlEditorPushButton");
        remove.addStyleName("remove");

        addSecondaryButton(remove);

        final PushButton move = new PushButton();
        move.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                toggleButtons(false);
                LegacyDragAndDrop.moveComponentStart(getModel(), ComponentBar.this);
            }
        });
        move.setTitle(getI18nMessage("buttons.component.move.js"));
        move.setStylePrimaryName("mgnlEditorPushButton");
        move.addStyleName("move");
        addPrimaryButton(move);

        if (dialog != null) {
            final PushButton edit = new PushButton();
            edit.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    getEventBus().fireEvent(new EditComponentEvent(getWorkspace(), getPath(), dialog));
                }
            });
            edit.setTitle(getI18nMessage("buttons.component.edit.js"));
            edit.setStylePrimaryName("mgnlEditorPushButton");
            edit.addStyleName("edit");
            addPrimaryButton(edit);
        }
    }

    private void createControls() {

               final Label remove = new Label();
                remove.setStyleName(ICON_CLASSNAME);
                remove.addStyleName(REMOVE_CLASSNAME);
                remove.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        getEventBus().fireEvent(new DeleteComponentEvent(getWorkspace(), getPath()));
                    }
                });
                addSecondaryButton(remove);


        /* final Label move = new Label();
        move.setStyleName("icon icon-trash");
        move.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getEventBus().fireEvent(new DeleteComponentEvent(getWorkspace(), getPath()));
            }
        });
        addSecondaryButton(move);*/

        final Label edit = new Label();
        edit.setStyleName(ICON_CLASSNAME);
        edit.addStyleName(EDIT_CLASSNAME);
        edit.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                getEventBus().fireEvent(new EditComponentEvent(getWorkspace(), getPath(), dialog));
            }
        });
        addPrimaryButton(edit);

    }

    @Override
    public String getDialog() {
        return dialog;
    }
}
