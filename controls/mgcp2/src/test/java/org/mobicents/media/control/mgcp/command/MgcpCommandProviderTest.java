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

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Test;
import org.mobicents.media.control.mgcp.command.AuditConnectionCommand;
import org.mobicents.media.control.mgcp.command.AuditEndpointCommand;
import org.mobicents.media.control.mgcp.command.CreateConnectionCommand;
import org.mobicents.media.control.mgcp.command.DeleteConnectionCommand;
import org.mobicents.media.control.mgcp.command.MgcpCommand;
import org.mobicents.media.control.mgcp.command.MgcpCommandProvider;
import org.mobicents.media.control.mgcp.command.ModifyConnectionCommand;
import org.mobicents.media.control.mgcp.command.NotifyCommand;
import org.mobicents.media.control.mgcp.command.RequestNotificationCommand;
import org.mobicents.media.control.mgcp.connection.MgcpConnectionProvider;
import org.mobicents.media.control.mgcp.endpoint.MgcpEndpointManager;
import org.mobicents.media.control.mgcp.message.MgcpRequestType;

/**
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class MgcpCommandProviderTest {

    @Test
    public void testProvideCrcx() {
        // given
        MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        MgcpCommandProvider commandProvider = new MgcpCommandProvider(endpointManager, connectionProvider);

        // when
        MgcpCommand command = commandProvider.provide(MgcpRequestType.CRCX);

        // then
        Assert.assertTrue(command instanceof CreateConnectionCommand);
    }

    @Test
    public void testProvideMdcx() {
        // given
        MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        MgcpCommandProvider commandProvider = new MgcpCommandProvider(endpointManager, connectionProvider);

        // when
        MgcpCommand command = commandProvider.provide(MgcpRequestType.MDCX);

        // then
        Assert.assertTrue(command instanceof ModifyConnectionCommand);
    }

    @Test
    public void testProvideDlcx() {
        // given
        MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        MgcpCommandProvider commandProvider = new MgcpCommandProvider(endpointManager, connectionProvider);

        // when
        MgcpCommand command = commandProvider.provide(MgcpRequestType.DLCX);

        // then
        Assert.assertTrue(command instanceof DeleteConnectionCommand);
    }

    @Test
    public void testProvideAucx() {
        // given
        MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        MgcpCommandProvider commandProvider = new MgcpCommandProvider(endpointManager, connectionProvider);

        // when
        MgcpCommand command = commandProvider.provide(MgcpRequestType.AUCX);

        // then
        Assert.assertTrue(command instanceof AuditConnectionCommand);
    }

    @Test
    public void testProvideAuep() {
        // given
        MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        MgcpCommandProvider commandProvider = new MgcpCommandProvider(endpointManager, connectionProvider);

        // when
        MgcpCommand command = commandProvider.provide(MgcpRequestType.AUEP);

        // then
        Assert.assertTrue(command instanceof AuditEndpointCommand);
    }

    @Test
    public void testProvideRqnt() {
        // given
        MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        MgcpCommandProvider commandProvider = new MgcpCommandProvider(endpointManager, connectionProvider);

        // when
        MgcpCommand command = commandProvider.provide(MgcpRequestType.RQNT);

        // then
        Assert.assertTrue(command instanceof RequestNotificationCommand);
    }

    @Test
    public void testProvideNtfy() {
        // given
        MgcpEndpointManager endpointManager = mock(MgcpEndpointManager.class);
        MgcpConnectionProvider connectionProvider = mock(MgcpConnectionProvider.class);
        MgcpCommandProvider commandProvider = new MgcpCommandProvider(endpointManager, connectionProvider);

        // when
        MgcpCommand command = commandProvider.provide(MgcpRequestType.NTFY);

        // then
        Assert.assertTrue(command instanceof NotifyCommand);
    }

}
