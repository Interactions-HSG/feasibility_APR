package test.Encodability;

public class WikiAnswer {
    private String top_finding;
    private float cosine;
    private float page_rank;

    WikiAnswer(String tf, float cos, float pr){
        top_finding = tf;
        cosine = cos;
        page_rank = pr;
    }

    WikiAnswer(String tf, float cos){
        top_finding = tf;
        cosine = cos;
    }

    public String getFinding(){
        return top_finding;
    }
    public float getCosine(){
        return cosine;
    }
    public float getPR(){
        return page_rank;
    }

}
