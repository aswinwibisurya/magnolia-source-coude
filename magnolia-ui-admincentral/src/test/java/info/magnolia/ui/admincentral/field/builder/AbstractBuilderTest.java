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
package info.magnolia.ui.admincentral.field.builder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.model.field.definition.FieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Locale;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;

import com.vaadin.data.Item;


/**
 * Abstract test class used to initialize the DialogField Tests.
 */
public abstract class AbstractBuilderTest<D extends FieldDefinition> {
    protected static final Locale DEFAULT_LOCALE = new Locale("en");
    protected DefaultI18nContentSupport i18nContentSupport;
    protected final String workspaceName = "workspace";
    protected MockSession session;
    protected String propertyName = "propertyName";
    protected String itemName = "item";
    protected Node baseNode;
    protected Item baseItem;
    protected D definition;

    @Before
    public void setUp() throws Exception {
        // Init Message & Providers
        DefaultMessagesManager manager = new DefaultMessagesManager();
        ComponentsTestUtil.setInstance(MessagesManager.class, manager);
        SystemContext systemContext = mock(SystemContext.class);
        when(systemContext.getLocale()).thenReturn(DEFAULT_LOCALE);
        ComponentsTestUtil.setInstance(SystemContext.class, systemContext);

        //Init Session
        session = new MockSession(workspaceName);
        MockContext ctx = new MockContext();
        ctx.addSession(workspaceName, session);
        MgnlContext.setInstance(ctx);

        //Create ConfiguredField POJO
        createConfiguredFieldDefinition();

        //Init Node and Item.
        Node rootNode = session.getRootNode();
        baseNode = rootNode.addNode(itemName);
        baseItem = new JcrNodeAdapter(baseNode);

        // Init i18n
        i18nContentSupport = new DefaultI18nContentSupport();
        i18nContentSupport.setFallbackLocale(DEFAULT_LOCALE);

    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    /**
     * Create the specific ConfiguredFieldDefinition or sub class.
     */
    protected abstract void createConfiguredFieldDefinition();

}