package test.Encodability;

import java.util.ArrayList;
import java.util.List;

public class ParentParentCategory {
    
    private List<Predicate> previous; //each ParentParent can only be incremented once per predicate; this is to ensure it
    private int time_found;
    private String name;

    ParentParentCategory(String category_name){
        name = category_name;
        time_found = 0;
        previous = new ArrayList<Predicate>();
    }

    public String getName(){
        return name;
    }

    public void incrementTimeFound(Predicate current){
        if(!previous.contains(current)){
            time_found += 1;
            previous.add(current);
        }
    }

    public int getTimeFound(){
        return time_found;
    }

}
