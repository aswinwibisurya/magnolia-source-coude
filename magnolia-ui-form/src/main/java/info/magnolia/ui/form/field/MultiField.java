/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.form.field;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.transformer.TransformedProperty;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.field.transformer.multi.MultiTransformer;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;

/**
 * Generic Multi Field.<br>
 * This generic MultiField allows to handle a Field Set. It handle :<br>
 * - The creation of new Field<br>
 * - The removal of Field<br>
 * The Field is build based on a generic {@link ConfiguredFieldDefinition}.<br>
 * The Field values are handle by a configured {@link info.magnolia.ui.form.field.transformer.Transformer} dedicated to create/retrieve properties as {@link PropertysetItem}.<br>
 */
public class MultiField extends AbstractCustomMultiField<MultiValueFieldDefinition, PropertysetItem> {

    private static final Logger log = LoggerFactory.getLogger(MultiField.class);

    private final ConfiguredFieldDefinition fieldDefinition;

    private final Button addButton = new NativeButton();
    private String buttonCaptionAdd;
    private String buttonCaptionRemove;

    public MultiField(MultiValueFieldDefinition definition, FieldFactoryFactory fieldFactoryFactory, ComponentProvider componentProvider, Item relatedFieldItem, I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, fieldFactoryFactory, componentProvider, relatedFieldItem, i18nAuthoringSupport);
        this.fieldDefinition = definition.getField();
    }

    /**
     * @deprecated since 5.3.5 removing i18nContentSupport dependency (actually unused way before that). Besides, fields should use i18nAuthoringSupport for internationalization.
     */
    @Deprecated
    public MultiField(MultiValueFieldDefinition definition, FieldFactoryFactory fieldFactoryFactory, I18nContentSupport i18nContentSupport, ComponentProvider componentProvider, Item relatedFieldItem) {
        this(definition, fieldFactoryFactory, componentProvider, relatedFieldItem, null);
    }

    @Override
    protected Component initContent() {
        // Init root layout
        addStyleName("linkfield");
        root = new VerticalLayout();
        root.setSpacing(true);
        root.setWidth(100, Unit.PERCENTAGE);
        root.setHeight(-1, Unit.PIXELS);

        // Init addButton
        addButton.setCaption(buttonCaptionAdd);
        addButton.addStyleName("magnoliabutton");
        addButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {

                int newPropertyId = -1;
                Property<?> property = null;

                Transformer<?> transformer = ((TransformedProperty<?>) getPropertyDataSource()).getTransformer();
                PropertysetItem item = (PropertysetItem) getPropertyDataSource().getValue();

                if (transformer instanceof MultiTransformer) {
                    // create property and find its propertyId
                    property = ((MultiTransformer) transformer).createProperty();
                    newPropertyId = (Integer) findPropertyId(item, property);
                } else {
                    // get next propertyId based on property count
                    newPropertyId = item.getItemPropertyIds().size();
                }

                if (newPropertyId == -1) {
                    log.warn("Could not resolve new propertyId; cannot add new multifield entry to item '{}'.", item);
                    return;
                }

                root.addComponent(createEntryComponent(newPropertyId, property), root.getComponentCount() - 1);
            };
        });

        // Initialize Existing field
        initFields();

        return root;
    }

    /**
     * Initialize the MultiField. <br>
     * Create as many configured Field as we have related values already stored.
     */
    @Override
    protected void initFields(PropertysetItem newValue) {
        root.removeAllComponents();
        Iterator<?> it = newValue.getItemPropertyIds().iterator();
        while (it.hasNext()) {
            Object propertyId = it.next();
            Property<?> property = newValue.getItemProperty(propertyId);
            root.addComponent(createEntryComponent(propertyId, property));
        }
        root.addComponent(addButton);
    }

    /**
     * Create a single element.<br>
     * This single element is composed of:<br>
     * - a configured field <br>
     * - a remove Button<br>
     */
    private Component createEntryComponent(Object propertyId, Property<?> property) {

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth(100, Unit.PERCENTAGE);
        layout.setHeight(-1, Unit.PIXELS);

        Field<?> field = createLocalField(fieldDefinition, property, true); // creates property datasource if given property is null
        layout.addComponent(field);

        // bind the field's property to the item
        if (property == null) {
            property = field.getPropertyDataSource();
            ((PropertysetItem) getPropertyDataSource().getValue()).addItemProperty(propertyId, property);
        }
        final Property<?> propertyReference = property;

        // Delete Button
        Button deleteButton = new Button();
        deleteButton.setHtmlContentAllowed(true);
        deleteButton.setCaption("<span class=\"" + "icon-trash" + "\"></span>");
        deleteButton.addStyleName("inline");
        deleteButton.setDescription(buttonCaptionRemove);
        deleteButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                Component layout = event.getComponent().getParent();
                root.removeComponent(layout);
                Transformer<?> transformer = ((TransformedProperty<?>) getPropertyDataSource()).getTransformer();

                // get propertyId to delete, this might have changed since initialization above (see #removeValueProperty)
                Object propertyId = findPropertyId(getValue(), propertyReference);

                if (transformer instanceof MultiTransformer) {
                    ((MultiTransformer) transformer).removeProperty(propertyId);
                } else {
                    if (propertyId != null && propertyId.getClass().isAssignableFrom(Integer.class)) {
                        removeValueProperty((Integer) propertyId);
                    }
                    getPropertyDataSource().setValue(getValue());
                }
            }
        });
        layout.addComponent(deleteButton);

        // set layout to full width
        layout.setWidth(100, Unit.PERCENTAGE);

        // distribute space in favour of field over delete button
        layout.setExpandRatio(field, 1);
        layout.setExpandRatio(deleteButton, 0);

        // make sure button stays aligned with the field and not with the optional field label when used
        layout.setComponentAlignment(deleteButton, Alignment.BOTTOM_RIGHT);

        return layout;
    }


    @Override
    public Class<? extends PropertysetItem> getType() {
        return PropertysetItem.class;
    }

    /**
     * Caption section.
     */
    public void setButtonCaptionAdd(String buttonCaptionAdd) {
        this.buttonCaptionAdd = buttonCaptionAdd;
    }

    public void setButtonCaptionRemove(String buttonCaptionRemove) {
        this.buttonCaptionRemove = buttonCaptionRemove;
    }

    /**
     * Ensure that id of the {@link PropertysetItem} stay coherent.<br>
     * Assume that we have 3 values 0:a, 1:b, 2:c, and 1 is removed <br>
     * If we just remove 1, the {@link PropertysetItem} will contain 0:a, 2:c, .<br>
     * But we should have : 0:a, 1:c, .
     */
    private void removeValueProperty(int fromIndex) {
        getValue().removeItemProperty(fromIndex);
        int toIndex = fromIndex;
        int valuesSize = getValue().getItemPropertyIds().size();
        if (fromIndex == valuesSize) {
            return;
        }
        while (fromIndex < valuesSize) {
            toIndex = fromIndex;
            fromIndex +=1;
            getValue().addItemProperty(toIndex, getValue().getItemProperty(fromIndex));
            getValue().removeItemProperty(fromIndex);
        }
    }

}
