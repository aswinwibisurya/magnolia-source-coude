/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.pages.app.editor.availability;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.pages.app.editor.parameters.DefaultPageEditorStatus;
import info.magnolia.pages.app.editor.parameters.PageEditorStatus;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockRepositoryAcquiringStrategy;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;

import java.util.Locale;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link IsPreviewRule}.
 */
public class IsPreviewRuleTest {

    private IsPreviewRuleDefinition definition;
    private IsPreviewRule rule;
    private PageEditorStatus pageEditorStatus;

    @Before
    public void setUp() throws Exception {
        MockWebContext ctx = new MockWebContext();
        MockSession session =  new MockSession(RepositoryConstants.WEBSITE);
        ctx.addSession(null, session);
        MockRepositoryAcquiringStrategy strategy = new MockRepositoryAcquiringStrategy();
        strategy.addSession(RepositoryConstants.WEBSITE, session);
        ctx.setRepositoryStrategy(strategy);
        MgnlContext.setInstance(ctx);

        I18nContentSupport i18nContentSupport = mock(I18nContentSupport.class);
        when(i18nContentSupport.getLocale()).thenReturn(new Locale("en"));
        ComponentsTestUtil.setInstance(I18nContentSupport.class, i18nContentSupport);

        I18NAuthoringSupport i18NAuthoringSupport = mock(I18NAuthoringSupport.class);
        when(i18NAuthoringSupport.createI18NURI(any(Node.class), any(Locale.class))).thenReturn("/");

        this.definition = mock(IsPreviewRuleDefinition.class);

        pageEditorStatus = new DefaultPageEditorStatus(i18NAuthoringSupport);
        PageEditorPresenter pageEditorPresenter = mock(PageEditorPresenter.class);

        when(pageEditorPresenter.getStatus()).thenReturn(pageEditorStatus);
        this.rule = new IsPreviewRule(definition, pageEditorPresenter);

    }

    @Test
    public void testExpectingToBeInPreviewAndIsPreview() throws Exception {
        // GIVEN
        when(definition.isPreview()).thenReturn(true);
        pageEditorStatus.updateStatusFromLocation(new DetailLocation("pages", "detail", DetailView.ViewType.VIEW, "/", null));

        // WHEN
        boolean result = rule.isAvailableForItem(mock(JcrItemId.class));

        assertTrue(result);

    }

    @Test
    public void testExpectingToBeInPreviewAndIsNotPreview() throws Exception {
        // GIVEN
        when(definition.isPreview()).thenReturn(true);
        pageEditorStatus.updateStatusFromLocation(new DetailLocation("pages", "detail", DetailView.ViewType.EDIT, "/", null));

        // WHEN
        boolean result = rule.isAvailableForItem(mock(JcrItemId.class));

        assertFalse(result);

    }
}
