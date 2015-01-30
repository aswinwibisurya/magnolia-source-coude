/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
package info.magnolia.ui.dialog.registry;

import info.magnolia.config.registry.AbstractRegistry;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.DefinitionProviderWrapper;
import info.magnolia.config.registry.DefinitionType;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;

import javax.inject.Singleton;

/**
 * Maintains a registry of dialog providers registered by id.
 */
@Singleton
public class DialogDefinitionRegistry extends AbstractRegistry<DialogDefinition> {

    @Override
    public DefinitionType type() {
        return DefinitionTypes.DIALOG;
    }

    @Override
    protected String asReferenceString(DefinitionProvider<DialogDefinition> provider) {
        return provider.getMetadata().getModule() + ":" + provider.getMetadata().getRelativeLocation();
    }

    @Override
    protected DefinitionProvider<DialogDefinition> onRegister(DefinitionProvider<DialogDefinition> provider) {
        // This was in ConfiguredDialogDefinitionProvider
        // dialogDefinition.setId(id);
        return new DefinitionProviderWrapper<DialogDefinition>(provider) {
            @Override
            public DialogDefinition get() {
                DialogDefinition dd = super.get();
                if (dd instanceof ConfiguredDialogDefinition && dd.getId() == null) {
                    String referenceString = asReferenceString(getDelegate());
                    ((ConfiguredDialogDefinition) dd).setId(referenceString);
                }
                return dd;
            }
        };
    }

    /**
     * @deprecated since 5.4 use {@link #getProvider(String)}
     */
    @Deprecated
    public FormDialogDefinition getDialogDefinition(String id) throws RegistrationException {
        try {
            DefinitionProvider<DialogDefinition> dialogDefinitionProvider = getProvider(id);
            DialogDefinition dialogDefinition = dialogDefinitionProvider.get();
            return (FormDialogDefinition) dialogDefinition;
            // TODO: } catch (RegistrationException e) {
        } catch (IllegalArgumentException e) {
            throw new RegistrationException(e.getMessage(), e);
        }
    }

    /**
     * @deprecated since 5.4, get the {@link info.magnolia.ui.dialog.definition.DialogDefinition} first, and get the presenter class from it.
     */
    @Deprecated
    public Class<? extends FormDialogPresenter> getPresenterClass(String id) throws RegistrationException {
        return (Class<? extends FormDialogPresenter>) getProvider(id).get().getPresenterClass();
    }

}
