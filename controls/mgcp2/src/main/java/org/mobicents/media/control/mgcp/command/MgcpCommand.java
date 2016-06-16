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

import org.mobicents.media.control.mgcp.listener.MgcpCommandListener;
import org.mobicents.media.control.mgcp.message.MgcpRequest;

/**
 * Represents an MGCP action that can be executed.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public interface MgcpCommand {

    /**
     * Executes an MGCP command.
     * 
     * @param request The MGCP request to be executed
     * @param listener The listener that will receive the response of the execution. Depending on its return code, the response
     *        can be successful or not.
     */
    void execute(MgcpRequest request, MgcpCommandListener listener);

}
