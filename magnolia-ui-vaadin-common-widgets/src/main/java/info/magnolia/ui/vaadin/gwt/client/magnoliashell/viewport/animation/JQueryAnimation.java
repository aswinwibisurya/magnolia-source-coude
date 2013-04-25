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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.animation;

import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.AnimationSettings;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.Callbacks;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryCallback;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.Duration;
import com.google.gwt.dom.client.Element;

/**
 * GWT Animation wrapper for JQuery Animations.
 */
public class JQueryAnimation extends Animation {

    private AnimationSettings settings = new AnimationSettings();

    private Callbacks callbacks = Callbacks.create();

    private JQueryWrapper jQueryWrapper;

    private Element currentElement;

    private boolean wasCancelled = false;

    public void setProperty(String property, int value) {
        settings.setProperty(property, value);
    }

    public void setProperty(String property, double value) {
        settings.setProperty(property, value);
    }

    public void addCallback(JQueryCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void run(int duration, double startTime, Element element) {
        runDelayed(duration, startTime - Duration.currentTimeMillis(), element);
    }

    public void runDelayed(int duration, double delay, Element element) {
        this.currentElement = element;
        this.jQueryWrapper = JQueryWrapper.select((com.google.gwt.user.client.Element)element);
        double currentTime = Duration.currentTimeMillis();
        super.run(duration, currentTime + delay, element);
        this.wasCancelled = false;
        jQueryWrapper.animate(duration, settings);
    }

    protected JQueryWrapper getJQueryWrapper() {
        if (jQueryWrapper == null) {
            jQueryWrapper = JQueryWrapper.select((com.google.gwt.user.client.Element)currentElement);
        }
        return jQueryWrapper;
    }

    @Override
    public void run(int duration) {
        super.run(duration, currentElement);
    }

    @Override
    protected void onUpdate(double progress) {}

    @Override
    public void cancel() {
        if (getJQueryWrapper().isAnimationInProgress()) {
            getJQueryWrapper().stop();
        }
        wasCancelled = true;
        super.cancel();
    }

    @Override
    protected void onComplete() {
        super.onComplete();
        if (!wasCancelled) {
            callbacks.fire(getJQueryWrapper());
        }
    }

    public boolean wasCanceled() {
        return wasCancelled;
    }

    public Element getCurrentElement() {
        return currentElement;
    }
}
