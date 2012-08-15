/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.admincentral.field.builder;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.content.view.builder.DefinitionToImplementationMapping;
import info.magnolia.ui.admincentral.field.FieldBuilder;
import info.magnolia.ui.model.builder.FactoryBase;
import info.magnolia.ui.model.field.definition.FieldDefinition;

import java.io.Serializable;

import javax.inject.Inject;

import com.vaadin.data.Item;

/**
 * Factory for creating DialogField instances using an internal set of mappings connecting a {@link FieldDefinition}
 * class with a {@link FieldBuilder} class.
 *
 * @see FieldDefinition
 * @see FieldBuilder
 */
public class DialogFieldFactory extends FactoryBase<FieldDefinition, FieldBuilder> implements Serializable {


    @Inject
    public DialogFieldFactory(ComponentProvider componentProvider, DialogFieldRegistry dialogFieldRegistery) {
        super(componentProvider);

        for (DefinitionToImplementationMapping<FieldDefinition, FieldBuilder> definitionToImplementationMapping : dialogFieldRegistery.getDefinitionToImplementationMappings()) {
            addMapping(definitionToImplementationMapping.getDefinition(), definitionToImplementationMapping.getImplementation());
        }
    }

    public FieldBuilder create(FieldDefinition definition, Item item, Object... parameters) {
        return super.create(definition, item, parameters);
    }
}
