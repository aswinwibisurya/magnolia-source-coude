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
package info.magnolia.ui.framework.message;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;

import java.util.ArrayList;
import java.util.List;

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

    public static final String TIMESTAMP = "timestamp";
    public static final String SUBJECT = "subject";
    public static final String TYPE = "type";
    public static final String MESSAGE = "message";
    public static final String CLEARED = "cleared";

    static final String MESSAGE_NODE_TYPE = "mgnl:systemMessage";

    private static final String WORKSPACE_NAME = "messages";
    private static final String WORKSPACE_PATH = "/";
    private static final String USER_NODE_TYPE = NodeTypes.Content.NAME;

    /**
     * Stores a new message or overwrites an existing one depending on whether there's an id set. That is, the id of the
     * message is respected if present otherwise a new unique one is used. When the method returns the message has been
     * updated with a new id.
     * 
     * @param userName
     *            user to save the message for
     * @param message
     *            message to save
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
                        if (!messageNode.getProperty(CLEARED).getBoolean()) {
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
                    logger.error("Saving message failed for user: " + userName, e);
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
                }
            }
        });
    }

    void marshallMessage(Message message, Node node) throws RepositoryException {
        node.setProperty(TIMESTAMP, message.getTimestamp());
        node.setProperty(TYPE, message.getType().name());
        node.setProperty(SUBJECT, message.getSubject());
        node.setProperty(MESSAGE, message.getMessage());
        node.setProperty(CLEARED, message.isCleared());
    }

    Message unmarshallMessage(Node node) throws RepositoryException {
        final Message message = new Message(node.getProperty(TIMESTAMP).getLong());
        message.setId(node.getName());
        if (node.hasProperty(TYPE)) {
            message.setType(MessageType.valueOf(node.getProperty(TYPE).getString()));
        }
        if (node.hasProperty(SUBJECT)) {
            message.setSubject(node.getProperty(SUBJECT).getString());
        }
        if (node.hasProperty(MESSAGE)) {
            message.setMessage(node.getProperty(MESSAGE).getString());
        }
        if (node.hasProperty(CLEARED)) {
            message.setCleared(node.getProperty(CLEARED).getBoolean());
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
