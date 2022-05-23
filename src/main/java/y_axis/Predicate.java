package test.Encodability;

import java.util.List;
import java.util.ArrayList;

public class Predicate {
    private List<Category> my_categories;
    double my_score;
    String text;

    private List<String> words_found;   // does not include open-textured term
    int open_textured=0;

    float summed_cos;
    int count_cos=0;
    int sum_words_recognised=0;
    int nb_tested = 0;

    int nb_words_after_verb=0;
    int nb_words=0;

    Predicate(String t){
        text = t;
        my_categories = new <Category>ArrayList();
        words_found = new <String>ArrayList();
    }

    public void addCategory(Category c, int increment, String word){
        my_categories.add(c);
        
        nb_tested += increment;
        
        if(c.getParent().getGrandParentName().compareTo("Open-textured")==0){
            //increment is the number found!
            int nb_words_in_open_textured = word.split(" ").length;
            open_textured += increment * nb_words_in_open_textured;
        }
        else{
            sum_words_recognised += increment;
            words_found.add(word);
        }
            
    }

    public void incrementNbWords(int i){
        nb_words += i;
    }

    public void incrementAfterVerb(int i){
        nb_words_after_verb += i;
    }

    public double getSentenceComplexity(){
        if(nb_words_after_verb==0)nb_words_after_verb = nb_words; // it means that it didn't find a verb, and usually, this is because of very long sentences!
        double to_return = ((double)nb_words_after_verb / nb_words);
        to_return = Math.round((double)to_return*100.0)/100.0;
        return to_return;
    }

    public boolean hasPredicateCategory(Category c){
        for(int i=0;i<my_categories.size();i++){
            if(compareCategories(my_categories.get(i), c))return true;
        }
        return false;
    }
    
    private boolean compareCategories(Category a, Category b){
        if(a.getCatMatch().compareTo(b.getCatMatch())==0 && a.getCatName().compareTo(b.getCatName())==0) return true;
        else return false;
    }

    public void setSummedCos(float cos, int increment, String words){
        summed_cos += cos;
        count_cos += 1;
        sum_words_recognised += increment;
        nb_tested += increment;
        
        words_found.add(words);
    }

    public double getWordComplexity(){
        int non_recognised = nb_tested - sum_words_recognised;
        double calc = ((double)(non_recognised + open_textured)) / (nb_tested+open_textured);
        calc = Math.round((double)calc*100.0)/100.0;
        return calc;
    }

    public boolean wasFound(String s){
        return words_found.contains(s);
    }

    public void incrementWordsTested(int i){
        nb_tested += i;
    }

    public float getAverageCos(){
        if(count_cos != 0)
            return (summed_cos / count_cos);
        else return 0;
    }

    public double getPercentWordsRecognised(){
        //return the percentage of words recognised on the number of words tested!

        //int nb_words = text.split(" ").length;
        double percent = 0;
        if(nb_tested != 0) percent = ((double)sum_words_recognised) / nb_tested;
        return percent;
    }

    public String getText(){
        return text;
    }

    public void setScore(double s){
        my_score = s;
    }
    public double getScore(){
        return my_score;
    }
}
