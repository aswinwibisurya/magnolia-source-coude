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
package info.magnolia.ui.vaadin.integration.widget.client.applauncher;

import info.magnolia.ui.widget.jquerywrapper.gwt.client.AnimationSettings;
import info.magnolia.ui.widget.jquerywrapper.gwt.client.JQueryWrapper;

import com.google.web.bindery.event.shared.EventBus;

/**
 * The temporary app group.
 */
public class VTemporaryAppTileGroup extends VAppTileGroup {

    private static final int OPEN_STATE_HEIGHT_PX = 80;

    private static final int VISIBILITY_TOGGLE_SPEED = 200; 
    
    public VTemporaryAppTileGroup(EventBus eventBus, String color) {
        super(color);
        construct();
    }

    @Override
    protected void construct() {
        addStyleName("temporary");
        closeSection();
    }

    @Override
    public void addAppThumbnail(VAppTile thumbnail) {
        super.addAppThumbnail(thumbnail);
    }

    public void closeSection() {
        JQueryWrapper.select(this).animate(VISIBILITY_TOGGLE_SPEED, new AnimationSettings() {{
            setProperty("height", 0);
        }});
    }

    public void showSection() {
        JQueryWrapper.select(this).animate(VISIBILITY_TOGGLE_SPEED, new AnimationSettings() {{
            setProperty("height", OPEN_STATE_HEIGHT_PX);
        }});
    }

}
