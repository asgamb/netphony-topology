package es.tid.bgp.bgp4Peer.updateTEDB;

import es.tid.bgp.bgp4.update.fields.MapKeyValue;
import es.tid.tedb.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Hashtable;
import java.util.LinkedList;

//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;


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
	private String localDomain;



	public UpdateSlices(){
		log = LoggerFactory.getLogger("BGP4Peer");
	}

	public void configure(String path, String ip, int port, String domain, Hashtable<String,TEDB> intraTEDBs){
		this.pathSlicer=path;
		this.IPSlicer=ip;
		this.portSlicer=port;
		this.localDomain=domain;
		this.intraTEDBs=intraTEDBs;

	}

	private String queryForSlices()
	{
		String response = "";
		try
		{
			URL topoplogyURL = new URL("http://"+this.IPSlicer+":"+this.portSlicer+this.pathSlicer);

			log.info("http://"+this.IPSlicer+":"+this.portSlicer+this.pathSlicer);

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

			DomainTEDB domainTEDB= null;

			domainTEDB=(DomainTEDB)intraTEDBs.get(localDomain);
			SimpleTEDB simpleTEDB=null;
			if (domainTEDB instanceof SimpleTEDB){
				simpleTEDB = (SimpleTEDB) domainTEDB;
			}else if (domainTEDB==null){
				simpleTEDB = new SimpleTEDB();
				simpleTEDB.createGraph();
				log.info("not present, we need to create intra tedb with id "+localDomain);

				try {
					simpleTEDB.setDomainID((Inet4Address) InetAddress.getByName(localDomain));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				this.intraTEDBs.put(localDomain, simpleTEDB);

			}
			else {
				log.error("PROBLEM: TEDB not compatible");
				return;
			}

			//log.info("Inside parseJSON");
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(response);
			JSONObject objj= (JSONObject) obj;
			//JSONArray msg = (JSONArray) obj;
			JSONArray metadata = objj.getJSONArray("metadata");
			if(simpleTEDB.getSlices()==null){
				log.info("slices in ted are null");
				Slices slices = new Slices();
				slices.setLearntFrom("local");
				slices.setdomainID(localDomain);
				LinkedList<MapKeyValue> slicesList;
				slicesList=	new LinkedList<MapKeyValue>();
				//slicesList.add(elem);
				slices.setSlices(slicesList);
				simpleTEDB.setSlices(slices);
			}

			for (int i = 0; i < metadata.length(); ++i) {
				JSONObject metax = metadata.getJSONObject(i);
				String key = metax.getString("key");
				String value = metax.getString("value");
				log.info("read key="+key+" and value="+value);// ...
				MapKeyValue elem = new MapKeyValue();
				elem.setKey(key);
				elem.setValue(value);
				log.debug("Received Slice with key "+elem.key+" and value "+elem.value);
				boolean found=false;
				if (simpleTEDB.getSlices().getSlices().size()==0){
					log.info("Slice dict already created simply add Slice with key "+elem.key+" and value "+elem.value);
					simpleTEDB.getSlices().getSlices().add(elem);
				}
				else{
					log.debug("Size greater than 0");
					for(int k=0; i< simpleTEDB.getSlices().getSlices().size();k++) {
						MapKeyValue temp = simpleTEDB.getSlices().getSlices().get(k);
						if ((temp.getKey().equals(elem.getKey()))&&(temp.getValue().equals(elem.getValue()))){
							found = true;
							log.info("Already present, nothing to do");
							break;
						}
					}
					if (found==false){
						simpleTEDB.getSlices().addSlice(elem);
						log.info("new slice added");
					}
				}

			}
			log.info(simpleTEDB.getSlices().toString());

		}
		catch (Exception e)
		{
			log.info(e.toString());
		}
	}




	public void run() {
		String response = "";

		try
		{

			response = queryForSlices();//query for topology
			log.info("response for Slices:::"+response);
			parseSlices(response);
		}
		catch (Exception e)
		{
			log.info(e.toString());
		}
	}

}