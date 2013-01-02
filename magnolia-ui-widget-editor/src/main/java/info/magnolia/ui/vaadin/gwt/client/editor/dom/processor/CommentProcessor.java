/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.editor.dom.processor;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.Comment;
import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlElement;
import info.magnolia.ui.vaadin.gwt.client.editor.model.Model;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * Processor for comment elements.
 */
public class CommentProcessor {


    public MgnlElement process(Model model, Node node, MgnlElement currentElement) throws IllegalArgumentException {

        CMSComment comment = getCmsComment(node);

        // in case we fail, we want to keep the currentElement as current.
        MgnlElement mgnlElement = currentElement;

        if (!comment.isClosing()) {

            try {

                mgnlElement = createMgnlElement(comment, currentElement);
                String nodeData = node.getNodeValue();
                mgnlElement.setStartComment((Element) node.cast());

                if (mgnlElement.getParent() == null) {
                    model.setRootPage(mgnlElement);
                }
                else if (mgnlElement.getParent().isPage()) {
                    model.addRootArea(mgnlElement);
                    mgnlElement.getParent().getChildren().add(mgnlElement);
                }
                else {
                    mgnlElement.getParent().getChildren().add(mgnlElement);
                }

            } catch (IllegalArgumentException e) {
                GWT.log("Not MgnlElement, skipping: " + e.toString());
            }


        }
        // the cms:page tag should span throughout the page, but doesn't: kind of a hack.
        else if (currentElement != null && !currentElement.isPage()) {
            currentElement.setEndComment((Element) node.cast());
            mgnlElement = currentElement.getParent().asMgnlElement();
        }

        return mgnlElement;

    }

    private CMSComment getCmsComment(Node node) throws IllegalArgumentException {

        CMSComment cmsComment = new CMSComment();

        Comment domComment = node.cast();
        String comment = domComment.getData().trim();

        GWT.log("processing comment " + comment);

        String tagName = "";
        boolean isClosing = false;

        int delimiter = comment.indexOf(" ");
        String attributeString = "";

        if (delimiter < 0){
            tagName = comment;
        }
        else {
            tagName = comment.substring(0, delimiter);
            attributeString = comment.substring(delimiter + 1);
        }

        if (tagName.startsWith("/")) {
            isClosing = true;
            tagName = tagName.substring(1);
        }


        if (tagName.startsWith(Model.CMS_TAG)) {
            cmsComment.setTagName(tagName);
            cmsComment.setAttribute(attributeString);
            cmsComment.setClosing(isClosing);

        }
        else {

            throw new IllegalArgumentException("Tagname must start with +'" + Model.CMS_TAG + "'.");
        }
        return cmsComment;


    }

    private Map<String, String> getAttributes(String attributeString, MgnlElement parent) {
        String[] keyValue;
        Map<String, String> attributes = new HashMap<String, String>();

        RegExp regExp = RegExp.compile("(\\S+=[\"'][^\"]*[\"'])", "g");
        for (MatchResult matcher = regExp.exec(attributeString); matcher != null; matcher = regExp.exec(attributeString)) {
            keyValue = matcher.getGroup(0).split("=");
            if (keyValue[0].equals("content")) {
                String content = keyValue[1].replace("\"", "");
                int i = content.indexOf(':');
                attributes.put("workspace", content.substring(0, i));
                attributes.put("path", content.substring(i + 1));
            }
            else {
                attributes.put(keyValue[0], keyValue[1].replace("\"", ""));
            }
        }
        if (parent != null) {
            for (String inheritedAttribute : Model.INHERITED_ATTRIBUTES) {
                if (parent.asMgnlElement().containsAttribute(inheritedAttribute)) {
                    attributes.put(inheritedAttribute, parent.asMgnlElement().getAttribute(inheritedAttribute));
                }
            }
        }
        return attributes;
    }


    private  MgnlElement createMgnlElement(CMSComment comment, MgnlElement parent) throws IllegalArgumentException {
        String tagName = comment.getTagName();
        MgnlElement mgnlElement;
        if (Model.CMS_PAGE.equals(tagName)) {
            mgnlElement = new MgnlElement(parent);
            mgnlElement.setPage(true);
        }
        else if (Model.CMS_AREA.equals(tagName)) {
            mgnlElement = new MgnlElement(parent);
            mgnlElement.setArea(true);

        }
        else if (Model.CMS_COMPONENT.equals(tagName)) {
            mgnlElement = new MgnlElement(parent);
            mgnlElement.setComponent(true);
        }
        else {
            throw new IllegalArgumentException("The tagname must be one of the defined marker Strings.");
        }

        mgnlElement.setAttributes(getAttributes(comment.getAttributes(), parent));

        return mgnlElement;
    }

    /**
     * CmsComment.
     */
    private class CMSComment {


        private String tagName;
        private String attributes;
        private boolean isClosing = false;

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        public boolean isClosing() {
            return isClosing;
        }

        public void setClosing(boolean isClosing) {
            this.isClosing = isClosing;
        }

        public void setAttribute(String attributes) {
            this.attributes = attributes;
        }

        public String getAttributes() {
            return attributes;
        }
    }
}
