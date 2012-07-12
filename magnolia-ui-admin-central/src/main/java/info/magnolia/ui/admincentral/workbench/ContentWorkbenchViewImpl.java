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
package info.magnolia.ui.admincentral.workbench;

import info.magnolia.ui.admincentral.actionbar.builder.ActionbarBuilder;
import info.magnolia.ui.admincentral.jcr.view.ContentView;
import info.magnolia.ui.admincentral.jcr.view.ContentView.ViewType;
import info.magnolia.ui.admincentral.jcr.view.builder.ContentViewBuilderProvider;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.widget.actionbar.Actionbar;

import java.util.EnumMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;


/**
 * Implementation of {@link ContentWorkbenchView}.
 */
public class ContentWorkbenchViewImpl extends CustomComponent implements ContentWorkbenchView {

    private static final Logger log = LoggerFactory.getLogger(ContentWorkbenchViewImpl.class);

    private final ContentViewBuilderProvider contentViewBuilderProvider;

    private final HorizontalLayout root = new HorizontalLayout();

    private final VerticalLayout workbenchContainer = new VerticalLayout();

    private final Map<ViewType, ContentView> contentViews = new EnumMap<ViewType, ContentView>(ViewType.class);

    private ViewType currentViewType = ViewType.TREE;

    private ContentWorkbenchView.Listener contentWorkbenchViewListener;

    private final ContentView.Listener contentViewListener = new ContentView.Listener() {
        @Override
        public void onItemSelection(Item item) {
            contentWorkbenchViewListener.onItemSelected(item);
        }
    };

    @Inject
    public ContentWorkbenchViewImpl(final ContentViewBuilderProvider contentViewBuilderProvider) {
        super();
        this.contentViewBuilderProvider = contentViewBuilderProvider;
        setCompositionRoot(root);
        setSizeFull();

        root.setSizeFull();
        root.setStyleName("mgnl-app-root");
        root.addComponent(workbenchContainer);
        root.setExpandRatio(workbenchContainer, 1f);
        root.setMargin(false);
        root.setSpacing(true);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setSizeUndefined();
        toolbar.setStyleName("mgnl-workbench-toolbar");
        toolbar.addComponent(new Button("Tree", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                setGridType(ViewType.TREE);
            }
        }));
        toolbar.addComponent(new Button("List", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                setGridType(ViewType.LIST);
            }
        }));

        workbenchContainer.setSizeFull();
        workbenchContainer.setStyleName("mgnl-app-view");
        workbenchContainer.addComponent(toolbar);
    }

    @Override
    public void initWorkbench(final WorkbenchDefinition workbenchDefinition) {
        if (workbenchDefinition == null) {
            throw new IllegalArgumentException("Trying to init a workbench but got null definition.");
        }
        log.debug("Initializing workbench {}...", workbenchDefinition.getName());

        for (final ViewType type : ViewType.values()) {
            final ContentView contentView = contentViewBuilderProvider.getBuilder().build(workbenchDefinition, type);
            contentView.setListener(contentViewListener);
            contentView.select(StringUtils.defaultIfEmpty(workbenchDefinition.getPath(), "/"));
            contentViews.put(type, contentView);
        }

        if (StringUtils.isBlank(workbenchDefinition.getWorkspace())) {
            throw new IllegalStateException(workbenchDefinition.getName()
                + " workbench definition must specify a workspace to connect to. Please, check your configuration.");
        }

        setGridType(ViewType.TREE);
    }

    @Override
    public void initActionbar(final ActionbarDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Trying to init an action bar but got null definition.");
        }
        final Actionbar actionbar = ActionbarBuilder.build(definition, getListener());
        root.addComponent(actionbar);
    }

    public ContentWorkbenchView.Listener getListener() {
        return contentWorkbenchViewListener;
    }

    @Override
    public void setListener(final ContentWorkbenchView.Listener listener) {
        this.contentWorkbenchViewListener = listener;
    }

    @Override
    public void setGridType(ViewType type) {
        workbenchContainer.removeComponent(contentViews.get(currentViewType).asVaadinComponent());
        final Component c = contentViews.get(type).asVaadinComponent();

        workbenchContainer.addComponent(c);
        workbenchContainer.setExpandRatio(c, 1f);

        // split.addComponentAsFirst(c);
        this.currentViewType = type;
        refresh();
    }

    @Override
    public void refreshItem(Item item) {
        contentViews.get(currentViewType).refreshItem(item);
    }

    @Override
    public void refresh() {
        contentViews.get(currentViewType).refresh();
    }

}
