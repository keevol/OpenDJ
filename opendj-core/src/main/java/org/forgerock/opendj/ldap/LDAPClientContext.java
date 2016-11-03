/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyright [year] [name of copyright owner]".
 *
 * Copyright 2010 Sun Microsystems, Inc.
 * Portions copyright 2012-2016 ForgeRock AS.
 */

package org.forgerock.opendj.ldap;

import java.net.InetSocketAddress;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.security.sasl.SaslServer;

import org.forgerock.opendj.ldap.requests.UnbindRequest;
import org.forgerock.opendj.ldap.responses.ExtendedResult;

/**
 * An LDAP client which has connected to a {@link ServerConnectionFactory}. An
 * LDAP client context can be used to query information about the client's
 * connection such as their network address, as well as managing the state of
 * the connection.
 */
public interface LDAPClientContext {

    /** Listens for disconnection event. */
    public interface DisconnectListener {
        /**
         * Invoked when the connection has been disconnected because of an error (e.g: message too big).
         *
         * @param context
         *            The {@link LDAPClientContext} which has failed
         * @param error
         *            The error
         */
        void exceptionOccurred(LDAPClientContext context, Throwable error);

        /**
         * Invoked when the client closed the connection, possibly using an unbind request.
         *
         * @param context
         *            The {@link LDAPClientContext} which has been disconnected
         * @param unbindRequest
         *            The unbind request, which may be {@code null} if one was not sent before the connection was
         *            closed.
         */
        void connectionClosed(LDAPClientContext context, UnbindRequest unbindRequest);

        /**
         * Invoked when the connection has been disconnected by the server.
         *
         * @param context
         *            The {@link LDAPClientContext} which has been disconnected
         * @param resultCode
         *            The result code which was included with the disconnect notification, or {@code null} if no
         *            disconnect notification was sent.
         * @param diagnosticMessage
         *            The diagnostic message, which may be empty or {@code null} indicating that none was provided.
         */
        void connectionDisconnected(LDAPClientContext context, ResultCode resultCode, String diagnosticMessage);
    }

    /**
     * Register a listener which will be notified when this {@link LDAPClientContext} is disconnected.
     *
     * @param listener The {@link DisconnectListener} to register.
     */
    void onDisconnect(DisconnectListener listener);

    /**
     * Disconnects the client without sending a disconnect notification. Invoking this method causes
     * {@link DisconnectListener#connectionDisconnected(LDAPClientContext, ResultCode, String)} to be called before this
     * method returns.
     */
    void disconnect();

    /**
     * Disconnects the client and sends a disconnect notification, containing the provided result code and diagnostic
     * message. Invoking this method causes
     * {@link DisconnectListener#connectionDisconnected(LDAPClientContext, ResultCode, String)} to be called before this
     * method returns.
     *
     * @param resultCode
     *            The result code to include with the disconnect notification
     * @param diagnosticMessage
     *            The diagnostic message to include with the disconnect notification
     */
    void disconnect(ResultCode resultCode, String diagnosticMessage);

    /**
     * Returns the {@code InetSocketAddress} associated with the local system.
     *
     * @return The {@code InetSocketAddress} associated with the local system.
     */
    InetSocketAddress getLocalAddress();

    /**
     * Returns the {@code InetSocketAddress} associated with the remote system.
     *
     * @return The {@code InetSocketAddress} associated with the remote system.
     */
    InetSocketAddress getPeerAddress();

    /**
     * Returns the cipher strength, in bits, currently in use by the underlying
     * connection. This value is analogous to the
     * {@code javax.servlet.request.key_size} property defined in the Servlet
     * specification (section 3.8 "SSL Attributes"). It provides no indication
     * of the relative strength of different cipher algorithms, their known
     * weaknesses, nor the strength of other cryptographic information used
     * during SSL/TLS negotiation.
     *
     * @return The cipher strength, in bits, currently in use by the underlying
     *         connection.
     */
    int getSecurityStrengthFactor();

    /**
     * Returns the SSL session currently in use by the underlying connection, or
     * {@code null} if SSL/TLS is not enabled.
     *
     * @return The SSL session currently in use by the underlying connection, or
     *         {@code null} if SSL/TLS is not enabled.
     */
    SSLSession getSSLSession();

    /**
     * Returns {@code true} if the underlying connection has been closed as a
     * result of a client disconnect, a fatal connection error, or a server-side
     * {@link #disconnect}.
     * <p>
     * This method provides a polling mechanism which can be used by synchronous
     * request handler implementations to detect connection termination.
     * <p>
     * <b>Server connections:</b> this method will always return {@code true}
     * when called from within {@link ServerConnection#handleConnectionClosed
     * handleConnectionClosed},
     * {@link ServerConnection#handleConnectionDisconnected
     * handleConnectionDisconnected}, or
     * {@link ServerConnection#handleConnectionError handleConnectionError}.
     *
     * @return {@code true} if the underlying connection has been closed.
     */
    boolean isClosed();

    /**
     * Sends an unsolicited notification to the client.
     *
     * @param notification
     *            The notification to send.
     */
    void sendUnsolicitedNotification(ExtendedResult notification);

    /**
     * Installs the TLS/SSL security layer on the underlying connection. The TLS/SSL security layer will be installed
     * beneath any existing connection security layers and can only be installed at most once.
     *
     * @param sslEngine
     *            The {@code SSLEngine} which should be used to secure the connection.
     * @param startTls
     *            Must be {@code true} if the TLS filter has to be installed as a consequence of a StartTLS request
     *            performed by a client. When {@code true} the TLS filter will be installed atomically after the first
     *            message sent to prevent race-condition.
     * @return {@code true} if the TLS filter has been enabled, {@code false} if it was already enabled.
     * @throws NullPointerException
     *             if sslEngine is null
     */
    boolean enableTLS(SSLEngine sslEngine, boolean startTls);

    /**
     * Installs the SASL security layer on the underlying connection.
     *
     * @param saslServer
     *            The {@code SaslServer} which should be used to secure the conneciton.
     */
    void enableSASL(SaslServer saslServer);
}
