package test.Encodability;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import java.io.InputStream;
import org.apache.commons.io.IOUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.lambda3.graphene.core.Graphene;
import org.lambda3.graphene.core.discourse_simplification.model.DiscourseSimplificationContent;
import org.lambda3.graphene.core.relation_extraction.model.RelationExtractionContent;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import static org.neo4j.driver.Values.parameters;

public class App 
{
public static HttpURLConnection conn;

public static String output = "945_art1_RE.json";

public static String wikifier_usrname="clement_unisg";
public static String wikifier_key="menfpuggkqvwlekddtvpnhalqyftjx";

public static Map<String, String> complete_list = new TreeMap();

public static void main(String[] args) {
	      try {
	    	
	    	//From XML to Discourse Simplification  
	    	//String all_text = getXML("/Users/Clement/Dropbox/HSG_ICS/Feasibility/Tech/945.xml");
	    	//String all_text="This Regulation lays down the requirements for the design and manufacture of unmanned aircraft systems (‘UAS’) intended to be operated under the rules and conditions defined in Implementing Regulation (EU) 2019/947 and of remote identification add-ons. It also defines the type of UAS whose design, production and maintenance shall be subject to certification. It also establishes rules on making UAS intended for use in the ‘open’ category and remote identification add-ons available on the market and on their free movement in the Union. This Regulation also lays down rules for third-country UAS operators, when they conduct a UAS operation pursuant to Implementing Regulation (EU) 2019/947 within the single European sky airspace.";
	    	//setJson(output, all_text);
	    	//String all_text="This Regulation also defines the type of UAS whose design , production and maintenance shall be subject to certification";
	    	  //getRE("one_sentence.json", all_text);
	    
	    	
	    	 //createGraph();
	    	  
	    	//Error, throws exceptions, doesn't work unfortunately
	    	//RelationExtractionContent rec = getJsonREC(output);
	    	//System.out.println(rec.rdfFormat());
	    	
	    	//read   
	    	//printOutput(getJsonS(output), 100000);
	    	
	    	//processJson();
	    	  
	    	  String test="Drones must not fly higher than thousand meters at night";
	    	  getRE("test.json", test);
	    	  //getDiscourseSimplification("test.json", test);
	    	  //printOutput(getJsonS("test.json"), 100000);
	     
	      }catch (Exception e) {
	          e.printStackTrace();
	      }		
	}

public static String getXML(String filename, String tagname) throws Exception{
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
	DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(new File(filename));
    doc.getDocumentElement().normalize();
    NodeList list = doc.getElementsByTagName(tagname);

	String[] predicates = new String[list.getLength()];
	StringBuilder all_text_builder = new StringBuilder();
	for (int i = 0; i < list.getLength(); i++) {
		Node node = list.item(i);
		predicates[i] = node.getTextContent();
		all_text_builder.append(predicates[i]);
		//System.out.println(predicates[i]);
	}
	return all_text_builder.toString();
}

public static String getTextFile(String filename){
	String everything="";
	try(FileInputStream inputStream = new FileInputStream(filename)) {     
		everything = IOUtils.toString(inputStream);
	}catch(Exception e){}
	return everything;
}

public static void getDiscourseSimplification(String filename, String text) throws Exception{
    Graphene graphene = new Graphene();
	
    //doDiscourseSimplification(text, doCoreference, isolateSentences)
	DiscourseSimplificationContent dsc = graphene.doDiscourseSimplification(text, false, true);
	//Output, default
	String defaultRep = dsc.defaultFormat(true); // set **true** for resolved format
	System.out.print(defaultRep);
	
	dsc.serializeToJSON(new File(filename));
	
}

public static void getRE(String filename, String text) throws Exception{
    Graphene graphene = new Graphene();
	
    //doDiscourseSimplification(text, doCoreference, isolateSentences)
    RelationExtractionContent rec = graphene.doRelationExtraction(text, false, true);
    String defaultRep = rec.defaultFormat(false);
    String rdf = rec.rdfFormat();
	
	rec.serializeToJSON(new File(filename));
	
}


public static JSONArray getJsonA(String filename) throws Exception {
	FileReader reader = new FileReader(filename);
	JSONParser jsonParser = new JSONParser();
	JSONObject obj = (JSONObject)jsonParser.parse(reader);
	return (JSONArray) obj.get("sentences");
}

public static JSONObject getJsonO(String filename) throws Exception {
	FileReader reader = new FileReader(filename);
	JSONParser jsonParser = new JSONParser();
	return (JSONObject)jsonParser.parse(reader);
}

public static String getJsonS(String filename) throws Exception {
	FileReader reader = new FileReader(filename);
	JSONParser jsonParser = new JSONParser();
	JSONObject obj = (JSONObject)jsonParser.parse(reader);
	return obj.toJSONString();
}

public static RelationExtractionContent getJsonREC(String filename) throws Exception{
	return RelationExtractionContent.deserializeFromJSON(new File(filename), RelationExtractionContent.class);
}


public static void printOutput(String uglyJsonString, int maxCharacters) {
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	JsonParser jp = new JsonParser();
	JsonElement je = jp.parse(uglyJsonString);
	String prettyJsonString = gson.toJson(je);
	System.out.println(prettyJsonString.substring(0, maxCharacters<prettyJsonString.length() ? maxCharacters : prettyJsonString.length() ));
}

public static void processJson() throws Exception {
	JSONArray arr = getJsonA(output);
	
	for(int i=0;i<arr.size();i++) {
		goThroughObject((JSONObject)arr.get(i), -1);
	}
	
	
	//recap
	for(String key:complete_list.keySet()) {
		System.out.println(key+": "+complete_list.get(key));
	}
	
	
	/*
	String sentence_tmp = complete_list.get("0.0_arg1") + " "+complete_list.get("0.0_relation") + " "+complete_list.get("0.0_arg2");
	System.out.println("Input: "+sentence_tmp);
	getWiki(sentence_tmp);
	*/
	
	/*
	System.out.println();
	System.out.println("--Recap--");
	int no_relation=0;
	for(int i=0;i<sentence_type.size();i++) {
		int found_relations = sentence_type.get(i).split("/").length -1;
		if(found_relations<=0) {
			found_relations=0;
			no_relation+=1;
		}
		System.out.println("Sentence "+i+": "+found_relations);
	}
	long stat = 100-Math.round((double)no_relation / sentence_type.size() *100);
	System.out.println("Total sentences processed by OpenIE: "+ stat+"%");
	*/
}

public static void goThroughObject(JSONObject obj, Integer sentence_idx) {
	Iterator<String> itr = obj.keySet().iterator();
	
	while(itr.hasNext()) {
		String nextKey = itr.next();
		
		
		
		if (!Objects.isNull(obj.get(nextKey)) && obj.get(nextKey).getClass() == JSONArray.class) {
			goThroughArray((JSONArray)obj.get(nextKey), sentence_idx);
		}
		else if(!Objects.isNull(obj.get(nextKey)) && obj.get(nextKey).getClass() == JSONObject.class) {
			goThroughObject((JSONObject)obj.get(nextKey), sentence_idx);		
		}		
		else if(!Objects.isNull(obj.get(nextKey)) && obj.get(nextKey).getClass() == String.class) {
			if(nextKey.equals("relation") || nextKey.equals("arg1") || nextKey.equals("arg2")) {
				//get for the lower level sentence_idx2 within the object
				//int contextLayer = getContextLayer(obj);
				int subSentence = countSubSentences(sentence_idx, nextKey);			
				String newKey = sentence_idx+"."+subSentence+"_"+nextKey;
				complete_list.put(newKey, (String)obj.get(nextKey));
			}

		}
		else if(nextKey.equals("sentenceIdx")) {
			sentence_idx=((Long)obj.get(nextKey)).intValue();
		}

			
	}
}

public static int countSubSentences(int sentence_Idx, String nextKey) {
	for(int i=0;i<10;i++) {
		if(complete_list.get(sentence_Idx+"."+i+"_"+nextKey)==null) {
			return i;
		}
	}
	return -1;
	/*int max_value=0;
	for(String key:map.keySet()) {	
		String[] nb = key.split("_");
		int sentence = new Integer(nb[0].split("\\.")[0]);
		int sub = new Integer(nb[0].split("\\.")[1]);
		if(sentence==sentence_Idx && sub>max_value) max_value = sub;
	}
	return max_value;*/
}

public static int getContextLayer(JSONObject obj) {
	Iterator<String> itr = obj.keySet().iterator();
	
	while(itr.hasNext()) {
	String nextKey = itr.next();	
		if(nextKey.equals("contextLayer")) return ((Long)obj.get(nextKey)).intValue();
	}
	return -1;
}

public static void goThroughArray(JSONArray arr, Integer sentence_idx) {
	for(int i=0;i<arr.size();i++) {
		goThroughObject((JSONObject)arr.get(i), -1);
	}
}

public static Map<String, String> mergeTreeMaps(Map<String, String> tmp, Map<String, String> output){
	for(String key:tmp.keySet()) {
		output.put(key, tmp.get(key));
	}
	return output;
}

public static void createGraph() {
	
	
	Driver driver = GraphDatabase.driver("neo4j+s://a07a12b3.databases.neo4j.io", AuthTokens.basic("neo4j", "57JanY0tSzszSggP52eKqzpgIBZ3Pl6qkcM9p3zCW-0"));
	Session session = driver.session();
	AddPerson("Clement", session);
	
	/*Path databaseDirectory;
	DatabaseManagementService managementService = new DatabaseManagementServiceBuilder( databaseDirectory ).build();
	GraphDatabaseService graphDb = managementService.database("test1");
	
	Transaction tx = (Transaction) graphDb.beginTx();
	tx.commit();
	tx.createNode();
	*/
	session.close();
	System.out.println("Opened and closed");
	

}

public static void AddPerson(String name, Session session){
	session.run( "CREATE (a:Person {name: $name})", parameters( "name", name ) );

}

public static WikiAnswer getWiki(String text, String output_name, boolean print) throws Exception {
	double threshold=0.1;

	String url = "http://www.wikifier.org/annotate-article?text="+URLEncoder.encode(text)
	+"&lang=en&wikiDataClasses=true&ranges=false&userKey="+wikifier_key;
	//+"&pageRankSqThreshold="+threshold+"&applyPageRankSqThreshold=true&minLinkFrequency=2&maxMentionEntropy=3";

	//System.out.println("url: "+url);
	//String output_name = "945_art1_graphene1.json";
	String answer = connect(new URL(url), output_name);
	
	JSONObject obj = getJsonO(output_name);
	JSONArray arr = (JSONArray) obj.get("annotations");
	
	float pr=0;
	float cos=0;
	String title="";
	
	for(int i=0;i<arr.size();i++) {
		JSONObject sub_obj = (JSONObject)arr.get(i);
		Iterator<String> itr = sub_obj.keySet().iterator();
		
		float pr_tmp=((Number)sub_obj.get("pageRank")).floatValue();
		float cos_tmp = ((Number)sub_obj.get("cosine")).floatValue();
		/* top cos; could swap for top pr */
		if(cos_tmp>cos){
			pr=pr_tmp;
			cos=cos_tmp;
			title=(String)sub_obj.get("title");
		}

	}
	return new WikiAnswer(title, cos, pr);
}

public static String connect(URL url, String filename) {
	BufferedReader reader;
	String line;
	StringBuilder responseContent = new StringBuilder();
	try{
		conn = (HttpURLConnection) url.openConnection();
		
		// Request setup
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5000);// 5000 milliseconds = 5 seconds
		conn.setReadTimeout(5000);
		
		// Test if the response from the server is successful
		int status = conn.getResponseCode();
		
		if (status >= 300) {
			reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			while ((line = reader.readLine()) != null) {
				responseContent.append(line);
			}
			reader.close();
		}
		else {
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = reader.readLine()) != null) {
				responseContent.append(line);
			}
			reader.close();
		}
		
		if(filename.length()>0) {
			//System.out.println(responseContent.toString());
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		    writer.write(responseContent.toString());
		    
		    writer.close();
		    //System.out.println("**Wikifier response saved**");
		}
		return responseContent.toString();
	}
	catch (Exception e) {
		e.printStackTrace();
		return "";
	} finally {
		conn.disconnect();
	}
}


}
