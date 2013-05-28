/**
 * This file Copyright (c) 2013 Magnolia International
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


import org.apache.commons.lang.StringUtils;


import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;

/**
 * A base custom field comprising a text field and a button placed to its immediate right.
 * A {@link PropertyTranslator} can be set in order to have a different display and property stored.
 * For example, display can be the Item path and value stored is the identifier of the Item.
 */
public class LinkField extends CustomField<String> {

    private final HorizontalLayout root = new HorizontalLayout();
    private final TextField textField = new TextField();
    private final Button selectButton = new NativeButton();

    private final Converter<String, ?> converter;

    private final String buttonCaptionNew;
    private final String buttonCaptionOther;
    private boolean allowChangesOnSelected;


    public LinkField(Converter<String, ?> converter, String buttonCaptionNew, String buttonCaptionOther, boolean allowChangesOnSelected) {
        this.converter = converter;
        this.buttonCaptionNew = buttonCaptionNew;
        this.buttonCaptionOther = buttonCaptionOther;
        this.allowChangesOnSelected = allowChangesOnSelected;
        setImmediate(true);
    }

    @Override
    protected Component initContent() {
        addStyleName("linkfield");
        // Initialize root
        root.setSizeFull();
        root.setSpacing(true);
        // Handle Text Field
        textField.setImmediate(true);
        textField.setWidth(100, Unit.PERCENTAGE);
        textField.setNullRepresentation("");
        root.addComponent(textField);
        root.setExpandRatio(textField, 1);
        root.setComponentAlignment(textField, Alignment.MIDDLE_LEFT);
        // Only Handle Select button if the Text field is not Read Only.
        if (!textField.isReadOnly()) {
            selectButton.addStyleName("magnoliabutton");
            root.addComponent(selectButton);
            root.setExpandRatio(selectButton, 0);
            root.setComponentAlignment(selectButton, Alignment.MIDDLE_RIGHT);
        }

        return root;
    }

    public TextField getTextField() {
        return this.textField;
    }

    public Button getSelectButton() {
        return this.selectButton;
    }

    @Override
    public String getValue() {
        return textField.getValue();
    }

    @Override
    public void setValue(String newValue) throws ReadOnlyException, ConversionException {
        textField.setValue(newValue);
        if (!allowChangesOnSelected && StringUtils.isNotBlank(newValue)) {
            textField.setReadOnly(true);
            if (root.getComponentIndex(selectButton) != -1) {
                root.removeComponent(selectButton);
            }
        }
        setButtonCaption(newValue);
        fireValueChange(false);
    }

    /**
     * Set propertyDatasource.
     * If the translator is not null, set it as datasource.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void setPropertyDataSource(Property newDataSource) {
        if (converter != null) {
            textField.setConverter(converter);
        }
        textField.setPropertyDataSource(newDataSource);
        setButtonCaption(newDataSource.toString());
        super.setPropertyDataSource(newDataSource);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Property getPropertyDataSource() {
        return textField.getPropertyDataSource();
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    private void setButtonCaption(String value) {
        if (StringUtils.isNotBlank(value)) {
            selectButton.setCaption(buttonCaptionOther);
            selectButton.setDescription(buttonCaptionOther);
        } else {
            selectButton.setCaption(buttonCaptionNew);
            selectButton.setDescription(buttonCaptionNew);
        }
    }

}
