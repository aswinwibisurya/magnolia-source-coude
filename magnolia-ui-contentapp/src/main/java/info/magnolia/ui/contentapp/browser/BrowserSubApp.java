/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
package info.magnolia.ui.contentapp.browser;

import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.contentapp.ContentSubAppView;
import info.magnolia.ui.contentapp.dsmanager.DataSourceManagerProvider;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 31/01/14
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
public class BrowserSubApp extends BrowserSubAppBase {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    public BrowserSubApp(ActionExecutor actionExecutor, final SubAppContext subAppContext, final ContentSubAppView view, final BrowserPresenter browser, final @Named(SubAppEventBus.NAME) EventBus subAppEventBus, final ComponentProvider componentProvider, DataSourceManagerProvider dsManagerProvider) {
        super(actionExecutor, subAppContext, view, browser, subAppEventBus, componentProvider, dsManagerProvider);
    }

    @Override
    protected List<Item> getSelectedItems() {
        WorkbenchDefinition workbench = getWorkbench();
        List<Object> selectedItemIds = getBrowser().getSelectedItemIds();
        if (selectedItemIds.size() > 1) {
            return getItemsExceptOne(workbench.getWorkspace(), selectedItemIds, getRootItemId());
        } else {
            return getItemsExceptOne(workbench.getWorkspace(), selectedItemIds, null);
        }
    }

    @Override
    protected boolean verifyAvailability(Item item, AvailabilityDefinition availability) {
        if (item instanceof JcrItemAdapter) {
            JcrItemAdapter jcrItemAdapter = (JcrItemAdapter)item;

            // If its a property we display it only if the properties property is set
            if (!jcrItemAdapter.isNode()) {
                return availability.isProperties();
            }

            // If node is selected and the section is available for nodes
            if (availability.isNodes()) {
                // if no node type defined, the for all node types
                if (availability.getNodeTypes().isEmpty()) {
                    return true;
                }
                // else the node must match at least one of the configured node types
                for (String nodeType : availability.getNodeTypes()) {
                    try {
                        if (NodeUtil.isNodeType((Node) jcrItemAdapter.getJcrItem(), nodeType)) {
                            return true;
                        }
                    } catch (RepositoryException e) {
                        continue;
                    }
                }

            }

        }

        return super.verifyAvailability(item, availability);
    }

    public List<Item> getItemsExceptOne(final String workspaceName, List<Object> ids, Object itemIdToExclude) {
        List<Item> items = new ArrayList<Item>();

        for (Object itemId : ids) {
            if (!itemId.equals(itemIdToExclude)) {
                try {
                    javax.jcr.Item jcrItem = JcrItemUtil.getJcrItem(workspaceName, String.valueOf(itemId));
                    if (jcrItem.isNode()) {
                        items.add(new JcrNodeAdapter((Node)jcrItem));
                    } else {
                        items.add(new JcrPropertyAdapter((Property)jcrItem));
                    }

                } catch (RepositoryException e) {
                    e.printStackTrace();
                }

            }
        }
        return items;
    }
}
