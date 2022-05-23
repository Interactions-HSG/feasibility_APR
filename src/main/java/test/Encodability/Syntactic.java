package test.Encodability;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

public class Syntactic {
	
	public static String cat_input = "categories.txt";
	private static boolean print_all_scores = false;
	
	//create a hashmap <String, String> to create ParentParent linked to Parent
	//when create a parent, already give a ParentParent (todo: change call to initiate)
	private static HashMap<String, String> relations = new HashMap<String, String>();
	private static List<ParentParentCategory> all_grandparents = new ArrayList<ParentParentCategory>();
	public static List<Category> all_categories = new ArrayList<Category>();

	Syntactic(){
		loadModel();
	}
	public static void main(String[] args) {
		Syntactic syntactic = new Syntactic();
		
		try {
			String xml = App.getXML("gdpr_xml.txt", "ALINEA");
			
			//per sentence, do the rating
			String[] all_sentences = xml.split("\\.");
			List<Predicate> predicates = new <Predicate>ArrayList();
			for(int i=0;i<all_sentences.length;i++){
				int sentence_score = 0;
				String[] sub_sentences = all_sentences[i].split(";");
				if(sub_sentences.length==0){
					sub_sentences = new String[1];
					sub_sentences[0] = all_sentences[i];
				}
				for(int j=0;j<sub_sentences.length;j++){
					Predicate p = new Predicate(sub_sentences[j]);
					p = syntactic.countAndScore(p);
					predicates.add(p);
					if(print_all_scores) System.out.print(sentence_score+",");
				}	
			}

			//statistics per in detailled categories
			/*
			//Here to print the details of sub-categories
			Category previous = new Category(new ParentCategory("", new ParentParentCategory("")), "", 0);
			Category current = new Category(new ParentCategory("", new ParentParentCategory("")), "", 0);
			for(int i=0;i<all_categories.size();i++){
				current = all_categories.get(i);
				if(i==0) previous = all_categories.get(i);
				if(i==0 || !ParentCategory.hasSameParent(current, previous)){
					double percent = (double) current.getParent().getTimeFound() / predicates.size() * 100;
					percent = Math.round(percent);
					System.out.println(current.getParent().getName()+": "+current.getParent().getTimeFound()+" ("+percent+"%)");
				}
				previous = current;
			}
			*/
			syntactic.showGeneralStatistics(predicates);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showGeneralStatistics(List<Predicate> predicates){
		System.out.println("Predicate categories");
		
		
		//general for the broader categories
		for(int i=0;i<all_grandparents.size();i++){
			double percent = (double)all_grandparents.get(i).getTimeFound() / predicates.size() * 100;
			percent = Math.round(percent);
			System.out.println(all_grandparents.get(i).getName()+": "+all_grandparents.get(i).getTimeFound()+" ("+percent+"%)");
		}
	}

	public Predicate countAndScore(Predicate p){
		int sentence_score=0;
		for(int k=0;k<all_categories.size();k++){
			int nb_found=StringUtils.countMatches(p.getText(), all_categories.get(k).getCatMatch());
			if(nb_found>0){
				p.addCategory(all_categories.get(k), nb_found, all_categories.get(k).getCatMatch());
				all_categories.get(k).incrementTimeFound(p);
			}
			sentence_score += nb_found * all_categories.get(k).getCatWeight();
			p.setScore(sentence_score);
		}
		return p;
	}
	
	public void loadModel(){
		try{
			relations.put("Prohibition", "Legal effect");
			relations.put("Permission", "Legal effect");
			relations.put("Obligation", "Legal effect");
			relations.put("Dispensation", "Legal effect");
			relations.put("Definitional", "if-then");
			relations.put("Proof", "if-then");
			relations.put("Exception", "if-then");
			relations.put("Condition", "if-then");
			relations.put("Threshold", "if-then");
			relations.put("Internal reference", "References");
			relations.put("External reference", "References");
			relations.put("Reference (definition)", "References");
			relations.put("Fundamental rights", "Open-textured");
			relations.put("Quality of open-textured", "Open-textured");
			relations.put("Other open-textured", "Open-textured");
	
			BufferedReader br = new BufferedReader(new FileReader(cat_input));
			String line;
			ParentCategory parent = new ParentCategory("", new ParentParentCategory(""));
			ParentParentCategory grandparent = new ParentParentCategory("");
			while ((line = br.readLine()) != null) {
			   String[] tmp = line.split(":");
			   if(tmp[0].compareTo(parent.getName())!=0){
					boolean to_include=true;
					//check if grandparent already in the arraylist
					for(int i=0;i<all_grandparents.size();i++){
						if(all_grandparents.get(i).getName().compareTo(relations.get(tmp[0]))==0){
							to_include=false;
							grandparent = all_grandparents.get(i);
					}
				}
				if(to_include){
					grandparent = new ParentParentCategory(relations.get(tmp[0]));
					all_grandparents.add(grandparent);
				}
				parent=new ParentCategory(tmp[0], grandparent);
			   }
			   all_categories.add(new Category(parent, tmp[1], Double.parseDouble(tmp[2])));
			}
			br.close();
		}catch(Exception e){}
	
	}
	
}
