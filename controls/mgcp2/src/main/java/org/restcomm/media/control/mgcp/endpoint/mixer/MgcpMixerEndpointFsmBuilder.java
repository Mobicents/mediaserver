/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2017, Telestax Inc and individual contributors
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

package org.restcomm.media.control.mgcp.endpoint.mixer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.restcomm.media.control.mgcp.endpoint.AbstractMgcpEndpointFsmBuilder;
import org.restcomm.media.control.mgcp.endpoint.MgcpEndpointAction;

/**
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class MgcpMixerEndpointFsmBuilder extends AbstractMgcpEndpointFsmBuilder {

    private static final List<MgcpEndpointAction> activationActions = Arrays.asList(ActivateMixerAction.INSTANCE, RegisterMediaGroupInMixerAction.INSTANCE);
    private static final List<MgcpEndpointAction> deactivationActions = Arrays.asList(UnregisterMediaGroupFromMixerAction.INSTANCE, DeactivateMixerAction.INSTANCE);
    private static final List<MgcpEndpointAction> registeredConnectionActions = Arrays.asList(RegisterConnectionInMixerAction.INSTANCE);
    private static final List<MgcpEndpointAction> unregisteredConnectionActions = Arrays.asList(UnregisterConnectionsFromMixerAction.INSTANCE);

    public static final MgcpMixerEndpointFsmBuilder INSTANCE = new MgcpMixerEndpointFsmBuilder();

    MgcpMixerEndpointFsmBuilder() {
        super();
    }

    @Override
    protected List<MgcpEndpointAction> getActivationActions() {
        return activationActions;
    }

    @Override
    protected List<MgcpEndpointAction> getDeactivationActions() {
        return deactivationActions;
    }

    @Override
    protected List<MgcpEndpointAction> getRegisteredConnectionActions() {
        return registeredConnectionActions;
    }

    @Override
    protected List<MgcpEndpointAction> getUnregisteredConnectionActions() {
        return unregisteredConnectionActions;
    }

}
