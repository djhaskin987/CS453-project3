public class MNB_probability
{
    Map<String, Pair<String, Map<String, Integer>>>
        training_set;
    Set<String> training_classifications;
    Set<String> training_vocabulary;

    Map<String, Map<String, Double>> WordProbabilities;

    public MNB_probability(Map<String, Pair<String, Map<String, Integer>>>
        training_set)
    {
        this.training_set = training_set;
        WordProbabilities = new HashMap<String, Map<String, Double>>();
        ClassProbabilities = new HashMap<String, Double>();
        training_vocabulary = new Set<String>();
        training_classifications = new Set<String>();
        for (Map.Entry<String, Pair<String, Map<String, Integer>>>
                Document : training_set.entrySet())
        {
            training_classifications.add(Document.getValue().First());
            for (String w : Document.getValue().Second().keySet())
            {
                training_vocabulary.add(w);
            }
        }
    }

    Set<String> vocabulary()
    {
        return training_vocabulary;
    }

    Set<String> classifications()
    {
        return training_classifications;
    }
    
    public void computeClassProbabilites()
    {
        Map<String,Integer> ClassCounts = new Map<String,Integer>();
        for (Map.Entry<String, Pair<String, Map<String, Integer>>>
                Document : training_set.entrySet())
        {
            String c = Document.getValue().First();
            Utilities.MapIncrementCount(
                    ClassCounts,
                    c);
        }
        for (Map.Entry<String, Integer> ClassCount : ClassCounts)
        {
            ClassProbabilities.add(ClassCount.getValue().doubleValue() /
                    (double) training_set.size());
        }
    }

    public void computeWordProbability()
    {
        for (String c : training_classifications)
        {
            WordProbabilities.put(c,
                    new HashMap<String, Double>());
            for (String w : training_vocabulary)
            {
                tf_wc = 0;
                k
            }
        }
    }

}
