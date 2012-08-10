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
package info.magnolia.ui.admincentral.dialog.action;

import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.ui.admincentral.event.ContentChangedEvent;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.widget.dialog.MagnoliaDialogView;
import info.magnolia.ui.widget.dialog.MagnoliaDialogView.Presenter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Saves a dialog.
 *
 * @see SaveDialogActionDefinition
 */
public class SaveDialogAction extends ActionBase<SaveDialogActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(SaveDialogAction.class);

    private Item item;
    private EventBus eventBus;
    private Presenter presenter;

    public SaveDialogAction(SaveDialogActionDefinition definition, MagnoliaDialogView.Presenter presenter) {
        super(definition);
        this.presenter = presenter;
        this.item = presenter.getItem();
        this.eventBus = presenter.getEventBus();
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        presenter.showValidation(true);
        if (presenter.getView().isValid()) {
            final JcrNodeAdapter itemChanged = (JcrNodeAdapter) item;
            try {
                final Node node = itemChanged.getNode();
                //Update MetaData
                MetaDataUtil.updateMetaData(node);
                node.getSession().save();
            } catch (final RepositoryException e) {
                throw new ActionExecutionException(e);
            }
            eventBus.fireEvent(new ContentChangedEvent(itemChanged.getWorkspace(), itemChanged.getItemId()));
            presenter.closeDialog();
        } else {
            log.warn("View is not valid. No save performed");
        }
    }

}
