/**
 * This file Copyright (c) 2010-2014 Magnolia International
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
package info.magnolia.ui.contentapp.field;

import info.magnolia.event.EventBus;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.SearchEvent;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;

import java.util.Arrays;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

/**
 * Wraps a workbench instance into a field. While it is quite useful in dialogs the users are discouraged
 * from using it in a form.
 */
public class WorkbenchField extends CustomField<Object> {

    private static final String DEFAULT_HEIGHT = "400px";

    private Logger log = LoggerFactory.getLogger(getClass());

    private ImageProviderDefinition imageProvider;

    private WorkbenchDefinition workbenchDefinition;

    private WorkbenchPresenter presenter;

    private EventBus workbenchEventbus;

    private WorkbenchView view;

    public WorkbenchField(WorkbenchDefinition definition, ImageProviderDefinition imageProvider, WorkbenchPresenter presenter, EventBus eventBus) {
        this.workbenchDefinition = definition;
        this.imageProvider = imageProvider;
        this.presenter = presenter;
        this.workbenchEventbus = eventBus;
    }

    @Override
    protected Component initContent() {
        this.view = presenter.start(workbenchDefinition, imageProvider, workbenchEventbus);
        this.view.setViewType(TreePresenterDefinition.VIEW_TYPE);
        this.view.asVaadinComponent().setHeight(DEFAULT_HEIGHT);
        if (getConvertedValue() instanceof JcrItemAdapter) {
            presenter.select(Arrays.asList(((JcrItemAdapter) getConvertedValue()).getItemId()));
        }

        workbenchEventbus.addHandler(SelectionChangedEvent.class, new SelectionChangedEvent.Handler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                setValue(event.getFirstItemId(), false);
            }
        });

        workbenchEventbus.addHandler(SearchEvent.class, new SearchEvent.Handler() {
            @Override
            public void onSearch(SearchEvent event) {
                presenter.doSearch(event.getSearchExpression());
            }
        });

        return this.view.asVaadinComponent();
    }

    @Override
    public void setPropertyDataSource(com.vaadin.data.Property newDataSource) {
        super.setPropertyDataSource(null);
    }

    @Override
    public Class<? extends Item> getType() {
        return Item.class;
    }

    public WorkbenchPresenter getPresenter() {
        return presenter;
    }
}
