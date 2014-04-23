package org.mobicents.media.server.mgcp.tx.cmd;

import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.mobicents.media.server.mgcp.MgcpEvent;
import org.mobicents.media.server.mgcp.controller.MgcpConnection;
import org.mobicents.media.server.mgcp.controller.MgcpEndpoint;
import org.mobicents.media.server.mgcp.controller.NotifiedEntity;
import org.mobicents.media.server.mgcp.controller.naming.UnknownEndpointException;
import org.mobicents.media.server.mgcp.message.MgcpRequest;
import org.mobicents.media.server.mgcp.message.MgcpResponse;
import org.mobicents.media.server.mgcp.message.MgcpResponseCode;
import org.mobicents.media.server.mgcp.message.Parameter;
import org.mobicents.media.server.mgcp.tx.Action;
import org.mobicents.media.server.scheduler.Scheduler;
import org.mobicents.media.server.scheduler.Task;
import org.mobicents.media.server.scheduler.TaskChain;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.utils.Text;

/**
 * The AuditConnection command can be used by the Call Agent to retrieve the
 * parameters attached to a connection.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 * @see <a href="http://tools.ietf.org/html/rfc3435#section-2.3.11">RFC3435</a>
 */
public class AuditConnectionCmd extends Action {
	private final static Logger logger = Logger.getLogger(AuditConnectionCmd.class);
	
	// Response messages
    private final static Text ENDPOINT_ID_MISSING = new Text("Missing endpoint identifier");
    private final static Text ENDPOINT_ID_WILDCARD = new Text("Endpoint identifier cannot not use wildcard conventions");
    private final static Text ENDPOINT_INEXISTENT = new Text("Endpoint not available");
    private final static Text CONNECTION_ID_EXPECTED = new Text("Connection identifier was not specified");
    private final static Text CONNECTION_INEXISTENT = new Text("Connection not available");
    private final static Text SUCCESS= new Text("Success");

	// Media Server internals
	private final Scheduler scheduler;
	private TaskChain handler;
	
	// Endpoint identifier
	private Text localName = new Text();
	private Text domainName = new Text();
	private Text[] endpointName = new Text[] { localName, domainName };
	private MgcpEndpoint[] endpoints = new MgcpEndpoint[1];
	private MgcpEndpoint endpoint;

	// MGCP request and parameters
	private MgcpRequest request;
	/**
	 * The <b>ConnectionId</b> parameter is the identifier of the audited
	 * connection, within the context of the specified endpoint.
	 */
	private Parameter connectionId;
	/**
	 * The <b>EndpointId</b> parameter specifies the endpoint that handles the
	 * connection. The wildcard conventions SHALL NOT be used.
	 */
	private Parameter endpointId;
	/**
	 * The (possibly empty) <b>RequestedInfo</b> describes the information that
	 * is requested for the ConnectionId within the EndpointId specified. The
	 * following connection info can be audited with this command:<br>
	 * 
	 * CallId, NotifiedEntity, LocalConnectionOptions, Mode,
	 * RemoteConnectionDescriptor, LocalConnectionDescriptor,
	 * ConnectionParameters
	 */
	private Parameter requestedInfo;
	
	// Audited info
	private boolean queryCallId = false;
	private boolean queryNotifiedEntity = false;
	private boolean queryLocalConnectionOpts = false;
	private boolean queryMode = false;
	private boolean queryRemoteConnectionDes = false;
	private boolean queryLocalConnectionDes = false;
	private boolean queryConnectionParams = false;
	private boolean queryConnectionAvailability = false;
	
	private int callId;
	private NotifiedEntity notifiedEntity;
	private MgcpConnection connection;
	private ConnectionMode connectionMode;
	private Text localConnectionOpts;
	private Text localConnectionDes;
	private Text remoteConnectionDes;
	private ConnectionParameters connectionParameters;
	private boolean connectionAvailable;
	
	public AuditConnectionCmd(final Scheduler scheduler) {
		this.scheduler = scheduler;
		this.handler = new TaskChain(2, this.scheduler);
		this.handler.add(new Audit());
		this.handler.add(new Respond());
		this.setActionHandler(this.handler);
		this.setRollbackHandler(new Rollback());
	}
	
	private class Audit extends Task {

		@Override
		public int getQueueNumber() {
			return Scheduler.MANAGEMENT_QUEUE;
		}

		@Override
		public long perform() {
			// Get parameters from the MGCP request
			request = (MgcpRequest) getEvent().getMessage();
			endpointId = request.getParameter(Parameter.ENDPOINT_ID);
			connectionId = request.getParameter(Parameter.CONNECTION_ID);
			requestedInfo = request.getParameter(Parameter.REQUESTED_INFO);
			
			// Validate the parameters
			if(endpointId == null) {
				throw new MgcpCommandException(MgcpResponseCode.PROTOCOL_ERROR, ENDPOINT_ID_MISSING);
			} else {
				//getting endpoint name via existing method
	            request.getEndpoint().divide('@', endpointName);
	            // TODO endpoint id SHALL NOT use wildcard conventions
			}
			
			if (connectionId == null) {
                throw new MgcpCommandException(MgcpResponseCode.PROTOCOL_ERROR, CONNECTION_ID_EXPECTED);
            }

			// Search for the MGCP endpoint
			findMgcpEndpoints(localName, endpoints);
			endpoint = endpoints[0];
			
			// Search for the connection on the endpoint
			connection = endpoint.getConnection(connectionId.getValue().hexToInteger());
			if (connection == null) {
                throw new MgcpCommandException(MgcpResponseCode.INCORRECT_CONNECTION_ID, CONNECTION_INEXISTENT);
            }
			
			// Retrieve requested information from the connection
			if(requestedInfo != null) {
				Collection<Text> requestedParams = requestedInfo.getValue().split(',');
				auditRequestedInfo(requestedParams, connection);
			}
			
			return 0;
		}
		
		private int findMgcpEndpoints(final Text localName, final MgcpEndpoint[] endpoints) {
			try {
				int n = transaction().find(localName, endpoints);
				if (n == 0) {
					throw new MgcpCommandException(MgcpResponseCode.ENDPOINT_NOT_AVAILABLE, ENDPOINT_INEXISTENT);
				}
				return n;
			} catch (UnknownEndpointException e) {
				throw new MgcpCommandException(MgcpResponseCode.ENDPOINT_UNKNOWN, ENDPOINT_INEXISTENT);
			}
		}
		
		private void auditRequestedInfo(Collection<Text> requestedParams, MgcpConnection connection) {
			for (Text param : requestedParams) {
				if (param.equals(Parameter.CALL_ID)) {
					queryCallId = true;
					callId = connection.getCallId();
				} else if (param.equals(Parameter.NOTIFIED_ENTITY)) {
					queryNotifiedEntity = true;
					notifiedEntity = endpoint.getRequest().getCallAgent();
				} else if (param.equals(Parameter.LOCAL_CONNECTION_OPTIONS)) {
					queryLocalConnectionOpts = true;
					// TODO hrosa - get local connection options
					localConnectionOpts = new Text();
				} else if (param.equals(Parameter.MODE)) {
					queryMode = true;
					connectionMode = connection.getConnection().getMode();
				} else if (param.equals(Parameter.REMOTE_CONNECTION_DESCRIPTION)) {
					queryRemoteConnectionDes = true;
					String remoteSdp = connection.getConnection().getRemoteDescriptor();
					remoteConnectionDes = remoteSdp == null ? new Text() : new Text(remoteSdp);
				} else if (param.equals(Parameter.LOCAL_CONNECTION_DESCRIPTION)) {
					queryLocalConnectionDes = true;
					String localSdp = connection.getConnection().getLocalDescriptor();
					localConnectionDes = localSdp == null ? new Text() : new Text(localSdp);
				} else if (param.equals(Parameter.CONNECTION_PARAMETERS)) {
					queryConnectionParams = true;
					auditConnectionParameters(connection);
				} else if (param.equals(Parameter.CONNECTION_AVAILABILITY)) {
					queryConnectionAvailability = true;
					connectionAvailable = connection.getConnection().isAvailable();
				}
			}
		}
		
		private void auditConnectionParameters(MgcpConnection connection) {
			connectionParameters = new ConnectionParameters();
			connectionParameters.packetsSent = connection.getPacketsTransmitted();
			connectionParameters.packetsReceived = connection.getPacketsReceived();
			connectionParameters.jitter = (int) connection.getConnection().getJitter();
			// TODO hrosa - get transmitted octets from MGCP connection
			// TODO hrosa - get received octets from MGCP connection
			// TODO hrosa - get latency from MGCP connection
			// TODO hrosa - get lost packets from MGCP connection
		}
	}
	
	private class Respond extends Task {

		@Override
		public int getQueueNumber() {
			return Scheduler.MANAGEMENT_QUEUE;
		}

		@Override
		public long perform() {
            MgcpEvent evt = transaction().getProvider().createEvent(MgcpEvent.RESPONSE, getEvent().getAddress());
            MgcpResponse response = (MgcpResponse) evt.getMessage();
            response.setResponseCode(MgcpResponseCode.TRANSACTION_WAS_EXECUTED);
            response.setResponseString(SUCCESS);
            response.setTxID(transaction().getId());

			/*
			 * If no info was requested and the EndpointId is valid, the gateway
			 * simply checks that the connection exists, and if so returns a
			 * positive acknowledgement.
			 */
            response.setParameter(Parameter.CONNECTION_ID, connectionId.getValue());

			/*
			 * The AuditConnection response will in turn include information
			 * about each of the items auditing info was requested for
			 */
			if(requestedInfo != null) {
				if(queryCallId) {
					response.setParameter(Parameter.CALL_ID, new Text(callId));
				}
				if(queryNotifiedEntity) {
					Text entity = notifiedEntity == null ? new Text() : notifiedEntity.getValue();
					response.setParameter(Parameter.NOTIFIED_ENTITY, entity);
				}
				if(queryLocalConnectionOpts) {
					// TODO Add local connection options to response
					response.setParameter(Parameter.LOCAL_CONNECTION_OPTIONS, new Text());
				}
				if(queryMode) {
					response.setParameter(Parameter.MODE, connectionMode.getDescription());
				}
				if(queryConnectionParams) {
					// see http://tools.ietf.org/html/rfc3435#appendix-F.9
					response.setParameter(Parameter.CONNECTION_PARAMETERS, connectionParameters.toText());
				}
				if(queryLocalConnectionDes) {
					// TODO hrosa - Reusing the existing "SDP" parameter. Replace with LOCAL_CONNECTION_DESCRIPTOR
					response.setParameter(Parameter.SDP, localConnectionDes);
				}
				if(queryRemoteConnectionDes) {
					// TODO hrosa - Need to implement this. MgcpResponse only supports ONE sdp description.
					// response.setParameter(Parameter.SDP, remoteConnectionDes);
				}
				if(queryConnectionAvailability) {
					Text availability = new Text(String.valueOf(connectionAvailable));
					response.setParameter(Parameter.CONNECTION_AVAILABILITY, availability);
				}
			}
			
            try {
                transaction().getProvider().send(evt);
            } catch (IOException e) {
            	logger.error(e);
            } finally {
                evt.recycle();
            }
			
			return 0;
		}
		
	}
	
	private class Rollback extends Task {

		@Override
		public int getQueueNumber() {
			return Scheduler.MANAGEMENT_QUEUE;
		}

		@Override
		public long perform() {
			int code = ((MgcpCommandException)transaction().getLastError()).getCode();
            Text message = ((MgcpCommandException)transaction().getLastError()).getErrorMessage();
            
            MgcpEvent evt = transaction().getProvider().createEvent(MgcpEvent.RESPONSE, getEvent().getAddress());
            MgcpResponse response = (MgcpResponse) evt.getMessage();
            response.setResponseCode(code);
            response.setResponseString(message);
            response.setTxID(transaction().getId());

            try {
                transaction().getProvider().send(evt);
            } catch (IOException e) {
            	logger.error(e);
            } finally {
                evt.recycle();
            } 
			return 0;
		}
		
	}
	
	private class ConnectionParameters {
		private final String PACKETS_SENT = "PS";
		private final String PACKETS_RECEIVED = "PR";
		private final String PACKETS_LOST = "PL";
		private final String OCTETS_SENT = "OS";
		private final String OCTETS_RECEIVED = "OR";
		private final String JITTER = "JI";
		private final String LATENCY = "LA";

		int packetsSent = -1;
		int packetsReceived = -1;
		int packetsLost = -1;
		int octetsSent = -1;
		int octetsReceived = -1;
		int jitter = -1;
		int latency = -1;

		public ConnectionParameters() {
			super();
		}
		
		public Text toText() {
			// Blindly append all possible parameters
			StringBuilder builder = new StringBuilder();
			appendParameter(PACKETS_SENT, packetsSent, builder);
			appendParameter(OCTETS_SENT, octetsSent, builder);
			appendParameter(PACKETS_RECEIVED, packetsReceived, builder);
			appendParameter(OCTETS_RECEIVED, octetsReceived, builder);
			appendParameter(PACKETS_LOST, packetsLost, builder);
			appendParameter(JITTER, jitter, builder);
			appendParameter(LATENCY, latency, builder);
			
			// Verify correctness of the resulting string
			int lastComma = builder.lastIndexOf(",");
			if(lastComma == builder.length() - 1) {
				builder.deleteCharAt(lastComma);
			}
			return new Text(builder.toString().trim());
		}
		
		private void appendParameter(String parameter, int value, StringBuilder builder) {
			if(value >= 0) {
				builder.append(" ").append(parameter).append("=").append(value);
			}
		}
	}

}
