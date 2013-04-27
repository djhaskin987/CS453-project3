import java.util.*;

public class MNB_evaluation
{
    MNB_classification c;
    public MNB_evaluation(MNB_classification c)
    {
        this.c = c;
    }
    public static double accuracyMeasure(
        Map<String,Pair<String,Map<String, Integer>>>
            test_set)
    {
        int CorrectCount = 0;
        for (Map.Entry<String,Pair<String,Map<String,Integer>>>
                Document :
                test_set.entrySet())
        {
            String ShouldBeLabeled = Document.getValue().First();
            String IsLabeled = c.label(Document.getValue().Second());
            if (ShouldBeLabeled == IsLabeled)
            {
                CorrectCount++;
            }
        }
        return (double)CorrectCount / (double)test_set.size();
    }
}



 
