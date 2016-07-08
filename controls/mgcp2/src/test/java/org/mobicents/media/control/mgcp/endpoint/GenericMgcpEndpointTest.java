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

package org.mobicents.media.control.mgcp.endpoint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mobicents.media.control.mgcp.pkg.AbstractMgcpSignal;
import org.mobicents.media.control.mgcp.pkg.MgcpSignal;
import org.mobicents.media.control.mgcp.pkg.SignalType;

/**
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class GenericMgcpEndpointTest {

    @Test
    public void testExecuteSignal() {
        // given
        final MgcpSignal signal = mock(MgcpSignal.class);
        final MgcpEndpoint genericMgcpEndpoint = new GenericMgcpEndpoint("mobicents/endpoint/1");

        // when
        genericMgcpEndpoint.execute(signal);

        // then
        verify(signal, times(1)).execute();
    }

    @Test
    public void testExecuteSignalDuringSignalExecution() {
        // given
        final MockSignal signal1 = new MockSignal("AU", "pa", SignalType.TIME_OUT);
        final MockSignal signal2 = new MockSignal("AU", "pc", SignalType.TIME_OUT);
        final MgcpEndpoint genericMgcpEndpoint = new GenericMgcpEndpoint("mobicents/endpoint/1");

        // when
        genericMgcpEndpoint.execute(signal1);
        genericMgcpEndpoint.execute(signal2);

        // then
        assertTrue(signal1.calledExecute);
        assertTrue(signal1.calledCancel);
        assertTrue(signal2.calledExecute);
        assertFalse(signal2.calledCancel);
    }

    @Test
    public void testExecuteDuplicateSignal() {
        // given
        final MockSignal signal1 = new MockSignal("AU", "pa", SignalType.TIME_OUT);
        final MockSignal signal2 = new MockSignal("AU", "pa", SignalType.TIME_OUT);
        final MgcpEndpoint genericMgcpEndpoint = new GenericMgcpEndpoint("mobicents/endpoint/1");
        
        // when
        genericMgcpEndpoint.execute(signal1);
        genericMgcpEndpoint.execute(signal2);
        
        // then
        assertTrue(signal1.calledExecute);
        assertFalse(signal1.calledCancel);
        assertFalse(signal2.calledExecute);
    }

    /**
     * Needed to create a mock class because Mockito overrides equals() so we cannot use mocks for MgcpSignal.
     * 
     * @author Henrique Rosa (henrique.rosa@telestax.com)
     *
     */
    private final class MockSignal extends AbstractMgcpSignal {

        boolean calledExecute = false;
        boolean calledCancel = false;

        public MockSignal(String packageName, String symbol, SignalType type) {
            super(packageName, symbol, type);
        }

        @Override
        public void execute() {
            this.calledExecute = true;
        }

        @Override
        public void cancel() {
            this.calledCancel = true;
        }

        @Override
        protected boolean isParameterSupported(String name) {
            return true;
        }

    }

}
