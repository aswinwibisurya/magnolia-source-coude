/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
package info.magnolia.ui.dialog.setup;

import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.dialog.setup.migration.ActionCreator;
import info.magnolia.ui.dialog.setup.migration.BaseActionCreator;
import info.magnolia.ui.dialog.setup.migration.CheckBoxRadioControlMigrator;
import info.magnolia.ui.dialog.setup.migration.CheckBoxSwitchControlMigrator;
import info.magnolia.ui.dialog.setup.migration.ControlMigrator;
import info.magnolia.ui.dialog.setup.migration.DateControlMigrator;
import info.magnolia.ui.dialog.setup.migration.EditCodeControlMigrator;
import info.magnolia.ui.dialog.setup.migration.EditControlMigrator;
import info.magnolia.ui.dialog.setup.migration.FckEditControlMigrator;
import info.magnolia.ui.dialog.setup.migration.FileControlMigrator;
import info.magnolia.ui.dialog.setup.migration.HiddenControlMigrator;
import info.magnolia.ui.dialog.setup.migration.LinkControlMigrator;
import info.magnolia.ui.dialog.setup.migration.MultiSelectControlMigrator;
import info.magnolia.ui.dialog.setup.migration.SelectControlMigrator;
import info.magnolia.ui.dialog.setup.migration.StaticControlMigrator;
import info.magnolia.ui.form.field.definition.StaticFieldDefinition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog migration main task.<br>
 * Migrate all dialogs defined within the specified module.<br>
 */
public class DialogMigrationTask extends AbstractTask {

    private static final Logger log = LoggerFactory.getLogger(DialogMigrationTask.class);
    private final String moduleName;
    private final HashSet<Property> extendsAndReferenceProperty = new HashSet<Property>();

    private HashMap<String, ControlMigrator> controlsToMigrate;
    private String defaultDialogActions = "defaultDialogActions";
    private HashMap<String, List<ActionCreator>> dialogActionsToMigrate;
    protected InstallContext installContext;

    /**
     * @param taskName
     * @param taskDescription
     * @param moduleName all dialog define under this module name will be migrated.
     * @param customControlsToMigrate Custom controls to migrate.
     * @param customDialogActionsToMigrate Custom actions to migrate
     */
    public DialogMigrationTask(String taskName, String taskDescription, String moduleName, HashMap<String, ControlMigrator> customControlsToMigrate, HashMap<String, List<ActionCreator>> customDialogActionsToMigrate) {
        super(taskName, taskDescription);
        this.moduleName = moduleName;
        registerControlsToMigrate(customControlsToMigrate);
        registerDialogActionToCreate(customDialogActionsToMigrate);
    }

    public DialogMigrationTask(String taskName, String taskDescription, String moduleName) {
        this(taskName, taskDescription, moduleName, null, null);
    }

    public DialogMigrationTask(String moduleName) {
        this("Dialog Migration for 5.x", "Migrate dialog for the following module: " + moduleName, moduleName, null, null);
    }

    /**
     * Handle all Dialogs registered and migrate them.
     */
    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        Session session = null;
        this.installContext = installContext;
        try {
            addCustomControlsToMigrate(controlsToMigrate);
            addCustomDialogActionToCreate(dialogActionsToMigrate);

            String dialogNodeName = "dialogs";
            String dialogPath = "/modules/" + moduleName + "/" + dialogNodeName;
            session = installContext.getJCRSession(RepositoryConstants.CONFIG);

            // Check
            if (!session.itemExists(dialogPath)) {
                log.warn("Dialog definition do not exist for the following module {}. No Dialog migration task will be performed", moduleName);
                return;
            }
            Node dialog = session.getNode(dialogPath);
            NodeUtil.visit(dialog, new NodeVisitor() {
                @Override
                public void visit(Node current) throws RepositoryException {
                    for (Node dialogNode : NodeUtil.getNodes(current, NodeTypes.ContentNode.NAME)) {
                        performDialogMigration(dialogNode);
                    }
                }
            }, new NodeTypePredicate(NodeTypes.Content.NAME));

            // Try to resolve references for extends.
            postProcessForExtendsAndReference();

        } catch (Exception e) {
            log.error("", e);
            installContext.warn("Could not Migrate Dialog for the following module " + moduleName);
            throw new TaskExecutionException("Could not Migrate Dialog ", e);
        }
    }

    /**
     * Register default UI controls to migrate.
     */
    private void registerControlsToMigrate(HashMap<String, ControlMigrator> customControlsToMigrate) {
        this.controlsToMigrate = new HashMap<String, ControlMigrator>();
        // Register default controls
        this.controlsToMigrate.put("edit", new EditControlMigrator());
        this.controlsToMigrate.put("fckEdit", new FckEditControlMigrator());
        this.controlsToMigrate.put("date", new DateControlMigrator());
        this.controlsToMigrate.put("select", new SelectControlMigrator());
        this.controlsToMigrate.put("checkbox", new CheckBoxRadioControlMigrator(true));
        this.controlsToMigrate.put("checkboxSwitch", new CheckBoxSwitchControlMigrator());
        this.controlsToMigrate.put("radio", new CheckBoxRadioControlMigrator(false));
        this.controlsToMigrate.put("uuidLink", new LinkControlMigrator());
        this.controlsToMigrate.put("link", new LinkControlMigrator());
        this.controlsToMigrate.put("multiselect", new MultiSelectControlMigrator(false));
        this.controlsToMigrate.put("file", new FileControlMigrator());
        this.controlsToMigrate.put("static", new StaticControlMigrator());
        this.controlsToMigrate.put("hidden", new HiddenControlMigrator());
        this.controlsToMigrate.put("editCode", new EditCodeControlMigrator());
        // Register custom
        if (customControlsToMigrate != null) {
            this.controlsToMigrate.putAll(customControlsToMigrate);
        }
    }

    /**
     * Override this method in order to register custom controls to migrate.<br>
     * In case a control name is already define in the default map, the old control migrator is replaced by the newly registered control migrator.
     * 
     * @param controlsToMigrate. <br>
     * - key : controls name <br>
     * - value : {@link ControlMigrator} used to take actions in order to migrate the control into a field.
     */
    protected void addCustomControlsToMigrate(HashMap<String, ControlMigrator> controlsToMigrate) {
    }

    /**
     * Register default actions to create on dialogs.
     */
    private void registerDialogActionToCreate(HashMap<String, List<ActionCreator>> customDialogActionsToMigrate) {
        this.dialogActionsToMigrate = new HashMap<String, List<ActionCreator>>();
        // Register default
        // Save
        ActionCreator saveAction = new BaseActionCreator("commit", "save changes", "info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition");
        // Cancel
        ActionCreator cancelAction = new BaseActionCreator("cancel", "cancel", "info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition");
        // Create an entry
        this.dialogActionsToMigrate.put(this.defaultDialogActions, Arrays.asList(saveAction, cancelAction));
        // Register custom
        if (customDialogActionsToMigrate != null) {
            this.dialogActionsToMigrate.putAll(customDialogActionsToMigrate);
        }
    }

    /**
     * Override this method in order to register custom actions to create on a specific dialog.<br>
     * 
     * @param dialogActionsToMigrate.<br>
     * - key: Dialog name <br>
     * - value: List of {@link ActionCreator} to create on the desired dialog.
     */
    protected void addCustomDialogActionToCreate(HashMap<String, List<ActionCreator>> dialogActionsToMigrate) {
    }

    /**
     * Handle and Migrate a Dialog node.
     */
    private void performDialogMigration(Node dialog) throws RepositoryException {
        // Get child Nodes (should be Tab)
        Iterable<Node> tabNodes = NodeUtil.getNodes(dialog, DIALOG_FILTER);
        if (tabNodes.iterator().hasNext()) {
            // Check if it's a tab definition
            if (dialog.hasProperty("controlType") && dialog.getProperty("controlType").getString().equals("tab")) {
                handleTab(dialog);
            } else {
                // Handle action
                if (!dialog.hasProperty("controlType") && !dialog.hasProperty("extends") && !dialog.hasProperty("reference")) {
                    handleAction(dialog);
                }
                // Handle tab
                handleTabs(dialog, tabNodes.iterator());
            }
            // Remove class property defined on Dialog level
            if (dialog.hasProperty("class")) {
                dialog.getProperty("class").remove();
            }
        } else {
            // Handle as a field.
            handleField(dialog);
        }

        handleExtendsAndReference(dialog);
    }

    /**
     * Add action to node.
     */
    private void handleAction(Node dialog) throws RepositoryException {
        // Create actions node
        dialog.addNode("actions", NodeTypes.ContentNode.NAME);
        Node actionsNode = dialog.getNode("actions");

        List<ActionCreator> actions = dialogActionsToMigrate.get(defaultDialogActions);
        //Use the specific Actions list if defined
        if (dialogActionsToMigrate.containsKey(dialog.getName())) {
            actions = dialogActionsToMigrate.get(dialog.getName());
        }

        for (ActionCreator action : actions) {
            action.create(actionsNode);
        }

    }

    /**
     * Handle Tabs.
     */
    private void handleTabs(Node dialog, Iterator<Node> tabNodes) throws RepositoryException {
        Node form = dialog.addNode("form", NodeTypes.ContentNode.NAME);
        handleFormLabels(dialog, form);
        Node dialogTabs = form.addNode("tabs", NodeTypes.ContentNode.NAME);
        while (tabNodes.hasNext()) {
            Node tab = tabNodes.next();
            // Handle Fields Tab
            handleTab(tab);
            // Move tab
            NodeUtil.moveNode(tab, dialogTabs);
        }
    }

    /**
     * Move the label property from dialog to form node.
     */
    private void handleFormLabels(Node dialog, Node form) throws RepositoryException {
        moveAndRenameLabelProperty(dialog, form, "label");
        moveAndRenameLabelProperty(dialog, form, "i18nBasename");
        moveAndRenameLabelProperty(dialog, form, "description");
    }

    /**
     * Move the desired property if present from the source to the target node.
     */
    private void moveAndRenameLabelProperty(Node source, Node target, String propertyName) throws RepositoryException {
        if (source.hasProperty(propertyName)) {
            Property dialogProperty = source.getProperty(propertyName);
            target.setProperty(propertyName, dialogProperty.getString());
            dialogProperty.remove();
        }
    }

    /**
     * Handle a Tab.
     */
    private void handleTab(Node tab) throws RepositoryException {
        if ((tab.hasProperty("controlType") && StringUtils.equals(tab.getProperty("controlType").getString(), "tab")) || (tab.getParent().hasProperty("extends"))) {
            if (tab.hasProperty("controlType") && StringUtils.equals(tab.getProperty("controlType").getString(), "tab")) {
                // Remove controlType Property
                tab.getProperty("controlType").remove();
            }
            // get all controls to be migrated
            Iterator<Node> controls = NodeUtil.getNodes(tab, NodeTypes.ContentNode.NAME).iterator();
            // create a fields Node
            Node fields = tab.addNode("fields", NodeTypes.ContentNode.NAME);

            while (controls.hasNext()) {
                Node control = controls.next();
                // Handle fields
                handleField(control);
                // Move to fields
                NodeUtil.moveNode(control, fields);
            }
        } else if (tab.hasNode("inheritable")) {
            // Handle inheritable
            Node inheritable = tab.getNode("inheritable");
            handleExtendsAndReference(inheritable);
        } else {
            handleExtendsAndReference(tab);
        }
    }

    /**
     * Change controlType to the equivalent class.
     * Change the extend path.
     */
    private void handleField(Node fieldNode) throws RepositoryException {
        if (fieldNode.hasProperty("controlType")) {
            String controlTypeName = fieldNode.getProperty("controlType").getString();

            if (controlsToMigrate.containsKey(controlTypeName)) {
                ControlMigrator controlMigration = controlsToMigrate.get(controlTypeName);
                controlMigration.migrate(fieldNode);
            } else {
                fieldNode.setProperty("class", StaticFieldDefinition.class.getName());
                if (!fieldNode.hasProperty("value")) {
                    fieldNode.setProperty("value", "Field not yet supported");
                }
                log.warn("No dialog define for control '{}' for node '{}'", controlTypeName, fieldNode.getPath());
            }
        } else {
            // Handle Field Extends/Reference
            handleExtendsAndReference(fieldNode);
        }
    }


    private void handleExtendsAndReference(Node node) throws RepositoryException {
        if (node.hasProperty("extends")) {
            // Handle Field Extends
            extendsAndReferenceProperty.add(node.getProperty("extends"));
        } else if (node.hasProperty("reference")) {
            // Handle Field Extends
            extendsAndReferenceProperty.add(node.getProperty("reference"));
        }
    }

    /**
     * Create a specific node filter.
     */
    private static AbstractPredicate<Node> DIALOG_FILTER = new AbstractPredicate<Node>() {
        @Override
        public boolean evaluateTyped(Node node) {
            try {
                return !node.getName().startsWith(NodeTypes.JCR_PREFIX)
                        && !NodeUtil.isNodeType(node, NodeTypes.MetaData.NAME) &&
                        NodeUtil.isNodeType(node, NodeTypes.ContentNode.NAME);
            } catch (RepositoryException e) {
                return false;
            }
        }
    };

    /**
     * Check if the extends and reference are correct. If not try to do the best
     * to found a correct path.
     */
    private void postProcessForExtendsAndReference() throws RepositoryException {
        for (Property p : extendsAndReferenceProperty) {
            String path = p.getString();
            if (path.equals("override")) {
                continue;
            }
            if (!p.getSession().nodeExists(path)) {

                String newPath = insertBeforeLastSlashAndTest(p.getSession(), path, "/tabs", "/fields", "/tabs/fields", "/form/tabs");
                if (newPath != null) {
                    p.setValue(newPath);
                    continue;
                }

                // try to add a tabs before the 2nd last /
                String beging = path.substring(0, path.lastIndexOf("/"));
                String end = path.substring(beging.lastIndexOf("/"));
                beging = beging.substring(0, beging.lastIndexOf("/"));
                newPath = beging + "/form/tabs" + end;
                if (p.getSession().nodeExists(newPath)) {
                    p.setValue(newPath);
                    continue;
                }
                // try with a fields before the last / with a tabs before the 2nd last /
                newPath = insertBeforeLastSlash(newPath, "/fields");
                if (p.getSession().nodeExists(newPath)) {
                    p.setValue(newPath);
                } else {
                    log.warn("reference to " + path + " not found");
                }
            }
        }
    }

    /**
     * Test insertBeforeLastSlash() for all toInserts.
     * If newPath exist as a node return it.
     */
    private String insertBeforeLastSlashAndTest(Session session, String reference, String... toInserts) throws RepositoryException {
        String res = null;
        for (String toInsert : toInserts) {
            String newPath = insertBeforeLastSlash(reference, toInsert);
            if (session.nodeExists(newPath)) {
                return newPath;
            }
        }
        return res;
    }

    /**
     * Insert the toInsert ("/tabs") before the last /.
     */
    private String insertBeforeLastSlash(String reference, String toInsert) {
        String beging = reference.substring(0, reference.lastIndexOf("/"));
        String end = reference.substring(reference.lastIndexOf("/"));
        return beging + toInsert + end;
    }

}
