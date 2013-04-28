import java.util.*;
import java.text.DecimalFormat;

public class MNB_evaluation
{
    MNB_classification c;
    public MNB_evaluation(MNB_classification c)
    {
        this.c = c;
    }

    public double accuracyMeasure(
        Map<String,Pair<String,Map<String, Integer>>>
            test_set)
    {
        int CorrectCount = 0;
        long done = 0;
        DecimalFormat df = new DecimalFormat("00.00%");
        for (Map.Entry<String,Pair<String,Map<String,Integer>>>
                Document :
                test_set.entrySet())
        {
            String ShouldBeLabeled = Document.getValue().First();
            String IsLabeled = c.label(Document.getValue().Second());
            if (ShouldBeLabeled.equals(IsLabeled))
            {
                CorrectCount++;
            }
            if ((done & 0x1F) == 0)
            {
                System.out.print(
                        df.format((double)done/(double)test_set.size()) +
                        "          \r");
            }
            done++;
        }
        System.out.println("                                \rDone.");
        return (double)CorrectCount / (double)test_set.size();
    }
}
