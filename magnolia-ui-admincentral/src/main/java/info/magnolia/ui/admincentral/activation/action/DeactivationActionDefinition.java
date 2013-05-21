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
package info.magnolia.ui.admincentral.activation.action;

import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.api.action.CommandActionDefinition;


/**
 * The deactivation action, invoking the deactivation command, and updating the UI accordingly.
 *
 * @see DeactivationAction
 */
public class DeactivationActionDefinition extends CommandActionDefinition {

    public DeactivationActionDefinition() {
        setImplementationClass(DeactivationAction.class);
    }

    @Override
    public String getSuccessMessage() {
        if (workflowInstalled()) {
            return "The workflow has been started.";
        }
        return "Deactivation has been started.";
    }

    @Override
    public String getFailureMessage() {
        if (workflowInstalled()) {
            return "The workflow could not be launched.";
        }
        return "Deactivation has failed.";
    }

    @Override
    public String getErrorMessage() {
        return "Deactivation failed. Please contact the system administrator for assistance.";
    }

    private boolean workflowInstalled() {
        return SessionUtil.getNode("config", "/modules/workflow-base") != null;
    }
}
