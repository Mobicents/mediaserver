/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.media.control.mgcp.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mobicents.media.control.mgcp.connection.MgcpConnection;
import org.mobicents.media.control.mgcp.connection.MgcpConnectionProvider;
import org.mobicents.media.control.mgcp.connection.MgcpLocalConnection;
import org.mobicents.media.control.mgcp.connection.MgcpRemoteConnection;
import org.mobicents.media.control.mgcp.endpoint.MgcpEndpoint;
import org.mobicents.media.control.mgcp.endpoint.MgcpEndpointManager;
import org.mobicents.media.control.mgcp.exception.MgcpCallNotFoundException;
import org.mobicents.media.control.mgcp.exception.MgcpConnectionNotFound;
import org.mobicents.media.control.mgcp.exception.MgcpException;
import org.mobicents.media.control.mgcp.message.MessageDirection;
import org.mobicents.media.control.mgcp.message.MgcpMessageParser;
import org.mobicents.media.control.mgcp.message.MgcpMessageSubject;
import org.mobicents.media.control.mgcp.message.MgcpParameterType;
import org.mobicents.media.control.mgcp.message.MgcpRequest;
import org.mobicents.media.control.mgcp.message.MgcpResponse;
import org.mobicents.media.control.mgcp.message.MgcpResponseCode;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class DeleteConnectionCommandTest {

    @Test
    public void testDeleteSingleConnection() throws MgcpException {
        // given
        final StringBuilder builder = new StringBuilder();
        builder.append("DLCX 147483653 mobicents/bridge/1@127.0.0.1:2427 MGCP 1.0").append(System.lineSeparator());
        builder.append("C:1").append(System.lineSeparator());
        builder.append("I:1").append(System.lineSeparator());

        final MgcpMessageParser parser = new MgcpMessageParser();
        final MgcpRequest request = parser.parseRequest(builder.toString());
        final MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        final MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        final MgcpEndpoint bridgeEndpoint = mock(MgcpEndpoint.class);
        final MgcpRemoteConnection connection = mock(MgcpRemoteConnection.class);
        final MgcpMessageSubject listener = mock(MgcpMessageSubject.class);
        final DeleteConnectionCommand dlcx = new DeleteConnectionCommand(endpointManager, connectionProvider);

        // when
        when(endpointManager.getEndpoint("mobicents/bridge/1")).thenReturn(bridgeEndpoint);
        when(bridgeEndpoint.deleteConnection(1, 1)).thenReturn(connection);
        when(connection.getIdentifier()).thenReturn(1);
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                MgcpResponse response = invocation.getArgumentAt(1, MgcpResponse.class);
                assertNotNull(response);
                assertEquals(MgcpResponseCode.TRANSACTION_WAS_EXECUTED.code(), response.getCode());
                assertNotNull(response.getParameter(MgcpParameterType.CONNECTION_PARAMETERS));
                return null;
            }

        }).when(listener).notify(eq(dlcx), any(MgcpResponse.class), eq(MessageDirection.OUTGOING));
        dlcx.execute(request, listener);

        // then
        verify(bridgeEndpoint, times(1)).deleteConnection(1, 1);
    }

    @Test
    public void testDeleteMultipleConnections() throws MgcpException {
        // given
        final StringBuilder builder = new StringBuilder();
        builder.append("DLCX 147483653 mobicents/bridge/1@127.0.0.1:2427 MGCP 1.0").append(System.lineSeparator());
        builder.append("C:1").append(System.lineSeparator());

        final MgcpMessageParser parser = new MgcpMessageParser();
        final MgcpRequest request = parser.parseRequest(builder.toString());
        final MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        final MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        final MgcpRemoteConnection connection1 = mock(MgcpRemoteConnection.class);
        final MgcpLocalConnection connection2 = mock(MgcpLocalConnection.class);
        final List<MgcpConnection> connections = new ArrayList<>();
        final MgcpEndpoint bridgeEndpoint = mock(MgcpEndpoint.class);
        final MgcpMessageSubject listener = mock(MgcpMessageSubject.class);
        final DeleteConnectionCommand dlcx = new DeleteConnectionCommand(endpointManager, connectionProvider);

        // when
        when(endpointManager.getEndpoint("mobicents/bridge/1")).thenReturn(bridgeEndpoint);
        when(bridgeEndpoint.deleteConnections(1)).thenReturn(connections);
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                MgcpResponse response = invocation.getArgumentAt(1, MgcpResponse.class);
                assertNotNull(response);
                assertEquals(MgcpResponseCode.TRANSACTION_WAS_EXECUTED.code(), response.getCode());
                assertNull(response.getParameter(MgcpParameterType.CONNECTION_PARAMETERS));
                return null;
            }

        }).when(listener).notify(eq(dlcx), any(MgcpResponse.class), eq(MessageDirection.OUTGOING));

        connections.add(connection1);
        connections.add(connection2);
        dlcx.execute(request, listener);

        // then
        verify(bridgeEndpoint, times(1)).deleteConnections(1);
    }

    @Test
    public void testNoConnectionFoundWhenDeletingSingleConnection() throws MgcpException {
        // given
        final StringBuilder builder = new StringBuilder();
        builder.append("DLCX 147483653 mobicents/bridge/1@127.0.0.1:2427 MGCP 1.0").append(System.lineSeparator());
        builder.append("C:1").append(System.lineSeparator());
        builder.append("I:1").append(System.lineSeparator());

        final MgcpMessageParser parser = new MgcpMessageParser();
        final MgcpRequest request = parser.parseRequest(builder.toString());
        final MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        final MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        final MgcpEndpoint bridgeEndpoint = mock(MgcpEndpoint.class);
        final MgcpMessageSubject listener = mock(MgcpMessageSubject.class);
        final DeleteConnectionCommand dlcx = new DeleteConnectionCommand(endpointManager, connectionProvider);

        // when
        when(endpointManager.getEndpoint("mobicents/bridge/1")).thenReturn(bridgeEndpoint);
        when(bridgeEndpoint.deleteConnection(1, 1)).thenThrow(new MgcpConnectionNotFound(""));
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // then
                MgcpResponse response = invocation.getArgumentAt(1, MgcpResponse.class);
                assertNotNull(response);
                assertEquals(MgcpResponseCode.INCORRECT_CONNECTION_ID.code(), response.getCode());
                return null;
            }

        }).when(listener).notify(eq(dlcx), any(MgcpResponse.class), eq(MessageDirection.OUTGOING));
        dlcx.execute(request, listener);

        // then
        verify(bridgeEndpoint, times(1)).deleteConnection(1, 1);
    }

    @Test
    public void testNoCallFoundWhenDeletingSingleConnection() throws MgcpException {
        // given
        final StringBuilder builder = new StringBuilder();
        builder.append("DLCX 147483653 mobicents/bridge/1@127.0.0.1:2427 MGCP 1.0").append(System.lineSeparator());
        builder.append("C:1").append(System.lineSeparator());
        builder.append("I:1").append(System.lineSeparator());

        final MgcpMessageParser parser = new MgcpMessageParser();
        final MgcpRequest request = parser.parseRequest(builder.toString());
        final MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        final MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        final MgcpEndpoint bridgeEndpoint = mock(MgcpEndpoint.class);
        final MgcpMessageSubject listener = mock(MgcpMessageSubject.class);
        final DeleteConnectionCommand dlcx = new DeleteConnectionCommand(endpointManager, connectionProvider);

        // when
        when(endpointManager.getEndpoint("mobicents/bridge/1")).thenReturn(bridgeEndpoint);
        when(bridgeEndpoint.deleteConnection(1, 1)).thenThrow(new MgcpCallNotFoundException(""));
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // then
                MgcpResponse response = invocation.getArgumentAt(1, MgcpResponse.class);
                assertNotNull(response);
                assertEquals(MgcpResponseCode.INCORRECT_CALL_ID.code(), response.getCode());
                return null;
            }

        }).when(listener).notify(eq(dlcx), any(MgcpResponse.class), eq(MessageDirection.OUTGOING));
        dlcx.execute(request, listener);

        // then
        verify(bridgeEndpoint, times(1)).deleteConnection(1, 1);
    }

    @Test
    public void testNoCallFoundWhenDeletingMultipleConnections() throws MgcpException {
        // given
        final StringBuilder builder = new StringBuilder();
        builder.append("DLCX 147483653 mobicents/bridge/1@127.0.0.1:2427 MGCP 1.0").append(System.lineSeparator());
        builder.append("C:1").append(System.lineSeparator());

        final MgcpMessageParser parser = new MgcpMessageParser();
        final MgcpRequest request = parser.parseRequest(builder.toString());
        final MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        final MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        final MgcpEndpoint bridgeEndpoint = mock(MgcpEndpoint.class);
        final MgcpMessageSubject listener = mock(MgcpMessageSubject.class);
        final DeleteConnectionCommand dlcx = new DeleteConnectionCommand(endpointManager, connectionProvider);

        // when
        when(endpointManager.getEndpoint("mobicents/bridge/1")).thenReturn(bridgeEndpoint);
        when(bridgeEndpoint.deleteConnections(1)).thenThrow(new MgcpCallNotFoundException(""));
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // then
                MgcpResponse response = invocation.getArgumentAt(1, MgcpResponse.class);
                assertNotNull(response);
                /*
                 * Note that the command will still succeed if there were no connections with the CallId specified, as long as
                 * the EndpointId was valid.
                 */
                assertEquals(MgcpResponseCode.TRANSACTION_WAS_EXECUTED.code(), response.getCode());
                assertNull(response.getParameter(MgcpParameterType.CONNECTION_PARAMETERS));
                return null;
            }

        }).when(listener).notify(eq(dlcx), any(MgcpResponse.class), eq(MessageDirection.OUTGOING));
        dlcx.execute(request, listener);

        // then
        verify(bridgeEndpoint, times(1)).deleteConnections(1);
    }
    
    @Test
    public void testValidateRequestWithEndpointNameContainingWildCardAllWithEndpointIdSpecified() throws MgcpException {
        // given
        final StringBuilder builder = new StringBuilder();
        builder.append("DLCX 147483653 mobicents/bridge/*@127.0.0.1:2427 MGCP 1.0").append(System.lineSeparator());
        builder.append("C:1").append(System.lineSeparator());
        builder.append("I:1").append(System.lineSeparator());
        builder.append(System.lineSeparator());

        final MgcpMessageParser parser = new MgcpMessageParser();
        final MgcpRequest request = parser.parseRequest(builder.toString());
        final MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        final MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        final MgcpMessageSubject listener = mock(MgcpMessageSubject.class);
        final DeleteConnectionCommand dlcx = new DeleteConnectionCommand(endpointManager, connectionProvider);

        // when
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // then
                MgcpResponse response = invocation.getArgumentAt(1, MgcpResponse.class);
                assertNotNull(response);
                assertEquals(MgcpResponseCode.WILDCARD_TOO_COMPLICATED.code(), response.getCode());
                return null;
            }

        }).when(listener).notify(eq(dlcx), any(MgcpResponse.class), eq(MessageDirection.OUTGOING));
        dlcx.execute(request, listener);
    }

    @Test
    public void testValidateRequestWithEndpointNameContainingWildCardAnyWithEndpointIdSpecified() throws MgcpException {
        // given
        final StringBuilder builder = new StringBuilder();
        builder.append("DLCX 147483653 mobicents/bridge/$@127.0.0.1:2427 MGCP 1.0").append(System.lineSeparator());
        builder.append("C:1").append(System.lineSeparator());
        builder.append("I:1").append(System.lineSeparator());
        builder.append(System.lineSeparator());
        
        final MgcpMessageParser parser = new MgcpMessageParser();
        final MgcpRequest request = parser.parseRequest(builder.toString());
        final MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        final MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        final MgcpMessageSubject listener = mock(MgcpMessageSubject.class);
        final DeleteConnectionCommand dlcx = new DeleteConnectionCommand(endpointManager, connectionProvider);
        
        // when
        doAnswer(new Answer<Object>() {
            
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // then
                MgcpResponse response = invocation.getArgumentAt(1, MgcpResponse.class);
                assertNotNull(response);
                assertEquals(MgcpResponseCode.WILDCARD_TOO_COMPLICATED.code(), response.getCode());
                return null;
            }
            
        }).when(listener).notify(eq(dlcx), any(MgcpResponse.class), eq(MessageDirection.OUTGOING));
        dlcx.execute(request, listener);
    }

    @Test
    public void testValidateRequestWithEndpointNameContainingWildCardAnyWithoutEndpointIdSpecified() throws MgcpException {
        // given
        final StringBuilder builder = new StringBuilder();
        builder.append("DLCX 147483653 mobicents/bridge/$@127.0.0.1:2427 MGCP 1.0").append(System.lineSeparator());
        builder.append("C:1").append(System.lineSeparator());
        builder.append(System.lineSeparator());
        
        final MgcpMessageParser parser = new MgcpMessageParser();
        final MgcpRequest request = parser.parseRequest(builder.toString());
        final MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        final MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        final MgcpMessageSubject listener = mock(MgcpMessageSubject.class);
        final DeleteConnectionCommand dlcx = new DeleteConnectionCommand(endpointManager, connectionProvider);
        
        // when
        doAnswer(new Answer<Object>() {
            
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // then
                MgcpResponse response = invocation.getArgumentAt(1, MgcpResponse.class);
                assertNotNull(response);
                assertEquals(MgcpResponseCode.WILDCARD_TOO_COMPLICATED.code(), response.getCode());
                return null;
            }
            
        }).when(listener).notify(eq(dlcx), any(MgcpResponse.class), eq(MessageDirection.OUTGOING));
        dlcx.execute(request, listener);
    }

    @Test
    public void testValidateRequestWithoutCallId() throws MgcpException {
        // given
        final StringBuilder builder = new StringBuilder();
        builder.append("DLCX 147483653 mobicents/bridge/$@127.0.0.1:2427 MGCP 1.0").append(System.lineSeparator());
        
        final MgcpMessageParser parser = new MgcpMessageParser();
        final MgcpRequest request = parser.parseRequest(builder.toString());
        final MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        final MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        final MgcpMessageSubject listener = mock(MgcpMessageSubject.class);
        final DeleteConnectionCommand dlcx = new DeleteConnectionCommand(endpointManager, connectionProvider);
        
        // when
        doAnswer(new Answer<Object>() {
            
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // then
                MgcpResponse response = invocation.getArgumentAt(1, MgcpResponse.class);
                assertNotNull(response);
                assertEquals(MgcpResponseCode.INCORRECT_CALL_ID.code(), response.getCode());
                return null;
            }
            
        }).when(listener).notify(eq(dlcx), any(MgcpResponse.class), eq(MessageDirection.OUTGOING));
        dlcx.execute(request, listener);
    }

    @Test
    public void testUnknownEndpoint() throws MgcpException {
        // given
        final StringBuilder builder = new StringBuilder();
        builder.append("DLCX 147483653 mobicents/bridge/1@127.0.0.1:2427 MGCP 1.0").append(System.lineSeparator());
        builder.append("C:1").append(System.lineSeparator());
        builder.append(System.lineSeparator());
        
        final MgcpMessageParser parser = new MgcpMessageParser();
        final MgcpRequest request = parser.parseRequest(builder.toString());
        final MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        final MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        final MgcpMessageSubject listener = mock(MgcpMessageSubject.class);
        final DeleteConnectionCommand dlcx = new DeleteConnectionCommand(endpointManager, connectionProvider);
        
        // when
        when(endpointManager.getEndpoint("mobicents/bridge/1")).thenReturn(null);
        doAnswer(new Answer<Object>() {
            
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // then
                MgcpResponse response = invocation.getArgumentAt(1, MgcpResponse.class);
                assertNotNull(response);
                assertEquals(MgcpResponseCode.ENDPOINT_UNKNOWN.code(), response.getCode());
                return null;
            }
            
        }).when(listener).notify(eq(dlcx), any(MgcpResponse.class), eq(MessageDirection.OUTGOING));
        dlcx.execute(request, listener);
    }

    @Test
    public void testUnexpectedExceptionWhileExecutingCommand() throws MgcpException {
        // given
        final StringBuilder builder = new StringBuilder();
        builder.append("DLCX 147483653 mobicents/bridge/1@127.0.0.1:2427 MGCP 1.0").append(System.lineSeparator());
        builder.append("C:1").append(System.lineSeparator());
        builder.append(System.lineSeparator());
        
        final MgcpMessageParser parser = new MgcpMessageParser();
        final MgcpRequest request = parser.parseRequest(builder.toString());
        final MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        final MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        final MgcpMessageSubject listener = mock(MgcpMessageSubject.class);
        final DeleteConnectionCommand dlcx = new DeleteConnectionCommand(endpointManager, connectionProvider);
        
        // when
        when(endpointManager.getEndpoint("mobicents/bridge/1")).thenThrow(new RuntimeException("Test Purposes!"));
        doAnswer(new Answer<Object>() {
            
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                // then
                MgcpResponse response = invocation.getArgumentAt(1, MgcpResponse.class);
                assertNotNull(response);
                assertEquals(MgcpResponseCode.PROTOCOL_ERROR.code(), response.getCode());
                return null;
            }
            
        }).when(listener).notify(eq(dlcx), any(MgcpResponse.class), eq(MessageDirection.OUTGOING));
        dlcx.execute(request, listener);
    }
}
