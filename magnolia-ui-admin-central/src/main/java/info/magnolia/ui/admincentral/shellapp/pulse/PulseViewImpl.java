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
package info.magnolia.ui.admincentral.shellapp.pulse;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.admincentral.components.ActivityItem;
import info.magnolia.ui.admincentral.components.SplitFeed;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMainLauncher.ShellAppType;
import info.magnolia.ui.widget.tabsheet.ShellTab;
import info.magnolia.ui.widget.tabsheet.ShellTabSheet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Default view implementation for Pulse.
 *
 * @version $Id$
 */
@SuppressWarnings("serial")
public class PulseViewImpl implements PulseView, IsVaadinComponent {

    private ShellTabSheet tabsheet = new ShellTabSheet() {

        @Override
        public void onActiveTabSet(String tabId) {
            super.onActiveTabSet(tabId);
            presenter.onPulseTabChanged(m.getKey(getTabById(tabId)).toString().toLowerCase());
        }
    };

    private enum PulseTabType {
        PULSE,
        STATS,
        MESSAGES,
        INBOX;

        public static PulseTabType getDefault() {
            return PULSE;
        }
    }

    private MagnoliaShell shell;
    
    private MessagesManager messagesManager;
    
    private Presenter  presenter;

    private BidiMap m = new DualHashBidiMap();

    @Inject
    public PulseViewImpl(MessagesManager messagesManager, MagnoliaShell shell) {
        this.shell = shell;
        this.messagesManager = messagesManager;
        tabsheet.addStyleName("v-pulse");
        final Panel testLayout2 = new Panel();
        final Panel testLayout3 = new Panel();

        testLayout2.setSizeFull();
        testLayout2.addComponent(new Label("Test2".toUpperCase()));
        testLayout3.setSizeFull();
        testLayout3.addComponent(new Label("Test3".toUpperCase()));

        final SplitFeed pulseFeed = new SplitFeed();
        pulseFeed.getLeftContainer().setTitle("Activity Stream");
        pulseFeed.getLeftContainer().setTitleLinkEnabled(true);

        pulseFeed.getRightContainer().setTitle("Pages I changed recently");
        pulseFeed.getRightContainer().setTitleLinkEnabled(true);
       

        ComponentContainer messagesTable = createMessagesLayout();

        final ShellTab pulse = tabsheet.addTab("pulse".toUpperCase(), pulseFeed);
        final ShellTab stats = tabsheet.addTab("stats".toUpperCase(), testLayout2);
        final ShellTab messages = tabsheet.addTab("messages".toUpperCase(), messagesTable);
        final ShellTab inbox = tabsheet.addTab("inbox".toUpperCase(), new VerticalLayout());

        tabsheet.updateTabNotification(stats, "4");
        tabsheet.updateTabNotification(messages, "2");
        tabsheet.updateTabNotification(inbox, "1");

        tabsheet.setSizeFull();
        tabsheet.setWidth("900px");

        final Label l = new Label("Today");
        l.addStyleName("category-separartor");
        pulseFeed.getLeftContainer().addComponent(l);
        pulseFeed.getLeftContainer().addComponent(new ActivityItem("Test", "Lorem ipsum...", "Say hi", "green", new Date()));
        pulseFeed.getLeftContainer().addComponent(new ActivityItem("Test", "Lorem ipsum once again", "Say hi", "red", new Date()));

        m.put(PulseTabType.PULSE, pulse);
        m.put(PulseTabType.STATS, stats);
        m.put(PulseTabType.MESSAGES, messages);
        m.put(PulseTabType.INBOX, inbox);
    }

    private ComponentContainer createMessagesLayout() {

        final IndexedContainer container = new IndexedContainer();
        container.addContainerProperty("new", String.class, null);
        container.addContainerProperty("type", String.class,  null);
        container.addContainerProperty("text", String.class,  null);
        container.addContainerProperty("sender", String.class,  null);
        container.addContainerProperty("date", String.class,  null);

        for (Message message : messagesManager.getMessagesForUser(MgnlContext.getUser().getName())) {
            addMessageToContainer(container, message);
        }

        messagesManager.registerMessagesListener(MgnlContext.getUser().getName(), new MessagesManager.MessageListener() {

            @Override
            public void messageSent(Message message) {
                addMessageToContainer(container, message);
                if (message.getType() != MessageType.INFO) {
                    shell.updateShellAppIndication(ShellAppType.PULSE, 1);   
                }
            }

            @Override
            public void messageCleared(Message message) {
                /**
                 * FIXME: we do not remove message here, but in MessagesManager those are removed.
                 * The logic needs to be clearer somehow.
                 */
                //container.removeItem(message.getId());
                if (message.getType() != MessageType.INFO) {
                    shell.updateShellAppIndication(ShellAppType.PULSE, -1);
                }
            }
        });

        Table table = new Table();
        table.setSizeFull();
        table.setContainerDataSource(container);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponent(table);
        return layout;
    }

    private void addMessageToContainer(IndexedContainer container, Message message) {
        final Item item = container.addItem(message.getId());
        item.getItemProperty("type").setValue(message.getType().name());
        item.getItemProperty("text").setValue(message.getMessage());
        item.getItemProperty("date").setValue(new SimpleDateFormat().format(new Date(message.getTimestamp())));
    }

    @Override
    public Component asVaadinComponent() {
        return tabsheet;
    }

    @Override
    public String setCurrentPulseTab(final String tabId, final List<String> params) {
        PulseTabType type = null;
        String finalDisplayedTabId = tabId;
        try {
            type = PulseTabType.valueOf(String.valueOf(tabId).toUpperCase());
        } catch (IllegalArgumentException e) {
            type = PulseTabType.getDefault();
            finalDisplayedTabId = type.name().toLowerCase();
        }
        final ShellTab tab = (ShellTab)m.get(type);
        if (tab != null) {
            tabsheet.setActiveTab(tab);
        }
        return finalDisplayedTabId;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
}
