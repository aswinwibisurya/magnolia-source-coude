/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesUtil;

/**
 * Abstract base class for dialog items, provides resolution of {@link Messages} in the hierarchical.
 *
 * @see Messages
 * @see DialogItem
 */
public abstract class AbstractDialogItem implements DialogItem {

    private DialogItem parent;

    private static Messages messages;

    static {
        String uiPackagePrefix = "info.magnolia.ui.";
        String[] uiModules = {"model", "framework", "widget.actionbar", "widget.dialog", "widget.editor",
                "widget.magnoliashell", "widget.tabsheet", "vaadin.integration"};
        List<String> basenames = new ArrayList<String>();
        for(String module: uiModules) {
            basenames.add(uiPackagePrefix + module + ".messages");
        }
        messages = MessagesUtil.chain(basenames.toArray(new String[]{}));
    }

    @Override
    public void setParent(DialogItem parent) {
        this.parent = parent;
    }

    @Override
    public DialogItem getParent() {
        return parent;
    }

    @Override
    public Messages getMessages() {
        if (getParent() != null) {
            messages = getParent().getMessages();
        }
        if (StringUtils.isNotBlank(getI18nBasename())) {
            messages = MessagesUtil.chain(getI18nBasename(), messages);
        }
        return messages;
    }

    protected abstract String getI18nBasename();

    public String getMessage(String key) {
        return getMessages().getWithDefault(key, key);
    }
}
