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
package info.magnolia.ui.form.field.definition;

import static org.junit.Assert.assertEquals;

import info.magnolia.i18n.I18nizer;
import info.magnolia.i18n.proxytoys.ProxytoysI18nizer;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.definition.TestDialogDef;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * TODO Type description here.
 */
public class FieldDefinitionKeyGeneratorTest {

    @Test
    public void keysForFieldLabel() throws SecurityException, NoSuchMethodException {
        // GIVEN
        // generator
        FieldDefinitionKeyGenerator generator = new FieldDefinitionKeyGenerator();
        // structure
        TestDialogDef dialog = new TestDialogDef("test-module:testFolder/testDialog");
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("testTab");
        ConfiguredFieldDefinition field = new ConfiguredFieldDefinition();
        field.setName("testField");
        // hierarchy
        dialog.setForm(form);
        form.addTab(tab);
        tab.addField(field);
        // i18n
        I18nizer i18nizer = new ProxytoysI18nizer(null, null);
        dialog = i18nizer.decorate(dialog);

        // WHEN
        List<String> keys = new ArrayList<String>(4);
        generator.keysFor(keys, dialog.getForm().getTabs().get(0).getFields().get(0), field.getClass().getMethod("getLabel"));

        // THEN
        assertEquals(4, keys.size());
        assertEquals("test-module.testFolder.testDialog.testTab.testField.label", keys.get(0));
        assertEquals("test-module.testFolder.testDialog.testTab.testField", keys.get(1));
        assertEquals("test-module.testFolder.testDialog.testField.label", keys.get(2));
        assertEquals("test-module.testFolder.testDialog.testField", keys.get(3));
    }

    @Test
    public void keysForNestedFieldLabel() throws SecurityException, NoSuchMethodException {
        // GIVEN
        // generator
        FieldDefinitionKeyGenerator generator = new FieldDefinitionKeyGenerator();
        // structure
        TestDialogDef dialog = new TestDialogDef("test-module:testFolder/testDialog");
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("testTab");
        MultiValueFieldDefinition parentField = new MultiValueFieldDefinition();
        parentField.setName("parentField");
        ConfiguredFieldDefinition field = new ConfiguredFieldDefinition();
        field.setName("testField");
        // hierarchy
        dialog.setForm(form);
        form.addTab(tab);
        tab.addField(parentField);
        parentField.setField(field);
        // i18n
        I18nizer i18nizer = new ProxytoysI18nizer(null, null);
        dialog = i18nizer.decorate(dialog);

        // WHEN
        List<String> keys = new ArrayList<String>(4);
        generator.keysFor(
                keys,
                ((MultiValueFieldDefinition) dialog.getForm().getTabs().get(0).getFields().get(0)).getField(),
                field.getClass().getMethod("getLabel"));

        // THEN
        assertEquals(6, keys.size());
        assertEquals("test-module.testFolder.testDialog.testTab.parentField.testField.label", keys.get(0));
        assertEquals("test-module.testFolder.testDialog.testTab.parentField.testField", keys.get(1));
        assertEquals("test-module.testFolder.testDialog.testTab.testField.label", keys.get(2));
        assertEquals("test-module.testFolder.testDialog.testTab.testField", keys.get(3));
        assertEquals("test-module.testFolder.testDialog.testField.label", keys.get(4));
        assertEquals("test-module.testFolder.testDialog.testField", keys.get(5));
    }
}
