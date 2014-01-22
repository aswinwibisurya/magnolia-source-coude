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
package info.magnolia.ui.framework.action;

import info.magnolia.cms.core.version.VersionInfo;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.VersionUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.text.MessageFormat;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.util.BeanItem;

/**
 * Abstract version action showing available versions of an item.
 *
 * @param <D> {@link info.magnolia.ui.api.action.ActionDefinition}.
 */
public abstract class AbstractVersionAction<D extends ActionDefinition> extends AbstractAction<D> {

    /**
     * Label format for versions.
     * Will display: VersionNumber (Date) (Comment), i.e. <code>1.1 (MM/dd/YYYY HH:mm) (Comment)</code>.
     */
    public final String MESSAGE_FORMAT_VERSION_OPTION_LABEL = "{0} ({1}) ({2})";
    public final String MESSAGE_FORMAT_VERSION_OPTION_LABEL_NO_COMMENT = "{0} ({1})";

    private final D definition;
    protected final LocationController locationController;
    protected final UiContext uiContext;
    protected final SimpleTranslator i18n;
    protected final FormDialogPresenter formDialogPresenter;
    protected final AbstractJcrNodeAdapter nodeAdapter;
    private BeanItem<?> item;

    protected AbstractVersionAction(D definition, LocationController locationController, UiContext uiContext, FormDialogPresenter formDialogPresenter, AbstractJcrNodeAdapter nodeAdapter,SimpleTranslator i18n) {
        super(definition);
        this.definition = definition;
        this.locationController = locationController;
        this.uiContext = uiContext;
        this.formDialogPresenter = formDialogPresenter;
        this.nodeAdapter = nodeAdapter;
        this.i18n = i18n;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            final FormDialogDefinition dialogDefinition = buildNewComponentDialog();

            // Using a beanItem instead of an jcrItem
            item = new BeanItem(getBeanItemClass());

            // Perform custom chaining of dialogs
            formDialogPresenter.start(item, dialogDefinition, uiContext, getEditorCallback());
        } catch (RepositoryException e) {
            throw new ActionExecutionException("Could not execute action", e);
        }
    }

    protected EditorCallback getEditorCallback() {
        return new EditorCallback() {
            @Override
            public void onSuccess(String actionName) {
                try {
                    // Build the new location to go to
                    Location location = getLocation();

                    // Open location
                    locationController.goTo(location);
                } catch (ActionExecutionException e) {
                    uiContext.openNotification(MessageStyleTypeEnum.ERROR, true, i18n.translate("ui-framework.version.executionException.noValidVersion"));
                }

                // Close the dialog
                formDialogPresenter.closeDialog();
            }

            @Override
            public void onCancel() {
                formDialogPresenter.closeDialog();
            }
        };
    }

    protected List<VersionInfo> getAvailableVersionInfoList() throws ActionExecutionException {
        final Node node;
        try {
            node = getNode();
            List<VersionInfo> versionInfoList = VersionUtil.getVersionInfoList(node);

            // This should not happen, as we use action availability for this action
            if (versionInfoList == null || versionInfoList.isEmpty()) {
                throw new ActionExecutionException(String.format(i18n.translate("ui-framework.version.infoList.noListForItem"), nodeAdapter.getItemId()));
            }

            return versionInfoList;
        } catch (RepositoryException e) {
            throw new ActionExecutionException(String.format(i18n.translate("ui-framework.version.infoList.repositoryException"), nodeAdapter.getItemId()));
        }
    }

    protected String getVersionLabel(VersionInfo versionInfo) {
        if (StringUtils.isEmpty(versionInfo.getVersionComment())) {
            return MessageFormat.format(MESSAGE_FORMAT_VERSION_OPTION_LABEL_NO_COMMENT, versionInfo.getVersionName(), versionInfo.getVersionDate());
        } else {
            return MessageFormat.format(MESSAGE_FORMAT_VERSION_OPTION_LABEL, versionInfo.getVersionName(), versionInfo.getVersionDate(), versionInfo.getVersionComment());
        }
    }

    protected BeanItem<?> getItem() {
        return item;
    }

    protected abstract Class getBeanItemClass();

    protected abstract FormDialogDefinition buildNewComponentDialog() throws ActionExecutionException, RepositoryException;

    protected abstract Node getNode() throws RepositoryException;

    protected abstract Location getLocation() throws ActionExecutionException;

}
