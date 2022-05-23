package test.Encodability;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import org.neo4j.cypher.internal.expressions.functions.E;

import edu.stanford.nlp.parser.lexparser.*;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.CoreNLPProtos.DependencyGraph.Node;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import scala.reflect.io.Path;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.CoreDocument;

public class Approach2 {
	
	public static String text = "Drones must not fly higher than thousand meters at night";
	public static double threshold = 0.4;
	public static HashMap<String, WikiAnswer> words = new HashMap<String, WikiAnswer>(); //in order not to query several times the same word; saves time
	public static Syntactic syntactic;

	public static boolean VB_predicate=false; //used for sentence complexity, to count after the verb

	public static String filename_output = "tmp_output_approach2.txt";

	public static String token_iate="";
	public static void main(String[] args) {
		
		//Reference (definition):means:0.5
		//deleted from categories.txt and to check after NLP Core bc of issue between noun/vb 

		try {
			//text=getStringFromFile("945_art1_input.txt");
			//text = readFile("945_art1_input.txt", StandardCharsets.US_ASCII);
			
			//text = App.getXML("gdpr.xml", "ALINEA");
			//text = App.getXML("can_overtime.xml", "Text");
			//text = App.getXML("charter_human_rights.xml", "ALINEA");
			text = App.getTextFile("/Users/Clement/eclipse-workspace/test/rate_rebates_nz.txt");
			//text = "Data localisation requirements shall be prohibited, unless they are justified on grounds of public security in compliance with the principle of proportionality.";

			token_iate = IATE.connectIATE();

			syntactic = new Syntactic();

			List<Predicate> predicates = new <Predicate>ArrayList();
			//predicate by period and semi-colons
			String[] sub_text1 = text.split("\\.");

			FileWriter fileWriter = new FileWriter(filename_output);
    		PrintWriter printWriter = new PrintWriter(fileWriter);

			for(int i=0;i<sub_text1.length;i++){
			//for(int i=0;i<60;i++){
				String[] sub_text2 = sub_text1[i].split(";");
				if(sub_text2.length==0){
					sub_text2 = new String[1];
					sub_text2[0] = sub_text1[i];
				}
				for(int j=0;j<sub_text2.length;j++){
					System.out.println( (i+j+1) +"/"+(sub_text1.length+sub_text2.length)+": "+ sub_text2[j]+" ");
					Tree parseTree = getNLPParser(sub_text2[j]);
					Predicate p = new Predicate(sub_text2[j]);

					//syntactic parsing first as countAndScore is at the predicate level and navigate/processNode at the word level
					p = syntactic.countAndScore(p);

					//semantic
					navigate(parseTree, p);
					predicates.add(p);

					//System.out.println(Math.round(p.getPercentWordsRecognised()*100)+"%"+","+p.getWordComplexity()+","+p.getSentenceComplexity());
					
					printWriter.println(p.getText()+";"+p.getWordComplexity());
					
					VB_predicate = false;
				}
			}
			printWriter.close();
			System.out.println();
			System.out.println("Number of predicates: "+predicates.size());

			//average for all predicates
			float avg_cos=0;
			double avg_words_recognised=0;
			double avg_word_complexity = 0;
			double avg_sentence_complexity=0;
			double average_encodability=0;
			for(int i=0;i<predicates.size();i++){
				avg_cos += predicates.get(i).getAverageCos();
				avg_words_recognised += predicates.get(i).getPercentWordsRecognised();
				avg_word_complexity += predicates.get(i).getWordComplexity();
				avg_sentence_complexity += predicates.get(i).getSentenceComplexity();
				double encodability = 1-Math.sqrt(0.5 * (predicates.get(i).getWordComplexity() * predicates.get(i).getWordComplexity() + 
					predicates.get(i).getSentenceComplexity()*predicates.get(i).getSentenceComplexity()));
				average_encodability += encodability;
				//System.out.println(predicates.get(i).getWordComplexity()+","+predicates.get(i).getSentenceComplexity()+","+encodability);
				System.out.println(predicates.get(i).getWordComplexity()+",");
			}
			avg_cos = (float) (avg_cos / predicates.size());
			avg_words_recognised = avg_words_recognised / predicates.size();
			avg_word_complexity = avg_word_complexity / predicates.size();
			avg_sentence_complexity = avg_sentence_complexity / predicates.size();
			average_encodability = average_encodability / predicates.size();
			System.out.println();
			//System.out.println("Average cosine of words detected (above threshold) on all predicates: "+ Math.round ((double)avg_cos * 100)+"%");
			System.out.println("Average number of words recognised when tested on all predicates: "+ Math.round(avg_words_recognised*100)+"%");
			System.out.println("Average word complexity: "+ Math.round(avg_word_complexity*100) +"%");
			
			//System.out.println("Average sentence complexity: "+ Math.round(avg_sentence_complexity*100) +"%");
			//System.out.println("Average encodability: "+ Math.round(average_encodability*100) +"%");
			System.out.println();
			syntactic.showGeneralStatistics(predicates);
			//parseTree.pennPrint();
			//inOntology("cleaning");
			//App.getWiki(text, "wiki_drones", true);
			//System.out.println("done");



		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*TreebankLanguagePack tlp =  lp.treebankLanguagePack(); 
	    GrammaticalStructureFactory gsf =  tlp.grammaticalStructureFactory(); 
	    GrammaticalStructure gs =  gsf.newGrammaticalStructure(parseTree); 
	    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed(); 
	    System.out.println(tdl); 
	    
	    for(TypedDependency dependency : tdl) { 
	        System.out.println("Governor Word: [" + dependency.gov()  
	            + "] Relation: [" + dependency.reln().getLongName() 
	            + "] Dependent Word: [" + dependency.dep() + "]"); 
	    } */
		
		//connectGraphDB("clement", "7xsLyrT_$4");	
	}

	public static Tree getNLPParser(String text){
		// Stanford NLP Parser
		LexicalizedParser lp = LexicalizedParser.loadModel();
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		List<CoreLabel> wordList = tokenizerFactory.getTokenizer(new StringReader(text)).tokenize();
		return lp.apply(wordList);
	}
	
	public static String readFile(String path, Charset encoding)
	{
		String content="";
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(path));
			content = new String(encoded, encoding);
		} catch (Exception e) {}
		return content;
	}

	public static void navigate(Tree t, Predicate p) throws Exception{
		List<Tree> children = t.getChildrenAsList();
		
		String np_string="";

		for(Tree child : children) {
			if(child.size()==2) {
				String word=child.firstChild().toString();

				if(t.value().compareTo("NP")==0){
					if(np_string.length()==0) np_string += word;
					else np_string = np_string+" "+word;
				}
				else{
					String label = child.value();
					p.incrementNbWords(1);
					processNode(label, word, p);
				}
			}
			
			else if(!child.isLeaf()) {
				List<Tree> children_child = child.getChildrenAsList();
				for(Tree t2 : children_child) {
					//System.out.println("dig-further: "+t2.value());
					navigate(t2, p);
					String label = t2.value();
					if(t2.size()==2){
						String word=t2.firstChild().toString();
						p.incrementNbWords(1);
						processNode(label, word, p);
					}
				}
			}	
		}

		if(t.value().compareTo("NP")==0){
			int increment = np_string.split(" ").length;
			p.incrementNbWords(increment);
			processNode("NP", np_string, p);
		}
	}
	
	public static void processNode(String label, String value, Predicate p) throws Exception{
		int increment=value.split(" ").length;

		if(VB_predicate)
			p.incrementAfterVerb(increment);
			
		
		/* syntactic processing has been done first in main
		if the word has been found in syntactic, shouldn't test it further! */
		if(!p.wasFound(value)){
			/* semantic processing */
			float cos=0;
			if(label.compareTo("NN")==0 || label.compareTo("NNS")==0 || 
				label.compareTo("NP")==0 ||
				label.compareTo("VB")==0 || label.compareTo("VBZ")==0 || 
				label.compareTo("VBG")==0 || label.compareTo("VBN")==0){
				
				if(inOntology(value) || inSchema(value) || IATE.queryIATE(token_iate, value))cos=1;
				//if(inSchema(value) || IATE.queryIATE(token_iate, value))cos=1;
				else cos=inWiki(value, false);
				
				if(cos<threshold){
					/*
					//before dismissing it as not found, if it's a combination JJ+NN(S), try again just with NN(S)
					if(increment>1)processNode(label, value.split(" ")[1], p);
					else
					*/
					p.incrementWordsTested(increment);
				}
				if(label.compareTo("VB")==0 || label.compareTo("VBZ")==0){
					VB_predicate=true;

					//handling the problem with the syntactic search for definitions for "means"/"mean" here
					if(value.compareTo("means")==0 || value.compareTo("mean")==0){
						ParentParentCategory pp = new ParentParentCategory("References");
						ParentCategory pc = new ParentCategory("Reference (definition)", pp);
						Category c = new Category(pc, value, 0.5);
						c.incrementTimeFound(p);
						p.addCategory(c, increment, value);
					}
				}
					
			}
			//modal verb
			else if(label.compareTo("MD")==0 || label.compareTo("CD")==0) cos=1;
			if(cos >= threshold) p.setSummedCos(cos, increment, value);
		}
	}
	
	public static boolean inOntology(String s) throws Exception{
		GraphDBInterface gdb=new GraphDBInterface("encodableLaw", "clement", "7xsLyrT_$4");
		
		String query = "SELECT * where {BIND(\""+
			s + "\"@en AS ?label) {?s ?p ?label ;} UNION {?label ?a1 ?a2} UNION {?a1 ?label ?a2}}";
		//String query = "SELECT * where {?s ?p ?o .}";
		Map ans = gdb.query(query);
		
		int size_ans = ((ArrayList)((Map)ans.get("results")).get("bindings")).size();
		if(size_ans>0)
			return true;
		else
			return false;
	}

	public static boolean inSchema(String s){
		try{
			String s2 = getSingular(s);
			String s3 = s2.substring(0, 1).toUpperCase() + s2.substring(1);
			URL url = new URL("https://schema.org/"+s3);
			HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));	
			   if (br.readLine() != null) return true;
			   br.close();
			return false;
		}
		catch(Exception e){return false;}
	}


	public static String getSingular(String text){
		Properties props = new Properties();
		// set the list of annotators to run
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		CoreDocument document = pipeline.processToCoreDocument(text);
		if(document.tokens().size()>0)return document.tokens().get(0).lemma();
		else return "";
	}

	public static float inWiki(String s, boolean printDebug) throws Exception{
		/* Currently, returns top by PR, not by cosine! */
		if(words.get(s) == null){
			WikiAnswer answer=App.getWiki(s.toLowerCase(), "tmp", false);
			if(printDebug) System.out.println(s+" vs Wikifier: "+answer.getFinding()+" cos:"+answer.getCosine());
			words.put(s, answer);
			return answer.getCosine();
		}
		else return words.get(s).getCosine();
	}
}
