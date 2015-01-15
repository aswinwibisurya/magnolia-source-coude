/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.framework.message;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.framework.AdmincentralNodeTypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores messages on behalf of {@link MessagesManager} in the repository, every user in the system has its own set of
 * messages that have ids unique in combination with their userid. Ids are generated by taking the largest id in use and
 * incrementing it by 1.
 */
@Singleton
public class MessageStore {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    static final String WORKSPACE_NAME = "messages";
    static final String MESSAGE_NODE_TYPE = AdmincentralNodeTypes.SystemMessage.NAME;

    private static final String WORKSPACE_PATH = "/";
    private static final String USER_NODE_TYPE = NodeTypes.Content.NAME;

    /**
     * Stores a new message or overwrites an existing one depending on whether there's an id set. That is, the id of the
     * message is respected if present otherwise a new unique one is used. When the method returns the message has been
     * updated with a new id.
     * 
     * @param userName user to save the message for
     * @param message message to save
     * @return true if saving was successful or false if it failed
     */
    public boolean saveMessage(final String userName, final Message message) {

        return MgnlContext.doInSystemContext(new MgnlContext.Op<Boolean, RuntimeException>() {

            @Override
            public Boolean exec() {
                try {
                    Session session = MgnlContext.getJCRSession(WORKSPACE_NAME);

                    if (message.getId() == null) {
                        message.setId(getUniqueMessageId(getOrCreateUserNode(session, userName)));
                    }

                    marshallMessage(message, getOrCreateMessageNode(session, userName, message));

                    session.save();

                    return true;

                } catch (RepositoryException e) {
                    logger.error("Saving message failed for user: " + userName, e);
                    return false;
                }
            }
        });
    }

    public int getNumberOfUnclearedMessagesForUser(final String userName) {

        return MgnlContext.doInSystemContext(new MgnlContext.Op<Integer, RuntimeException>() {

            @Override
            public Integer exec() throws RuntimeException {
                try {
                    Session session = MgnlContext.getJCRSession(WORKSPACE_NAME);

                    int n = 0;
                    for (Node messageNode : NodeUtil.getNodes(getOrCreateUserNode(session, userName), MESSAGE_NODE_TYPE)) {
                        if (!messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.CLEARED).getBoolean()) {
                            n++;
                        }
                    }
                    return n;

                } catch (RepositoryException e) {
                    logger.warn("Failed to find the number of uncleared messages for user: " + userName, e);
                    return 0;
                }
            }
        });
    }

    public List<Message> findAllMessagesForUser(final String userName) {
        return MgnlContext.doInSystemContext(new MgnlContext.Op<List<Message>, RuntimeException>() {

            @Override
            public List<Message> exec() throws RuntimeException {
                try {
                    Session session = MgnlContext.getJCRSession(WORKSPACE_NAME);

                    ArrayList<Message> messages = new ArrayList<Message>();

                    for (Node messageNode : NodeUtil.getNodes(getOrCreateUserNode(session, userName), MESSAGE_NODE_TYPE)) {

                        Message message = unmarshallMessage(messageNode);

                        messages.add(message);
                    }
                    return messages;

                } catch (RepositoryException e) {
                    logger.error("Retrieving messages from JCR failed for user: " + userName, e);
                    return new ArrayList<Message>();
                } catch (Node2BeanException e) {
                    logger.error("Unmarshalling message failed for user: " + userName, e);
                    return new ArrayList<Message>();
                }
            }
        });
    }

    public Message findMessageById(final String userName, final String messageId) {

        return MgnlContext.doInSystemContext(new MgnlContext.Op<Message, RuntimeException>() {

            @Override
            public Message exec() {
                try {
                    Session session = MgnlContext.getJCRSession(WORKSPACE_NAME);

                    Node messageNode = getMessageNode(session, userName, messageId);

                    if (messageNode == null) {
                        return null;
                    }

                    return unmarshallMessage(messageNode);

                } catch (RepositoryException e) {
                    logger.error("Unable to read message: " + messageId + " for user: " + userName, e);
                    return null;
                } catch (Node2BeanException e) {
                    logger.error("Unable to read message: " + messageId + " for user: " + userName, e);
                    return null;
                }
            }
        });
    }

    public void removeMessageById(final String userName, final String messageId) {
        MgnlContext.doInSystemContext(new MgnlContext.Op<Void, RuntimeException>() {

            @Override
            public Void exec() {
                try {
                    Session session = MgnlContext.getJCRSession(WORKSPACE_NAME);

                    Node messageNode = getMessageNode(session, userName, messageId);

                    if (messageNode == null) {
                        return null;
                    }

                    messageNode.remove();

                    session.save();

                } catch (RepositoryException e) {
                    logger.error("Unable to read message: " + messageId + " for user: " + userName, e);
                }
                return null;
            }
        });

    }

    public int getNumberOfUnclearedMessagesForUserAndByType(final String userName, final MessageType type) {
        return MgnlContext.doInSystemContext(new MgnlContext.Op<Integer, RuntimeException>() {

            @Override
            public Integer exec() throws RuntimeException {
                try {
                    Session session = MgnlContext.getJCRSession(WORKSPACE_NAME);

                    int n = 0;
                    for (Node messageNode : NodeUtil.getNodes(getOrCreateUserNode(session, userName), MESSAGE_NODE_TYPE)) {
                        if (messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.MESSAGETYPE).getString().equals(type.name()) && !messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.CLEARED).getBoolean()) {
                            n++;
                        }
                    }
                    return n;

                } catch (RepositoryException e) {
                    logger.warn("Failed to find the number of uncleared messages for user: " + userName, e);
                    return 0;
                }
            }
        });
    }

    void marshallMessage(final Message message, final Node node) throws RepositoryException {
        node.setProperty(AdmincentralNodeTypes.SystemMessage.ID, message.getId());
        node.setProperty(AdmincentralNodeTypes.SystemMessage.TIMESTAMP, message.getTimestamp());
        node.setProperty(AdmincentralNodeTypes.SystemMessage.SENDER, message.getSender() != null ? message.getSender() : "");
        node.setProperty(AdmincentralNodeTypes.SystemMessage.MESSAGE, message.getMessage() != null ? message.getMessage() : "");
        node.setProperty(AdmincentralNodeTypes.SystemMessage.SUBJECT, message.getSubject() != null ? message.getSubject() : "");
        node.setProperty(AdmincentralNodeTypes.SystemMessage.MESSAGETYPE, message.getType() != null ? message.getType().name() : MessageType.UNKNOWN.name());
        node.setProperty(AdmincentralNodeTypes.SystemMessage.VIEW, message.getView());
        node.setProperty(AdmincentralNodeTypes.SystemMessage.CLEARED, message.isCleared());

        final Iterator<String> propertyNames = message.getPropertNames().iterator();
        while (propertyNames.hasNext()) {
            final String propertyName = propertyNames.next();
            PropertyUtil.setProperty(node, propertyName, message.getProperty(propertyName));
        }
    }

    Message unmarshallMessage(Node node) throws RepositoryException, Node2BeanException {
        Map<String, Object> map = Node2MapUtil.node2map(node);
        long timestamp = ((Long) map.get(AdmincentralNodeTypes.SystemMessage.TIMESTAMP)).longValue();

        final Message message = new Message(timestamp);
        message.setId(node.getName());
        message.setSender(node.getProperty(AdmincentralNodeTypes.SystemMessage.SENDER).getString());
        message.setMessage(node.getProperty(AdmincentralNodeTypes.SystemMessage.MESSAGE).getString());
        message.setSubject(node.getProperty(AdmincentralNodeTypes.SystemMessage.SUBJECT).getString());
        message.setType(MessageType.valueOf(node.getProperty(AdmincentralNodeTypes.SystemMessage.MESSAGETYPE).getString()));
        message.setCleared(node.getProperty(AdmincentralNodeTypes.SystemMessage.CLEARED).getBoolean());
        if (node.hasProperty(AdmincentralNodeTypes.SystemMessage.VIEW)) {
            message.setView(node.getProperty(AdmincentralNodeTypes.SystemMessage.VIEW).getString());
        }

        // remove all attributes that are already explicitly treated - see above.
        map.remove(AdmincentralNodeTypes.SystemMessage.TIMESTAMP);
        map.remove(AdmincentralNodeTypes.SystemMessage.SENDER);
        map.remove(AdmincentralNodeTypes.SystemMessage.SUBJECT);
        map.remove(AdmincentralNodeTypes.SystemMessage.MESSAGETYPE);
        map.remove(AdmincentralNodeTypes.SystemMessage.VIEW);
        map.remove(AdmincentralNodeTypes.SystemMessage.CLEARED);

        final Iterator<String> propertyNames = map.keySet().iterator();
        while (propertyNames.hasNext()) {
            final String propertyName = propertyNames.next();
            message.addProperty(propertyName, map.get(propertyName));
        }

        return message;
    }

    private Node getOrCreateUserNode(Session session, String userName) throws RepositoryException {
        String userNodePath = WORKSPACE_PATH + userName;
        return JcrUtils.getOrCreateByPath(userNodePath, USER_NODE_TYPE, session);
    }

    private Node getOrCreateMessageNode(Session session, String userName, Message message) throws RepositoryException {
        String messageNodePath = WORKSPACE_PATH + userName + "/" + message.getId();
        return JcrUtils.getOrCreateByPath(messageNodePath, false, USER_NODE_TYPE, MESSAGE_NODE_TYPE, session, false);
    }

    private Node getMessageNode(Session session, String userName, String messageId) throws RepositoryException {
        String messageNodePath = WORKSPACE_PATH + userName + "/" + messageId;
        return session.nodeExists(messageNodePath) ? session.getNode(messageNodePath) : null;
    }

    private String getUniqueMessageId(Node userNode) throws RepositoryException {
        int largestIdFound = -1;
        for (Node node : NodeUtil.getNodes(userNode, MESSAGE_NODE_TYPE)) {
            try {
                int nameAsInt = Integer.parseInt(node.getName());
                if (nameAsInt > largestIdFound) {
                    largestIdFound = nameAsInt;
                }
            } catch (NumberFormatException e) {
                logger.warn("Expected name of node " + userNode.getPath() + " to be numeric", e);
            }
        }
        return String.valueOf(largestIdFound + 1);
    }

}
