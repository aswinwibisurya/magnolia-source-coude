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
package info.magnolia.ui.form.field.transformer.composite;

import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;

/**
 * Empty Implementation of {@link info.magnolia.ui.form.field.transformer.Transformer} handling {@link PropertysetItem}.<br>
 * This is mainly used if the {@link PropertysetItem} is handle by a parent field that define a sub node {@link info.magnolia.ui.form.field.transformer.Transformer} like a {@link info.magnolia.ui.form.field.MultiField} displaying {@link info.magnolia.ui.form.field.CompositeField}.
 */
public class NoOpCompositeTransformer extends BasicTransformer<PropertysetItem> {

    public NoOpCompositeTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type) {
        super(relatedFormItem, definition, type);
    }

    private PropertysetItem propertysetItem;

    @Override
    public void writeToItem(PropertysetItem newValue) {
        this.propertysetItem = newValue;
    }

    @Override
    public PropertysetItem readFromItem() {
        if (this.propertysetItem == null) {
            return new PropertysetItem();
        }
        return this.propertysetItem;
    }

}
