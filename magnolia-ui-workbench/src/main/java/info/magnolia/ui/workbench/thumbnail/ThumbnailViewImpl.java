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
package info.magnolia.ui.workbench.thumbnail;

import info.magnolia.ui.vaadin.layout.LazyThumbnailLayout;
import info.magnolia.ui.vaadin.layout.LazyThumbnailLayout.ThumbnailDblClickListener;
import info.magnolia.ui.vaadin.layout.LazyThumbnailLayout.ThumbnailSelectionListener;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.ui.Component;

/**
 * Default Vaadin implementation of the thumbnail view.
 */
public class ThumbnailViewImpl implements ThumbnailView {

    private Listener listener;

    private final LazyThumbnailLayout thumbnailLayout = new LazyThumbnailLayout();;

    public ThumbnailViewImpl() {
        thumbnailLayout.setSizeFull();
        thumbnailLayout.addStyleName("mgnl-workbench-thumbnail-view");
        bindHandlers();
    }

    private void bindHandlers() {
        thumbnailLayout.addThumbnailSelectionListener(new ThumbnailSelectionListener() {

            @Override
            public void onThumbnailSelected(final String thumbnailId) {
                Item node = thumbnailLayout.getContainerDataSource().getItem(thumbnailId);
                listener.onItemSelection(node);
            }
        });

        thumbnailLayout.addDoubleClickListener(new ThumbnailDblClickListener() {

            @Override
            public void onThumbnailDblClicked(final String thumbnailId) {
                Item node = thumbnailLayout.getContainerDataSource().getItem(thumbnailId);
                listener.onDoubleClick(node);
            }
        });
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void select(String path) {
        // do something?
    }

    @Override
    public void refresh() {
        thumbnailLayout.refresh();
    }

    @Override
    public void setContainer(Container container) {
        thumbnailLayout.setContainerDataSource(container);
    }

    @Override
    public void setThumbnailSize(int width, int height) {
        thumbnailLayout.setThumbnailSize(width, height);
    }

    @Override
    public ViewType getViewType() {
        return ViewType.THUMBNAIL;
    }

    @Override
    public Component asVaadinComponent() {
        return thumbnailLayout;
    }
}
