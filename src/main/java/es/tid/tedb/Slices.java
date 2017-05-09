package es.tid.tedb;

//import es.tid.bgp.bgp4Peer.peer.MapKeyValue;

import es.tid.bgp.bgp4.update.fields.MapKeyValue;

import java.util.LinkedList;

public class Slices {

	private LinkedList<MapKeyValue> slicesList;
	private String learntFrom;
	private String domainID;
	/**
	 * TEDB logger
	 */
	public Slices()
	{
		//initWLANs();
	}


	public LinkedList<MapKeyValue> getSlices() {
		return slicesList;
	}

	public void setSlices(LinkedList<MapKeyValue> slices) {
		this.slicesList = slices;
	}
	public String getLearntFrom() {
		return learntFrom;
	}
	public void setLearntFrom(String learntFrom) {
		this.learntFrom = learntFrom;
	}

	public String getdomainID() {
		return domainID;
	}

	public void setdomainID(String ID) {
		this.domainID = ID;
	}

	public String toString(){
		String ret="";
		ret=ret+"lernt from: "+learntFrom;
		ret=ret+"domain: "+domainID;
		for(int i=0; i< slicesList.size();i++) {
			MapKeyValue temp = slicesList.get(i);
			ret=ret+"key: "+temp.getKey();
			ret=ret+"value: "+temp.getValue();

		}
		
		return ret;
	}


	
}
