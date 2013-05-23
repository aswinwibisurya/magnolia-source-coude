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
package info.magnolia.ui.form.field.builder;

import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;

import java.util.HashSet;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.OptionGroup;

/**
 * Creates and initializes a select field based on a field definition.
 */
public class OptionGroupFieldBuilder extends SelectFieldBuilder<OptionGroupFieldDefinition> {

    public OptionGroupFieldBuilder(OptionGroupFieldDefinition definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
    }

    @Override
    protected AbstractSelect buildField() {
        super.buildField();
        select.setMultiSelect(getFieldDefinition().isMultiselect());
        if (select.isMultiSelect()) {
            select.setNullSelectionAllowed(true);
        }
        return select;
    }

    @Override
    protected AbstractSelect createSelectionField() {
        return new OptionGroup();
    }

    @Override
    protected Property<?> getOrCreateProperty() {
        if (!select.isMultiSelect()) {
            return super.getOrCreateProperty();
        }
        String propertyName = definition.getName();
        Class<?> fieldType = getFieldType(definition);
        Property<?> property = item.getItemProperty(propertyName);
        if (property == null) {
            property = new DefaultProperty(propertyName, HashSet.class, new HashSet());
            item.addItemProperty(propertyName, property);
        }

        return property;
    }
}
