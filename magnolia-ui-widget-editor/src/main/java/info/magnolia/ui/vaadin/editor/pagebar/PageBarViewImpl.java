/**
 * This file Copyright (c) 2013-2014 Magnolia International
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
package info.magnolia.ui.vaadin.editor.pagebar;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.vaadin.editor.gwt.shared.PlatformType;

import java.util.List;
import java.util.Locale;

import com.vaadin.data.Property;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

/**
 * Implements {@link PageBarView}.
 */
public class PageBarViewImpl extends CustomComponent implements PageBarView {

    private CssLayout root = new CssLayout();

    private Label pageNameLabel = new Label();

    private ComboBox languageSelector = new ComboBox();
    private ComboBox platformSelector = new ComboBox();

    private PageBarView.Listener listener;

    public PageBarViewImpl() {
        super();
        setCompositionRoot(root);
        construct();
    }

    private void construct() {
        root.addStyleName("pagebar");

        for (PlatformType type : PlatformType.values()) {
            platformSelector.addItem(type);
        }
        platformSelector.setSizeUndefined();
        platformSelector.setImmediate(true);
        platformSelector.setNullSelectionAllowed(false);
        platformSelector.setTextInputAllowed(false);
        platformSelector.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (listener != null) {
                    listener.platformSelected((PlatformType) event.getProperty().getValue());
                }
            }
        });

        languageSelector.setSizeUndefined();
        languageSelector.setImmediate(true);
        languageSelector.setNullSelectionAllowed(false);
        languageSelector.setTextInputAllowed(false);
        languageSelector.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (listener != null) {
                    listener.languageSelected((Locale) event.getProperty().getValue());
                }
            }
        });

        this.platformSelector.setValue(PlatformType.DESKTOP);
        this.pageNameLabel.setSizeUndefined();
        this.pageNameLabel.addStyleName("title");

        root.addComponent(pageNameLabel);
        root.addComponent(languageSelector);
        root.addComponent(platformSelector);
    }

    @Override
    public void setPageName(String pageTitle, String path) {
        String label = pageTitle.toUpperCase() + "  -  " + path;
        this.pageNameLabel.setValue(label);
    }

    @Override
    public void setListener(PageBarView.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setCurrentLanguage(Locale locale) {
        if (languageSelector != null) {
            languageSelector.setValue(locale);
        }
    }

    @Override
    public void setAvailableLanguages(List<Locale> locales) {
        if (locales != null && !locales.isEmpty()) {
            languageSelector.removeAllItems();
            for (Locale locale : locales) {
                String label = locale.getDisplayLanguage(MgnlContext.getLocale());
                if (!locale.getDisplayCountry(MgnlContext.getLocale()).isEmpty()) {
                    label += " (" + locale.getDisplayCountry(MgnlContext.getLocale()) + ")";
                }
                languageSelector.addItem(locale);
                languageSelector.setItemCaption(locale, label);
            }
            languageSelector.setVisible(true);
        } else {
            languageSelector.setVisible(false);
        }
    }

    @Override
    public void togglePreviewMode(boolean isPreview) {
        platformSelector.setVisible(isPreview);
        if (isPreview) {
            root.addStyleName("preview");
        } else {
            root.removeStyleName("preview");
        }
    }

    @Override
    public void setPlatFormType(PlatformType targetPreviewPlatform) {
        platformSelector.setValue(targetPreviewPlatform);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

}
