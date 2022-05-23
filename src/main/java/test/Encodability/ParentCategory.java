package test.Encodability;

public class ParentCategory {
    private String name;
    private ParentParentCategory grandparent;
    private int time_found;

    ParentCategory(String n, ParentParentCategory pp){
        name = n;
        grandparent = pp;
        time_found = 0;
    }

    public void incrementTimeFound(Predicate p){
        time_found +=1;
        grandparent.incrementTimeFound(p);
        //System.out.println("incremented "+name+" to: "+time_found);
    }
    public int getTimeFound(){
        return time_found;
    }

    public static boolean hasSameParent(Category a, Category b){
        if(a.getCatName().compareTo(b.getCatName())==0) return true;
        else return false;
    }
    public String getName(){
        return name;
    }

    public String getGrandParentName(){
        return grandparent.getName();
    }

}
