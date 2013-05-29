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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import info.magnolia.cms.i18n.MessagesUtil;

import java.util.Iterator;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Message category navigation component in Pulse.
 */
public final class PulseMessageCategoryNavigator extends CssLayout {

    private CheckBox groupByTypeCheckBox;

    public PulseMessageCategoryNavigator() {
        super();
        setStyleName("navigator");
        construct();
    }

    private void construct() {
        for (final MessageCategory category : MessageCategory.values()) {
            MessageCategoryButton button = new MessageCategoryButton(category);
            if (category == MessageCategory.ALL) {
                button.setActive(true);
            }
            addComponent(button);
        }

        groupByTypeCheckBox = new CheckBox(MessagesUtil.get("pulse.messages.groupby"));
        groupByTypeCheckBox.addStyleName("navigator-grouping");
        groupByTypeCheckBox.setImmediate(true);
        addComponent(groupByTypeCheckBox);

    }

    public void addGroupingListener(ValueChangeListener listener) {
        groupByTypeCheckBox.addValueChangeListener(listener);
    }

    public void showGroupByType(boolean show) {
        groupByTypeCheckBox.setVisible(show);
    }

    /**
     * Enumeration for the category types.
     */
    public enum MessageCategory {
        ALL(MessagesUtil.get("pulse.messages.all")),
        WORK_ITEM(MessagesUtil.get("pulse.messages.workitems")),
        PROBLEM(MessagesUtil.get("pulse.messages.problems")),
        INFO(MessagesUtil.get("pulse.messages.info"));

        private String caption;

        private MessageCategory(final String caption) {
            this.caption = caption;
        }

        public String getCaption() {
            return caption;
        }
    }

    /**
     * Category changed event.
     */
    public static class CategoryChangedEvent extends Component.Event {

        public static final java.lang.reflect.Method MESSAGE_CATEGORY_CHANGED;

        static {
            try {
                MESSAGE_CATEGORY_CHANGED = MessageCategoryChangedListener.class.getDeclaredMethod("messageCategoryChanged", new Class[]{CategoryChangedEvent.class});
            } catch (final java.lang.NoSuchMethodException e) {
                throw new java.lang.RuntimeException(e);
            }
        }

        private final MessageCategory category;

        public CategoryChangedEvent(Component source, MessageCategory category) {
            super(source);
            this.category = category;
        }

        public MessageCategory getCategory() {
            return category;
        }
    }

    /**
     * MessageCategoryChangedListener.
     */
    public interface MessageCategoryChangedListener {

        public void messageCategoryChanged(final CategoryChangedEvent event);
    }

    public void addCategoryChangeListener(final MessageCategoryChangedListener listener) {
        addListener("category_changed", CategoryChangedEvent.class, listener, CategoryChangedEvent.MESSAGE_CATEGORY_CHANGED);
    }

    private void fireCategoryChangedEvent(MessageCategory category) {
        Iterator<Component> iterator = getComponentIterator();
        while (iterator.hasNext()) {
            Component component = iterator.next();
            if (component instanceof MessageCategoryButton) {
                MessageCategoryButton button = (MessageCategoryButton) component;
                button.setActive(button.category == category);
            }
        }
        fireEvent(new CategoryChangedEvent(this, category));
    }

    /**
     * Message category button.
     */
    public class MessageCategoryButton extends CustomComponent {

        private final HorizontalLayout root = new HorizontalLayout();
        private final MessageCategory category;
        private final NativeButton button;
        private final Label badge;

        public MessageCategoryButton(MessageCategory category) {
            super();
            this.category = category;
            addStyleName("navigator-button");

            button = new NativeButton();
            button.setStyleName(BaseTheme.BUTTON_LINK);
            button.setCaption(category.getCaption());
            button.addClickListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    fireCategoryChangedEvent(MessageCategoryButton.this.category);
                }
            });
            root.setSizeUndefined();
            root.addComponent(button);

            badge = new Label();
            badge.addStyleName("badge");
            badge.setVisible(false);

            root.setSizeFull();
            root.addComponent(button);
            root.addComponent(badge);

            setCompositionRoot(root);
        }

        public void setActive(boolean active) {
            if (active) {
                addStyleName("active");
            } else {
                removeStyleName("active");
            }
        }

        public void updateMessagesCount(int count) {
            if (count <= 0) {
                badge.setVisible(false);
            } else {
                badge.setValue(String.valueOf(count));
                badge.setVisible(true);
            }
        }
    }
}
