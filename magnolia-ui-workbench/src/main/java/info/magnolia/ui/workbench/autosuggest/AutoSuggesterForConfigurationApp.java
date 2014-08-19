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
package info.magnolia.ui.workbench.autosuggest;

import info.magnolia.cms.beans.config.DefaultVirtualURIMapping;
import info.magnolia.commands.chain.Command;
import info.magnolia.jcr.node2bean.PropertyTypeDescriptor;
import info.magnolia.jcr.node2bean.TypeDescriptor;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.jcr.wrapper.DelegateNodeWrapper;
import info.magnolia.jcr.wrapper.ExtendingNodeWrapper;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.renderer.AbstractRenderer;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.api.autosuggest.AutoSuggester;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import info.magnolia.ui.form.fieldtype.definition.ConfiguredFieldTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns suggestions and how to display them for a cell in the Configuration App tree.
 */
public class AutoSuggesterForConfigurationApp implements AutoSuggester {

    private static Logger log = LoggerFactory.getLogger(AutoSuggesterForConfigurationApp.class);

    private TypeMapping typeMapping = null;

    public AutoSuggesterForConfigurationApp() {
        typeMapping = Components.getComponentProvider().getComponent(TypeMapping.class);
        if (typeMapping == null) {
            log.warn("Could not get TypeMapping using component provider.");
        }
    }

    @Override
    public AutoSuggesterResult getSuggestionsFor(Object itemId, Object propertyId) {
        if (itemId == null || propertyId == null) {
            return noSuggestionsAvailable();
        }

        // If processing a JCR node
        if (itemId instanceof JcrNodeItemId) {
            Node node = getNodeFromJcrNodeItemId((JcrNodeItemId) itemId);

            if (node != null) {
                // If processing name field of a node
                if ("jcrName".equals(propertyId)) {
                    return getSuggestionsForNameOfNode(node);
                }
                // If not processing name field of a node
                else {
                    return noSuggestionsAvailable();
                }
            }
            else {
                return noSuggestionsAvailable();
            }
        }
        // If processing a JCR property
        else if (itemId instanceof JcrPropertyItemId) {
            JcrPropertyItemId propertyItemId = (JcrPropertyItemId) itemId;

            // If processing name field of a property
            if ("jcrName".equals(propertyId)) {
                return getSuggestionsForNameOfProperty(propertyItemId);
            }
            // If processing value field of a property
            else if ("value".equals(propertyId)) {
                return getSuggestionsForValueOfProperty(propertyItemId);
            }
            // If processing type field of a property
            else if ("type".equals(propertyId)) {
                return getSuggestionsForTypeOfProperty(propertyItemId);
            }
            // If not processing name, value, or type field of a property
            else {
                return noSuggestionsAvailable();
            }
        }
        // If neither processing a JCR node nor a JCR property
        else {
            return noSuggestionsAvailable();
        }
    }

    protected AutoSuggesterResult noSuggestionsAvailable() {
        return new AutoSuggesterForConfigurationAppResult();
    }

    protected AutoSuggesterResult getSuggestionsForNameOfNode(Node node) {
        if (node == null) {
            return noSuggestionsAvailable();
        }

        try {
            Node parentNode = node.getParent();

            // If processing a node that has a parent node
            if (parentNode != null) {
                TypeDescriptor parentNodeTypeDescriptor = getNodeTypeDescriptor(parentNode);

                // If processing name field of a node whose parent has no class
                if (parentNodeTypeDescriptor == null) {
                    return getSuggestionsForNameOfNodeInUnknown(node, parentNode);
                }
                // If processing name field of a node whose parent is an array
                else if (parentNodeTypeDescriptor.isArray()) {
                    return getSuggestionsForNameOfNodeInArray(parentNode, parentNodeTypeDescriptor);
                }
                // If processing name field of a node whose parent is a collection
                else if (parentNodeTypeDescriptor.isCollection()) {
                    return getSuggestionsForNameOfNodeInCollection(parentNode, parentNodeTypeDescriptor);
                }
                // If processing name field of a node whose parent is a map
                else if (parentNodeTypeDescriptor.isMap()) {
                    return getSuggestionsForNameOfNodeInMap(parentNode, parentNodeTypeDescriptor);
                }
                // If processing name field of a node whose parent is a bean
                else {
                    return getSuggestionsForNameOfNodeInBean(node, parentNode, parentNodeTypeDescriptor);
                }
            }
            // If processing a node that does not have a parent
            else {
                return noSuggestionsAvailable();
            }
        } catch (RepositoryException ex) {
            log.warn("Could not get suggestions for name of node: " + ex);
            return noSuggestionsAvailable();
        }
    }

    /**
     * Get suggestions for node name when node has a parent but the parent's type is unknown.
     * This includes the case where the parent node is a folder.
     */
    protected AutoSuggesterResult getSuggestionsForNameOfNodeInUnknown(final Node node, final Node parentNode) {
        // QUESTION Are there other cases where we can recommend a name for the node when its parent type is unknown?

        if (node == null || parentNode == null) {
            return noSuggestionsAvailable();
        }

        try {
            // If node is a folder and node's parent is a folder
            if (NodeUtil.isNodeType(node, NodeTypes.Content.NAME) && NodeUtil.isNodeType(parentNode, NodeTypes.Content.NAME)) {
                String parentPath = parentNode.getPath();

                // If node's parent's path is available
                if (parentPath != null) {

                    // If node's parent is /modules/<moduleName>
                    if (parentPath.startsWith("/modules/") && parentPath.indexOf("/", "/modules/".length()) == -1) {

                        // QUESTION Would it not be better to make suggestions based on existing subfolders under /modules/<moduleName>/ rather than hardcoding it?
                        final Collection<String> suggestions = getAllPossibleNewSubnodeNames(node, parentNode, Arrays.asList(
                                "apps", "templates", "dialogs", "commands", "fieldTypes", "virtualURIMapping", "renderers", "config"));

                        return new AutoSuggesterForConfigurationAppResult(suggestions != null && !suggestions.isEmpty(), suggestions, AutoSuggesterResult.STARTS_WITH, true, false);
                    }
                    // If node's parent is not /modules/<moduleName>
                    else {
                        return noSuggestionsAvailable();
                    }
                }
                // If node's parent's path is not available
                else {
                    return noSuggestionsAvailable();
                }
            }
            // If node is not a folder or node's parent is not a folder
            else {
                return noSuggestionsAvailable();
            }
        } catch (RepositoryException ex) {
            log.warn("Could not get suggestions for name of node when type of parent is unknown: " + ex);
            return noSuggestionsAvailable();
        }
    }

    protected AutoSuggesterResult getSuggestionsForNameOfNodeInMap(Node parentNode, TypeDescriptor parentNodeTypeDescriptor) {
        // TODO
        // QUESTION Are there special cases where we can make suggestions for the name of node in a map?
        return noSuggestionsAvailable();
    }

    protected AutoSuggesterResult getSuggestionsForNameOfNodeInCollection(Node parentNode, TypeDescriptor parentNodeTypeDescriptor) {
        // TODO
        // QUESTION Are there special cases where we can make suggestions for the name of node in a collection?
        return noSuggestionsAvailable();
    }

    protected AutoSuggesterResult getSuggestionsForNameOfNodeInArray(Node parentNode, TypeDescriptor parentNodeTypeDescriptor) {
        // TODO
        // QUESTION Are there special cases where we can make suggestions for the name of node in an array?
        return noSuggestionsAvailable();
    }

    protected AutoSuggesterResult getSuggestionsForNameOfNodeInBean(final Node node, final Node parentNode, final TypeDescriptor parentNodeTypeDescriptor) {
        if (node == null || parentNode == null || parentNodeTypeDescriptor == null) {
            return noSuggestionsAvailable();
        }

        final Collection<String> possibleSubnodeNames = getAllPossibleSubnodeNames(parentNodeTypeDescriptor);
        if (possibleSubnodeNames != null) {
            final Collection<String> suggestions = getAllPossibleNewSubnodeNames(node, parentNode, possibleSubnodeNames);

            return new AutoSuggesterForConfigurationAppResult(suggestions != null && !suggestions.isEmpty(), suggestions, AutoSuggesterResult.STARTS_WITH, true, true);
        }
        else {
            return noSuggestionsAvailable();
        }
    }

    protected AutoSuggesterResult getSuggestionsForNameOfProperty(JcrPropertyItemId propertyItemId) {
        // TODO
        return noSuggestionsAvailable();
    }

    protected AutoSuggesterResult getSuggestionsForValueOfProperty(JcrPropertyItemId propertyItemId) {
        // TODO
        return noSuggestionsAvailable();
    }

    protected AutoSuggesterResult getSuggestionsForTypeOfProperty(JcrPropertyItemId propertyItemId) {
        // TODO
        return noSuggestionsAvailable();
    }

    // UTILITY METHODS

    private Node getNodeFromJcrNodeItemId(JcrNodeItemId nodeItemId) {
        if (nodeItemId == null) {
            return null;
        }

        return SessionUtil.getNodeByIdentifier(nodeItemId.getWorkspace(), nodeItemId.getUuid());
    }

    /**
     * Get the type of a node. Take into account the class property of the node, class property of
     * ancestors, and location of the node within the configuration hierarchy. Take extends into
     * account. Take implementations mapped by ComponentProvider into account. Take into account
     * that some types cannot be types for nodes. Return null if the type cannot be deduced.
     */
    private TypeDescriptor getNodeTypeDescriptor(final Node node) {
        if (node == null) {
            return null;
        }

        NodeAndEntryTypeDescriptor nodeAndEntryTypeDescriptor = getNodeAndEntryTypeDescriptor(node);

        if (nodeAndEntryTypeDescriptor != null) {
            return nodeAndEntryTypeDescriptor.getTypeDescriptor();
        }
        else {
            return null;
        }
    }

    /**
     * Get the type of the node and the type of the entry if the node is an array, collection, or map.
     * Take into account the class property of the node, class property of ancestors, and location of
     * the node within the configuration hierarchy. Take extends into account. Take implementations
     * mapped by ComponentProvider into account. Take into account that some types cannot be types for
     * nodes. Return null if the type cannot be deduced.
     */
    private NodeAndEntryTypeDescriptor getNodeAndEntryTypeDescriptor(final Node node) {
        if (typeMapping == null) {
            log.warn("Could not get type for node because TypeMapping does not exist.");
            return null;
        }

        if (node == null) {
            return null;
        }

        // Take into account properties inherited due to extends
        final Node extendedNode = wrapNodeAndAncestorsForExtends(node);
        if (extendedNode == null) {
            return null;
        }

        try {
            // Try to get the type based on node's class property
            NodeAndEntryTypeDescriptor nodeAndEntryTypeDescriptor = getNodeAndEntryTypeDescriptorBasedOnClassProperty(extendedNode);

            // If we can get the type based on node's class property
            if (nodeAndEntryTypeDescriptor != null) {
                return nodeAndEntryTypeDescriptor;
            }
            // If we can't get the type based on node's class property
            else {
                // If node does not have class property or has a class property but we can't use it to get the type, try to get type based on parent
                Node parentNode = extendedNode.getParent();

                // If node has a parent
                if (parentNode != null) {

                    // Try to use path information to get the type of the node
                    nodeAndEntryTypeDescriptor = getNodeAndEntryTypeDescriptorBasedOnParentFolder(extendedNode, parentNode);

                    // If we are able to use path information to get the type of the node
                    if (nodeAndEntryTypeDescriptor != null) {
                        return nodeAndEntryTypeDescriptor;
                    }
                    // If we are not able to use path information to get the type of the node
                    else {
                        // Recursively get type of parent
                        NodeAndEntryTypeDescriptor parentNodeAndEntryTypeDescriptor = getNodeAndEntryTypeDescriptor(parentNode);

                        // If we can get the type of the parent using recursion
                        if (parentNodeAndEntryTypeDescriptor != null) {
                            TypeDescriptor parentTypeDescriptor = parentNodeAndEntryTypeDescriptor.getTypeDescriptor();

                            if (parentTypeDescriptor != null) {

                                // If parent is array, collection, or map
                                if (parentTypeDescriptor.isArray() || parentTypeDescriptor.isCollection() || parentTypeDescriptor.isMap()) {
                                    TypeDescriptor parentEntryTypeDescriptor = parentNodeAndEntryTypeDescriptor.getEntryTypeDescriptor();

                                    // QUESTION How do we deal with a parent whose type is a Collection<Collection<T>>?
                                    // If parent's entry type is known and is for a content node
                                    if (parentEntryTypeDescriptor != null && isTypeDescriptorForContentNode(parentEntryTypeDescriptor)) {
                                        return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(parentEntryTypeDescriptor), null);
                                    }
                                    // If parent's entry type is either not known or is not for a content node
                                    else {
                                        return null;
                                    }
                                }
                                // If parent is not array, collection, or map, and thus assumed to be a bean
                                else {
                                    PropertyTypeDescriptor nodePropertyTypeDescriptor = getPropertyTypeDescriptor(extendedNode.getName(), parentTypeDescriptor);

                                    // If can use the name of the node to get the type from the parent
                                    if (nodePropertyTypeDescriptor != null) {
                                        TypeDescriptor nodeTypeDescriptor = nodePropertyTypeDescriptor.getType();

                                        // If type gotten from the parent is for a content node
                                        if (nodeTypeDescriptor != null && isTypeDescriptorForContentNode(nodeTypeDescriptor)) {

                                            // If the type of the node is an array, collection, or map
                                            if (nodeTypeDescriptor.isArray() || nodeTypeDescriptor.isCollection() || nodeTypeDescriptor.isMap()) {
                                                return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(nodeTypeDescriptor),
                                                        getImplementingTypeDescriptor(nodePropertyTypeDescriptor.getCollectionEntryType()));
                                            }
                                            // If the type of the node is not array, collection, or map
                                            else {
                                                return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(nodeTypeDescriptor), null);
                                            }
                                        }
                                        // If type gotten from the parent is not for a content node
                                        else {
                                            return null;
                                        }
                                    }
                                    // If cannot use the name of the node to get the type from the parent
                                    else {
                                        return null;
                                    }
                                }
                            }
                            else {
                                return null;
                            }
                        }
                        // If cannot get the type of the parent using recursion
                        else {
                            return null;
                        }
                    }
                }
                // If node does not have a parent
                else {
                    return null;
                }
            }

        } catch (RepositoryException ex) {
            log.warn("Could not get TypeDescriptor for node: " + ex);
            return null;
        }
    }

    /**
     * Try to use class property of a node to get its type. Take extends into account. Take implementations
     * mapped by ComponentProvider into account. Returns null if the node does not have a class property or
     * its value is invalid.
     */
    private NodeAndEntryTypeDescriptor getNodeAndEntryTypeDescriptorBasedOnClassProperty(Node node) {
        if (typeMapping == null) {
            log.warn("Could not get type of node based on class property because TypeMapping does not exist.");
            return null;
        }

        if (node == null) {
            return null;
        }

        // Take into account properties inherited due to extends
        final Node extendedNode = wrapNodeAndAncestorsForExtends(node);
        if (extendedNode == null) {
            return null;
        }

        try {
            // If node has a class property
            if (extendedNode.hasProperty("class")) {
                Property classProperty = extendedNode.getProperty("class");

                // If node has class property and we can get the class property value
                if (classProperty != null) {
                    try {
                        TypeDescriptor nodeTypeDescriptor = typeMapping.getTypeDescriptor(Class.forName(classProperty.getString()));

                        // If we can get the TypeDescriptor based on the class
                        if (nodeTypeDescriptor != null) {
                            return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(nodeTypeDescriptor), null);
                        }
                        // If we can't get the TypeDescriptor based on the class
                        else {
                            return null;
                        }
                    } catch (ClassNotFoundException ex) {
                        log.warn("Could not get TypeDescriptor based on invalid value for class property: " + ex);
                        return null;
                    }
                }
                // If node has class property but we can't get the value
                else {
                    return null;
                }
            }
            // If node does not have a class property
            else {
                return null;
            }
        } catch (RepositoryException ex) {
            log.warn("Could not get TypeDescriptor for node based on class property: " + ex);
            return null;
        }
    }

    /**
     * Try to use path information to get the type of a node. For example, if all a node's ancestors are
     * folders and node is in /modules/<moduleName>/apps/, then we can assume it is a ConfiguredAppDescriptor.
     * Similarly for templates, dialogs, etc. Take implementations mapped by ComponentProvider into account.
     * Returns null if the type cannot be guessed based on folders.
     */
    private NodeAndEntryTypeDescriptor getNodeAndEntryTypeDescriptorBasedOnParentFolder(Node node, Node parentNode) {
        if (typeMapping == null) {
            log.warn("Could not get type of node based on path because TypeMapping does not exist.");
            return null;
        }

        if (node == null || parentNode == null) {
            return null;
        }

        try {
            // If node is a content node and node's parent is a folder and thus all its ancestors are folders
            if (NodeUtil.isNodeType(node, NodeTypes.ContentNode.NAME) && NodeUtil.isNodeType(parentNode, NodeTypes.Content.NAME)) {
                String nodePath = node.getPath();
                if (nodePath == null) {
                    return null;
                }

                // If node is in /modules/
                if (nodePath.startsWith("/modules/")) {
                    int indexOfModuleSubfolderNameEnd = nodePath.indexOf("/", "/modules/".length());

                    // If node is in a subfolder of /modules/
                    if (indexOfModuleSubfolderNameEnd != -1) {
                        String nodePathStartingAfterSubfolderOfModule = nodePath.substring(indexOfModuleSubfolderNameEnd + 1);

                        // If node is in /modules/<moduleName>/apps/
                        if (nodePathStartingAfterSubfolderOfModule.startsWith("apps/")) {
                            return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(typeMapping.getTypeDescriptor(ConfiguredAppDescriptor.class)), null);
                        }
                        // If node is in /modules/<moduleName>/templates/
                        else if (nodePathStartingAfterSubfolderOfModule.startsWith("templates/")) {
                            return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(typeMapping.getTypeDescriptor(ConfiguredTemplateDefinition.class)), null);
                        }
                        // If node is in /modules/<moduleName>/dialogs/
                        else if (nodePathStartingAfterSubfolderOfModule.startsWith("dialogs/")) {
                            return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(typeMapping.getTypeDescriptor(ConfiguredFormDialogDefinition.class)), null);
                        }
                        // If node is in /modules/<moduleName>/commands/
                        else if (nodePathStartingAfterSubfolderOfModule.startsWith("commands/")) {
                            return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(typeMapping.getTypeDescriptor(Command.class)), null);
                        }
                        // If node is in /modules/<moduleName>/fieldTypes/
                        else if (nodePathStartingAfterSubfolderOfModule.startsWith("fieldTypes/")) {
                            return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(typeMapping.getTypeDescriptor(ConfiguredFieldTypeDefinition.class)), null);
                        }
                        // If node is in /modules/<moduleName>/virtualURIMapping/
                        else if (nodePathStartingAfterSubfolderOfModule.startsWith("virtualURIMapping/")) {
                            return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(typeMapping.getTypeDescriptor(DefaultVirtualURIMapping.class)), null);
                        }
                        // If node is in /modules/<moduleName>/renderers/
                        else if (nodePathStartingAfterSubfolderOfModule.startsWith("renderers/")) {
                            return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(typeMapping.getTypeDescriptor(AbstractRenderer.class)), null);
                        }
                        // QUESTION Are there more cases when we can tell the type of a node based on its path?
                        // If node is not in a recognized subfolder of /modules/<moduleName>/
                        else {
                            return null;
                        }
                    }
                    // If node is directly under /modules/
                    else {
                        return null;
                    }
                }
                // If node is not in /modules/
                else {
                    return null;
                }
            }
            // If node is not a content node or node's parent is not a folder
            else {
                return null;
            }
        } catch (RepositoryException ex) {
            log.warn("Could not get TypeDescriptor for node based on path: " + ex);
            return null;
        }
    }

    private boolean isTypeDescriptorForContentNode(TypeDescriptor typeDescriptor) {
        if (typeDescriptor == null) {
            return false;
        }

        Class<?> clazz = typeDescriptor.getType();
        if (clazz == null) {
            return false;
        }

        // QUESTION Is there a better way to check that a class is for a bean?
        return !(ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isEnum() || clazz.equals(String.class) || clazz.equals(Class.class));
    }

    private Node wrapNodeAndAncestorsForExtends(Node node) {
        if (node == null) {
            return null;
        }

        try {
            if (!NodeUtil.isWrappedWith(node, ExtendingNodeAndAncestorsWrapper.class)) {
                return new ExtendingNodeAndAncestorsWrapper(node);
            }
            else {
                return node;
            }
        } catch (RepositoryException ex) {
            log.warn("Could not wrap node in ExtendingNodeAndAncestorsWrapper: " + ex);
            return null;
        }
    }

    /**
     * Same as TypeDescriptor.getPropertyTypeDescriptor(String propertyName) except we compensate for Oracle Java bug 4275879
     * by looking for property recursively in super-interfaces of an interface as well.
     */
    private PropertyTypeDescriptor getPropertyTypeDescriptor(String propertyName, TypeDescriptor parentTypeDescriptor) {
        if (typeMapping == null) {
            log.warn("Could not get property type descriptor from type descriptor because TypeMapping does not exist.");
            return null;
        }

        if (propertyName == null || parentTypeDescriptor == null) {
            return null;
        }

        TypeDescriptor implementedParentTypeDescriptor = getImplementingTypeDescriptor(parentTypeDescriptor);
        if (implementedParentTypeDescriptor == null) {
            return null;
        }

        PropertyTypeDescriptor propertyTypeDescriptor = implementedParentTypeDescriptor.getPropertyTypeDescriptor(propertyName, typeMapping);

        // If we are able to get the PropertyTypeDescriptor through type mapping
        if (propertyTypeDescriptor != null) {
            return propertyTypeDescriptor;
        }
        // If we are not able to get the PropertyTypeDescriptor through type mapping
        else {
            Class<?> parentClass = implementedParentTypeDescriptor.getType();

            // If parent's type is an interface, property may still be inherited from a super-interface
            if (parentClass != null && parentClass.isInterface()) {
                Class<?>[] superInterfaceClasses = parentClass.getInterfaces();

                if (superInterfaceClasses != null) {

                    // Recursively look for propertyTypeDescriptor in super-interfaces
                    for (Class<?> superInterfaceClass : superInterfaceClasses) {
                        TypeDescriptor superInterfaceTypeDescriptor = typeMapping.getTypeDescriptor(superInterfaceClass);

                        if (superInterfaceTypeDescriptor != null) {
                            PropertyTypeDescriptor superInterfacePropertyTypeDescriptor = getPropertyTypeDescriptor(propertyName, superInterfaceTypeDescriptor);

                            if (superInterfacePropertyTypeDescriptor != null) {
                                return superInterfacePropertyTypeDescriptor;
                            }
                        }
                    }

                    // Did not find propertyTypeDescriptor in super-interfaces
                    return null;
                }
                else {
                    return null;
                }
            }
            // If parent's type is not known or not an interface
            else {
                return null;
            }
        }
    }

    /**
     * If typeDescriptor is for an interface and the componentProvider maps it to an implementing type,
     * return the typeDescriptor of the implementing type. If not, then return original typeDescriptor.
     */
    private TypeDescriptor getImplementingTypeDescriptor(final TypeDescriptor typeDescriptor) {
        if (typeMapping == null) {
            log.warn("Could not get implementing type descriptor because TypeMapping does not exist.");
            return typeDescriptor;
        }

        if (typeDescriptor == null) {
            return null;
        }

        try {
            Class<?> interfaceClass = typeDescriptor.getType();

            if (interfaceClass != null) {
                Class<?> implementingClass = Components.getComponentProvider().getImplementation(interfaceClass);

                if (implementingClass != null) {
                    TypeDescriptor implementingTypeDescriptor = typeMapping.getTypeDescriptor(implementingClass);

                    if (implementingTypeDescriptor != null) {
                        return implementingTypeDescriptor;
                    }
                    else {
                        return typeDescriptor;
                    }
                }
                else {
                    return typeDescriptor;
                }
            }
            else {
                log.warn("Could not get implementing type descriptor because original type descriptor does not specify a class.");
                return typeDescriptor;
            }
        } catch (ClassNotFoundException ex) {
            log.warn("Could not get implementing type descriptor because could not get implementing class: " + ex);
            return typeDescriptor;
        }
    }

    /**
     * For a node which is a bean, get all possible subnode names. Use the implementing class mapped
     * to the type by ComponentProvider to deduce the subnode names.
     */
    private Collection<String> getAllPossibleSubnodeNames(TypeDescriptor nodeTypeDescriptor) {
        if (typeMapping == null) {
            log.warn("Could not get subnode names because TypeMapping does not exist.");
            return null;
        }

        if (nodeTypeDescriptor == null) {
            return null;
        }

        TypeDescriptor implementedNodeTypeDescriptor = getImplementingTypeDescriptor(nodeTypeDescriptor);
        if (implementedNodeTypeDescriptor == null) {
            return null;
        }

        // QUESTION Is there a better way to check that a TypeDescriptor is for a bean?
        // If the node is not a bean
        if (implementedNodeTypeDescriptor.isArray() || implementedNodeTypeDescriptor.isCollection() || implementedNodeTypeDescriptor.isMap()) {
            return null;
        }
        // If the node is a bean
        else {
            Map<String, PropertyTypeDescriptor> propertyTypeDescriptors = getAllPropertyTypeDescriptors(implementedNodeTypeDescriptor);

            if (propertyTypeDescriptors != null) {
                Collection<String> possibleSubnodeNames = new HashSet<String>();

                for (PropertyTypeDescriptor propertyTypeDescriptor : propertyTypeDescriptors.values()) {
                    TypeDescriptor typeDescriptor = propertyTypeDescriptor.getType();

                    if (typeDescriptor != null) {

                        if (isTypeDescriptorForContentNode(typeDescriptor)) {
                            String propertyName = propertyTypeDescriptor.getName();

                            if (propertyName != null) {
                                possibleSubnodeNames.add(propertyName);
                            }
                        }
                    }
                }

                return possibleSubnodeNames;
            }
            else {
                return null;
            }
        }
    }

    /**
     * Get all names from possibleSubnodeNames that are not already subnodes of parentNode.
     * If one of the names matches the current node's name and the current node is a subnode
     * of parentNode, include it also.
     */
    private Collection<String> getAllPossibleNewSubnodeNames(Node node, Node parentNode, Collection<String> possibleSubnodeNames) {
        Collection<String> possibleNewSubnodeNames = new HashSet<String>();

        if (node == null || parentNode == null || possibleSubnodeNames == null) {
            return null;
        }

        try {
            // Add all names in possibleSubnodeNames that are not already subnodes of parentNode
            for (String possibleSubnodeName : possibleSubnodeNames) {
                if (!parentNode.hasNode(possibleSubnodeName)) {
                    possibleNewSubnodeNames.add(possibleSubnodeName);
                }
            }

            // Add current node name as well if it is one of the possibleSubnodeNames and is a subnode of parentNode
            String nodeName = node.getName();
            if (nodeName != null && possibleSubnodeNames.contains(nodeName) && parentNode.hasNode(nodeName)) {
                possibleNewSubnodeNames.add(nodeName);
            }

            return possibleNewSubnodeNames;
        } catch (RepositoryException ex) {
            log.warn("Could not get nonexisting subnode names: " + ex);
            return null;
        }
    }

    /**
     * Same as TypeDescriptor.getPropertyTypeDescriptors() except we compensate for Oracle Java bug 4275879
     * by looking for properties recursively in super-interfaces of an interface as well.
     */
    private Map<String, PropertyTypeDescriptor> getAllPropertyTypeDescriptors(TypeDescriptor parentTypeDescriptor) {
        if (typeMapping == null) {
            log.warn("Could not get all property type descriptors from type descriptor because TypeMapping does not exist.");
            return null;
        }

        if (parentTypeDescriptor == null) {
            return null;
        }

        TypeDescriptor implementedParentTypeDescriptor = getImplementingTypeDescriptor(parentTypeDescriptor);
        if (implementedParentTypeDescriptor == null) {
            return null;
        }

        Map<String, PropertyTypeDescriptor> propertyTypeDescriptors = implementedParentTypeDescriptor.getPropertyDescriptors(typeMapping);

        if (propertyTypeDescriptors != null) {
            Class<?> parentClass = implementedParentTypeDescriptor.getType();

            // If parent is an interface
            if (parentClass != null && parentClass.isInterface()) {
                Class<?>[] superInterfaceClasses = parentClass.getInterfaces();

                if (superInterfaceClasses != null) {
                    Map<String, PropertyTypeDescriptor> collectedPropertyTypeDescriptors = new HashMap<String, PropertyTypeDescriptor>();
                    collectedPropertyTypeDescriptors.putAll(propertyTypeDescriptors);

                    // Recursively look for propertyTypeDescriptor in super-interfaces
                    for (Class<?> superInterfaceClass : superInterfaceClasses) {
                        TypeDescriptor superInterfaceTypeDescriptor = typeMapping.getTypeDescriptor(superInterfaceClass);

                        if (superInterfaceTypeDescriptor != null) {
                            Map<String, PropertyTypeDescriptor> superInterfacePropertyTypeDescriptors = getAllPropertyTypeDescriptors(superInterfaceTypeDescriptor);

                            if (superInterfacePropertyTypeDescriptors != null) {
                                for (Map.Entry<String, PropertyTypeDescriptor> superInterfacePropertyTypeDescriptor : superInterfacePropertyTypeDescriptors.entrySet()) {
                                    if (!collectedPropertyTypeDescriptors.containsKey(superInterfacePropertyTypeDescriptor.getKey())) {
                                        collectedPropertyTypeDescriptors.put(superInterfacePropertyTypeDescriptor.getKey(), superInterfacePropertyTypeDescriptor.getValue());
                                    }
                                }
                            }
                        }
                    }

                    return collectedPropertyTypeDescriptors;
                }
                else {
                    return propertyTypeDescriptors;
                }
            }
            // If parent class is not known or parent is not an interface
            else {
                return propertyTypeDescriptors;
            }
        }
        else {
            return null;
        }
    }

    /**
     * Convenient adapter for AutoSuggesterResult.
     */
    private static class AutoSuggesterForConfigurationAppResult implements AutoSuggesterResult {
        private boolean suggestionsAvailable;
        private Collection<String> suggestions;
        private int matchMethod;
        boolean showMismatchedSuggestions;
        boolean showErrorHighlighting;

        public AutoSuggesterForConfigurationAppResult(boolean suggestionsAvailable, Collection<String> suggestions, int matchMethod, boolean showMismatchedSuggestions, boolean showErrorHighlighting) {
            this.suggestionsAvailable = suggestionsAvailable;
            this.suggestions = suggestions;
            this.matchMethod = matchMethod;
            this.showMismatchedSuggestions = showMismatchedSuggestions;
            this.showErrorHighlighting = showErrorHighlighting;
        }

        /**
         * The result constructed by default has no suggestions available.
         */
        public AutoSuggesterForConfigurationAppResult() {
            this(false, null, STARTS_WITH, false, false);
        }

        @Override
        public boolean suggestionsAvailable() {
            if (!suggestionsAvailable) {
                return false;
            }
            else if (suggestions == null || suggestions.isEmpty()) {
                return false;
            }
            else {
                return true;
            }
        }

        @Override
        public Collection<String> getSuggestions() {
            return suggestions;
        }

        @Override
        public int getMatchMethod() {
            return matchMethod;
        }

        @Override
        public boolean showMismatchedSuggestions() {
            return showMismatchedSuggestions;
        }

        @Override
        public boolean showErrorHighlighting() {
            return showErrorHighlighting;
        }
    }

    /**
     * Wrapper that wraps both the node and its ancestors in {@link ExtendingNodeWrapper}, taking
     * into account items that may be inherited due to nodes extended by ancestors.
     */
    private static class ExtendingNodeAndAncestorsWrapper extends DelegateNodeWrapper {
        private ExtendingNodeAndAncestorsWrapper parent = null;

        public ExtendingNodeAndAncestorsWrapper(Node node) throws RepositoryException {
            if (node.getDepth() == 0) {
                this.parent = null;
                setWrappedNode(new ExtendingNodeWrapper(node));
            }
            else {
                this.parent = new ExtendingNodeAndAncestorsWrapper(node.getParent());
                setWrappedNode(this.parent.getNode(node.getName()));
            }
        }

        @Override
        public Node getParent() {
            return this.parent;
        }
    }

    /**
     * Convenience class so that we can return both the TypeDescriptor for a node and
     * the TypeDescriptor for the entry type if the node is an array, collection, or map
     * at the same time.
     */
    private static class NodeAndEntryTypeDescriptor {
        private TypeDescriptor typeDescriptor;
        private TypeDescriptor entryTypeDescriptor;

        public NodeAndEntryTypeDescriptor(TypeDescriptor typeDescriptor, TypeDescriptor entryTypeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            this.entryTypeDescriptor = entryTypeDescriptor;
        }

        public TypeDescriptor getTypeDescriptor() {
            return typeDescriptor;
        }

        public TypeDescriptor getEntryTypeDescriptor() {
            return entryTypeDescriptor;
        }
    }
}
