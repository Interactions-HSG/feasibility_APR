package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


import org.w3c.dom.Node;
import org.lambda3.graphene.core.Graphene;
import org.lambda3.graphene.core.relation_extraction.model.RelationExtractionContent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SentenceComplexity 
{
    public static String output = "gdpr_1_re.json";
    public static Map<String, String> complete_list;
 
    public static Graphene graphene = new Graphene();
    public static boolean debug = false;
    
    public static void main( String[] args )
    {

         try {
            String text = getXML("charter_human_rights.xml", "ALINEA");
            //String text = getTextFile("rate_rebates_nz.txt");
        	 String[] sub_text1 = text.split("\\.");
            
            StringBuilder sentence_complexity = new StringBuilder();
            
            for(int i=0;i<sub_text1.length;i++){
            //for(int i=0;i<2;i++) {
    				String[] sub_text2 = sub_text1[i].split(";");
    				if(sub_text2.length==0){
    					sub_text2 = new String[1];
    					sub_text2[0] = sub_text1[i];
    				}
    				for(int j=0;j<sub_text2.length;j++){
    					//System.out.println("**"+sub_text2[j]);
    					getRE(sub_text2[j]);
    					double score_tmp = processJson(sub_text2[j].split(" ").length, sub_text2[j]);
    					score_tmp = Math.round((double)score_tmp*100.0)/100.0;
    					sentence_complexity.append(score_tmp+",\n");
    					//System.out.println(sentence_complexity.toString());
    				}
            }
            System.out.println(sentence_complexity.toString());

        } catch (Exception e) {}
        

        System.out.println("**Done**");
    }
    
    public static String getTextFile(String filename) throws Exception{
    	String everything="";
    	
    	BufferedReader br = new BufferedReader(new FileReader(filename));
    	try {
    		
    	    StringBuilder sb = new StringBuilder();
    	    String line = br.readLine();

    	    while (line != null) {
    	        sb.append(line);
    	        sb.append(System.lineSeparator());
    	        line = br.readLine();
    	    }
    	   everything = sb.toString();
    	} finally {
    	    br.close();
    	}
    	return everything;
    }

    public static double processJson(int original_length, String sentence) throws Exception {
    	complete_list = new TreeMap();
    	double to_return=0;
        int nb_sub_sentences=0;
    	JSONArray arr = getJsonA(output);
        
        for(int i=0;i<arr.size();i++) {
            goThroughObject((JSONObject)arr.get(i), -1);
        }

        //recap -- would do the counting here
	    for(String key:complete_list.keySet()) {
	    	if(key.contains("arg2")) {
	    		to_return+=complete_list.get(key).split(" ").length;
	    		nb_sub_sentences += 1;
	    	}
	    }
	    if(nb_sub_sentences != 0) {
	    	to_return = to_return / nb_sub_sentences; //take the average
	    	if(original_length != 0) {
	    		
	    		if(to_return/original_length > 1) {
	    			if(debug) {
		    			System.out.println("*** > 1 "+sentence);
		    			for(String key:complete_list.keySet()) {
		    				System.out.println(key+": "+complete_list.get(key));
		    			}
		    			System.out.println();
	    			}

	    			return 1;
	    		}
	    			
	    		else 
	    			return (to_return/original_length);
	    	}
	    		
	    }
	    else {
	    	if(debug) {
		    	System.out.println("--- nbSentence==0 "+sentence);
				for(String key:complete_list.keySet()) {
					System.out.println(key+": "+complete_list.get(key));
				}
				System.out.println();
	    	}
	    	
	    }
	    return 1;
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

    public static void goThroughArray(JSONArray arr, Integer sentence_idx) {
        for(int i=0;i<arr.size();i++) {
            goThroughObject((JSONObject)arr.get(i), -1);
        }
    }

    public static int countSubSentences(int sentence_Idx, String nextKey) {
        for(int i=0;i<10;i++) {
            if(complete_list.get(sentence_Idx+"."+i+"_"+nextKey)==null) {
                return i;
            }
        }
        return -1;
    }

    public static JSONArray getJsonA(String filename) throws Exception {
        FileReader reader = new FileReader(filename);
        JSONParser jsonParser = new JSONParser();
        JSONObject obj = (JSONObject)jsonParser.parse(reader);
    	//JSONObject obj = new JSONObect(serialised);
        return (JSONArray) obj.get("sentences");
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

    /* Writing into a file to only read it right after is not optimal
     * But I couldn't find a way to obtain the JSON string result otherwise
     * */
    public static String getRE(String text){
        try {
            RelationExtractionContent rec = graphene.doRelationExtraction(text, false, true);
            //return rec.serializeToJSON();
            rec.serializeToJSON(new File(output));  
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
