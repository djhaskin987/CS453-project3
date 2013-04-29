import java.util.*;
public class MNB_probability
{
    Map<String, Pair<String, Map<String, Integer>>>
        training_set;
    Set<String> training_classifications;
    Set<String> training_vocabulary;

    Map<String, Map<String, Double>> WordProbabilities;
    Map<String, Double> ClassProbabilities;
    Map<String, Integer> classDenominators;

    public MNB_probability(Map<String, Pair<String, Map<String, Integer>>>
        training_set)
    {
        this.training_set = training_set;
        training_vocabulary = new HashSet<String>();
        training_classifications = new HashSet<String>();
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

    public Set<String> getVocabulary()
    {
        return training_vocabulary;
    }

    public Set<String> getClassifications()
    {
        return training_classifications;
    }

    public void computeClassProbability()
    {
        ClassProbabilities = new HashMap<String, Double>();
        Map<String,Integer> ClassCounts = new HashMap<String,Integer>();
        for (Map.Entry<String, Pair<String, Map<String, Integer>>>
                Document : training_set.entrySet())
        {
            String c = Document.getValue().First();
            Utilities.MapIncrementCount(
                    ClassCounts,
                    c);
        }
        for (Map.Entry<String, Integer> ClassCount : ClassCounts.entrySet())
        {
            ClassProbabilities.put(ClassCount.getKey(),
                    ClassCount.getValue().doubleValue() /
                    (double) training_set.size());
        }
    }

    public void computeWordProbability()
    {
        WordProbabilities = new HashMap<String, Map<String, Double>>();
        classDenominators =
            wordProbabilityDenominators();

        Map<String, Map<String, Integer>> tf_wc =
            termFrequencyWordGivenClass();

        for (String c : training_classifications)
        {
            HashMap<String,Double> WordProbabilityGivenClass =
                new HashMap<String,Double>();
            Double denominator =
                classDenominators.get(c).doubleValue();
            if (denominator == null)
            {
                continue;
            }
            Map<String,Integer>
                ClassTermFrequencies =
                tf_wc.get(c);
            for (String w : training_vocabulary)
            {
                Integer wordCount = ClassTermFrequencies.get(w);
                if (wordCount != null)
                {
                    WordProbabilityGivenClass.put(
                            w,
                            (wordCount + 1.0) /
                            denominator.doubleValue());
                }
            }
            WordProbabilities.put(c, WordProbabilityGivenClass);
        }
    }

    public Map<String, Map<String, Integer>> termFrequencyWordGivenClass()
    {
        Map<String, Map<String, Integer>> tf_wc =
            new HashMap<String, Map<String, Integer>>();
        for (Map.Entry<String, Pair<String, Map<String, Integer>>>
                Document : training_set.entrySet())
        {
            Map<String, Integer> termCountGivenClass =
                tf_wc.get(Document.getValue().First());
            if (termCountGivenClass == null)
            {
                termCountGivenClass = new HashMap<String,Integer>();
            }
            for (Map.Entry<String, Integer> TermCount :
                    Document.getValue().Second().entrySet())
            {
                Utilities.MapAddCount(termCountGivenClass,
                       TermCount.getKey(),
                    TermCount.getValue());
            }

            tf_wc.remove(Document.getValue().First());
            tf_wc.put(Document.getValue().First(),
                    termCountGivenClass);
        }
        return tf_wc;
    }

    public Map<String,Integer> wordProbabilityDenominators()
    {
        Map<String,Integer> classTermCount = new HashMap<String,Integer>();

        // compute |c|
        for (Map.Entry<String, Pair<String, Map<String, Integer>>>
                Document : training_set.entrySet())
        {
            for (Map.Entry<String, Integer> TermCount :
                    Document.getValue().Second().entrySet())
            {
                Utilities.MapAddCount(classTermCount,
                        Document.getValue().First(),
                    TermCount.getValue());
            }
        }

        // Add |V|
        for (String c : training_vocabulary)
        {
            Utilities.MapAddCount(
                    classTermCount,
                    c,
                    training_vocabulary.size());
        }
        return classTermCount;
    }

    public double getWordProbability(String word, String c)
    {
        Map<String,Double> WordProbabilityGivenClass =
            WordProbabilities.get(c);
        Double prob = WordProbabilityGivenClass.get(word);
        if (prob == null)
        {
            return 1.0 / classDenominators.get(c).doubleValue();
        }
        else
        {
            return prob.doubleValue();
        }
    }
    public double getClassProbability(String c)
    {
        return ClassProbabilities.get(c).doubleValue();
    }
}
