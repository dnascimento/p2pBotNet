package godfather.exception;

import rice.p2p.commonapi.Id;

public class SessionAlreadyOpenException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1148102662447690063L;

	private Id nodeID;
	
	
	public SessionAlreadyOpenException(Id nodeID) {
		this.setNodeID(nodeID);
	}


	/**
	 * @return the nodeID
	 */
	public Id getNodeID() {
		return nodeID;
	}


	/**
	 * @param nodeID the nodeID to set
	 */
	public void setNodeID(Id nodeID) {
		this.nodeID = nodeID;
	}
	
	
	
}
