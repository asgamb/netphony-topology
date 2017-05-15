package es.tid.bgp.bgp4Peer.updateTEDB;

import es.tid.bgp.bgp4Peer.bgp4session.BGP4SessionsInformation;
import es.tid.tedb.IntraDomainEdge;
import es.tid.tedb.MultiDomainTEDB;
import es.tid.tedb.TEDB;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Iterator;


/**
 * This class is in charge of storing the BGP4 update messages in a queue to be processing 
 * 
 * @author pac
 *
 */
public class UpdateSlices implements Runnable {

	//TEDBs
	private Hashtable<String,TEDB> intraTEDBs;

	// Multi-domain TEDB to redistribute Multi-domain Topology
	private MultiDomainTEDB multiDomainTEDB;

	private boolean isTest=false;
	private Logger log;

	private String pathSlicer;
	private String IPSlicer;
	private int portSlicer;



	public UpdateSlices(){
		log = LoggerFactory.getLogger("BGP4Peer");
	}

	public void configure( String path, String ip, int port, Hashtable<String,TEDB> intraTEDBs,BGP4SessionsInformation bgp4SessionsInformation,boolean sendTopology,int instanceId,boolean sendIntraDomainLinks, MultiDomainTEDB multiTED){
		this.pathSlicer=path;
		this.IPSlicer=ip;
		this.portSlicer=port;

		this.intraTEDBs=intraTEDBs;

	}

	private String queryForSlices()
	{
		String response = "";
		try
		{
			URL topoplogyURL = new URL("http://"+this.IPSlicer+":"+this.portSlicer+this.pathSlicer);

			log.info("http://+port+topologyPathNodes:::"+"http://"+this.IPSlicer+":"+this.portSlicer+this.pathSlicer);

			URLConnection yc = topoplogyURL.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				response = response + inputLine;
			}
			in.close();
		}
		catch (Exception e)
		{
			log.info(e.toString());
		}
		return response;
	}

	private void parseSlices(String response)
	{
		try {
			//log.info("Inside parseJSON");
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(response);

			JSONArray msg = (JSONArray) obj;
			Iterator<JSONObject> iterator = msg.iterator();
			while (iterator.hasNext())
			{
				JSONObject jsonObject = (JSONObject) iterator.next();
				//System.out.println(jsonObject.get("src-switch"));
				IntraDomainEdge edge= new IntraDomainEdge();

				String key = (String) jsonObject.get("key");//??
				String value = ((String) jsonObject.get("value"));
			}

		}
		catch (Exception e)
		{
			log.info(e.toString());
		}
	}




	public void run() {
		String responseLinks = "";
		String responseNodes = "";

		try
		{

			responseNodes = queryForSlices();//query for topology
			parseSlices(responseNodes);
			log.info("responseNodes:::"+responseNodes);

		}
		catch (Exception e)
		{
			log.info(e.toString());
		}
	}

}