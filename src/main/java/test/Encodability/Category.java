package test.Encodability;

public class Category {
    private ParentCategory category_name;
    private String category_match;
    private double category_weight;

    private int time_found; //only incremented by one for one predicate!

    Category(ParentCategory pc, String cm, double cw){
        category_name = pc;
        category_match = cm;
        category_weight = cw;

        time_found=0;
    }

    public String getCatName(){
        return category_name.getName();
    }

    public ParentCategory getParent(){
        return category_name;
    }

    public String getCatMatch(){
        return category_match;
    }
    public double getCatWeight(){
        return category_weight;
    }

    public void incrementTimeFound(Predicate p){
        time_found += 1;
        category_name.incrementTimeFound(p);
    }
    public int getTimeFound(){
        return time_found;
    }
}
