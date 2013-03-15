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
package info.magnolia.ui.contentapp.workbench;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.contentapp.ItemSubAppDescriptor;
import info.magnolia.ui.contentapp.item.ItemPresenter;
import info.magnolia.ui.contentapp.item.ItemView;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.action.ActionExecutor;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.view.View;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Presenter for the workbench displayed in the {@link info.magnolia.ui.contentapp.ItemSubApp}.
 * Contains the {@link ActionbarPresenter} for handling action events and the {@link ItemPresenter} for displaying the actual item.
 */
public class ItemWorkbenchPresenter implements ItemWorkbenchView.Listener, ActionbarPresenter.Listener {

    private static final Logger log = LoggerFactory.getLogger(ItemWorkbenchPresenter.class);

    private final ActionExecutor actionExecutor;
    private final AppContext appContext;
    private final ItemWorkbenchView view;
    private final ItemPresenter itemPresenter;
    private final ActionbarPresenter actionbarPresenter;
    private final ItemSubAppDescriptor subAppDescriptor;
    private String nodePath;

    @Inject
    public ItemWorkbenchPresenter(final ActionExecutor actionExecutor, final SubAppContext subAppContext, final ItemWorkbenchView view,final ItemPresenter itemPresenter, final ActionbarPresenter actionbarPresenter) {
        this.actionExecutor = actionExecutor;
        this.view = view;
        this.itemPresenter = itemPresenter;
        this.actionbarPresenter = actionbarPresenter;
        this.appContext = subAppContext.getAppContext();
        this.subAppDescriptor = (ItemSubAppDescriptor) subAppContext.getSubAppDescriptor();
    }

    public View start(String nodePath, ItemView.ViewType viewType) {
        view.setListener(this);
        this.nodePath = nodePath;
        JcrNodeAdapter item;
        try {
            Session session = MgnlContext.getJCRSession(subAppDescriptor.getWorkspace());
            if (session.nodeExists(nodePath) && session.getNode(nodePath).getPrimaryNodeType().getName().equals(subAppDescriptor.getNodeType().getName())) {
                item = new JcrNodeAdapter(SessionUtil.getNode(subAppDescriptor.getWorkspace(), nodePath));
            } else {
                String parentPath = StringUtils.substringBeforeLast(nodePath, "/");
                parentPath = parentPath.isEmpty() ? "/" : parentPath;
                Node parent = session.getNode(parentPath);
                item = new JcrNewNodeAdapter(parent, subAppDescriptor.getNodeType().getName());
            }
        } catch (RepositoryException e) {
            log.warn("Not able to create an Item based on the following path {} ", nodePath, e);
            throw new RuntimeException(e);
        }

        ItemView itemView = itemPresenter.start(subAppDescriptor.getFormDefinition(), item, viewType);

        view.setItemView(itemView);
        actionbarPresenter.setListener(this);
        ActionbarView actionbar = actionbarPresenter.start(subAppDescriptor.getActionbar());

        view.setActionbarView(actionbar);

        return view;
    }

    public String getNodePath() {
        return nodePath;
    }

    public ActionbarPresenter getActionbarPresenter() {
        return actionbarPresenter;
    }

    @Override
    public void onViewTypeChanged(final ItemView.ViewType viewType) {
        // eventBus.fireEvent(new ViewTypeChangedEvent(viewType));
    }

    @Override
    public void onExecute(String actionName) {
        try {
            Session session = MgnlContext.getJCRSession(subAppDescriptor.getWorkspace());
            final javax.jcr.Item item = session.getItem(nodePath);

            actionExecutor.execute(actionName, item);

        } catch (RepositoryException e) {
            Message error = new Message(MessageType.ERROR, "Could not get item: " + nodePath, e.getMessage());
            appContext.broadcastMessage(error);
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            appContext.broadcastMessage(error);
        }
    }

    @Override
    public String getLabel(String actionName) {
        ActionDefinition actionDefinition = actionExecutor.getActionDefinition(actionName);
        return actionDefinition != null ? actionDefinition.getLabel() : null;
    }

    @Override
    public String getIcon(String actionName) {
        ActionDefinition actionDefinition = actionExecutor.getActionDefinition(actionName);
        return actionDefinition != null ? actionDefinition.getIcon() : null;
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        if (fullScreen) {
            appContext.enterFullScreenMode();
        } else {
            appContext.exitFullScreenMode();
        }
    }
}
