/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ui.admincentral.dialog.registry;

import info.magnolia.ui.model.dialog.registry.DialogDefinitionProvider;
import info.magnolia.ui.model.dialog.registry.DialogDefinitionRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Used to create a DummyDialog. In the former implementation this Class used to observe nodes for dialogs
 * and pass the nodes to a provider which used content2bean to create a dialogdefinition from jcr.
 *
 * @version $Id$
 */
@Singleton
public class DummyDialogDefinitionManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> registeredIds = new HashSet<String>();
    private final DialogDefinitionRegistry dialogDefinitionRegistry;

    @Inject
    public DummyDialogDefinitionManager(DialogDefinitionRegistry dialogDefinitionRegistry) {
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
    }

    public void load() {

        final List<DialogDefinitionProvider> providers = new ArrayList<DialogDefinitionProvider>();


        DialogDefinitionProvider provider = new DummyDialogDefinitionProvider("admin-central:userpreferences");
        if (provider != null) {
            providers.add(provider);
        }

        this.registeredIds = dialogDefinitionRegistry.unregisterAndRegister(registeredIds, providers);
    }

    protected String createId(String dialogname, String path) {
        return dialogname + ":" + path;
    }
}
