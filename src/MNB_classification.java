import java.util.*;

public class MNB_classification
{
    public static double TRAINING_RATIO = .8;
    MNB_probability odds;
    Map<String,Pair<String,Map<String, Integer>>>
        DC_test;
    Map<String,Pair<String,Map<String, Integer>>>
        DC_training;
    Map<String,Pair<String,Map<String, Integer>>>
        test_set;
    Map<String,Pair<String,Map<String, Integer>>>
        training_set;

    public MNB_classification(Map<String,Pair<String,Map<String, Integer>>>
        DC)
    {
        // ID -> (Class,(Tokens -> Count))
        DC_test =
            new HashMap<String,Pair<String,Map<String,Integer>>>();
        DC_training =
            new HashMap<String,Pair<String,Map<String,Integer>>>();
        LoadTrainingAndTest(DC,
                DC_training,
                DC_test);

        training_set = DC_training;
        test_set = DC_test;
    }

    public Map<String,Pair<String,Map<String, Integer>>>
        getTestSet()
    {
        return test_set;
    }

    public Map<String,Pair<String,Map<String, Integer>>>
        getTrainingSet()
    {
        return training_set;
    }

    public void train()
    {
        odds = new MNB_probability(training_set);
        odds.computeWordProbability();
        odds.computeClassProbability();
    }

    public Set<String> featureSelection(int M)
    {
        Map<String, Integer> DC_ClassCounts = new HashMap<String,Integer>();
        Map<String, Integer> DC_WordCounts = new HashMap<String,Integer>();
        Set<String> training_classifications = new HashSet<String>();
        Set<String> training_vocabulary = new HashSet<String>();

        // word -> (class -> count)
        Map<String,
            Map<String, Integer>> DC_ClassWordCounts =
                new HashMap<String, Map<String, Integer>>();

        for (Map.Entry<String,Pair<String,Map<String,Integer>>>
                entry : DC_training.entrySet())
        {
            String ObservedClass = entry.getValue().First();
            Utilities.MapIncrementCount(DC_ClassCounts,
                    ObservedClass);
            Set<String> ObservedWords = entry.getValue().Second().keySet();
            training_classifications.add(ObservedClass);
            training_vocabulary.addAll(ObservedWords);
            Map<String,Integer> WordCounts =
                DC_ClassWordCounts.get(ObservedClass);
            if (WordCounts == null)
            {
                WordCounts = new HashMap<String,Integer>();
            }

            for (String word : ObservedWords)
            {
                Utilities.MapIncrementCount(DC_WordCounts,word);
                Utilities.MapIncrementCount(WordCounts,word);
            }
            DC_ClassWordCounts.remove(ObservedClass);
            DC_ClassWordCounts.put(ObservedClass,WordCounts);
        }

        Map<String, Double> P_w = new HashMap<String,Double>();
        for (String word : DC_WordCounts.keySet())
        {
            Integer Count = DC_WordCounts.get(word);
            P_w.put(word, Count.doubleValue() /
                (double) DC_training.size());
        }

        Map<String, Double> P_c = new HashMap<String,Double>();
        for (String c : DC_ClassCounts.keySet())
        {
            Integer Count = DC_ClassCounts.get(c);
            P_c.put(c, Count.doubleValue() /
                (double) DC_training.size());
        }

        Map<String, Map<String, Double>>
            P_c_given_w =
            Utilities.<String,String,Double>MapMapInit(training_vocabulary);
        Map<String, Map<String, Double>>
            P_c_given_not_w =
            Utilities.<String,String,Double>MapMapInit(training_vocabulary);

        for (String word : training_vocabulary)
        {
            Map<String, Double> ClassProbsGivenWord =
                P_c_given_w.get(word);
            Map<String, Double> ClassProbsGivenNotWord =
                P_c_given_not_w.get(word);
            Integer TotalWordCount = DC_WordCounts.get(word);
            for (String c : training_classifications)
            {
                Map<String, Integer>
                    WordCounts = DC_ClassWordCounts.get(c);

                Integer WordCount = WordCounts.get(word);
                if (WordCount == null)
                {
                    ClassProbsGivenWord.put(c,0.0);
                    ClassProbsGivenNotWord.put(c,
                            DC_ClassCounts.get(c).doubleValue() /
                            (DC_training.size() - TotalWordCount.intValue()));
                }
                else
                {
                    ClassProbsGivenWord.put(c,WordCount.doubleValue() /
                            TotalWordCount.doubleValue());
                    ClassProbsGivenNotWord.put(c,
                            (double)(DC_ClassCounts.get(c).intValue() -
                             WordCount.intValue()) /
                            (DC_training.size() - TotalWordCount.intValue()));
                }
            }
            P_c_given_w.remove(word);
            P_c_given_w.put(word,ClassProbsGivenWord);
            P_c_given_not_w.remove(word);
            P_c_given_not_w.put(word,ClassProbsGivenNotWord);
        }

        // NOW, compute information gain
        List<Pair<Double, String>> WordInformationGain =
            new ArrayList<Pair<Double, String>>();
        for (String word : training_vocabulary)
        {
            double IGw = 0.0;
            double log_coef = Math.log(2.0);
            double CurrentPw = P_w.get(word).doubleValue();
            double CurrentPnotw = 1.0 - CurrentPw;

            Map<String, Double> ClassProbGivenWord =
                P_c_given_w.get(word);
            Map<String, Double> ClassProbGivenNotWord =
                P_c_given_not_w.get(word);

            for (String c : training_classifications)
            {
                IGw -= P_c.get(c).doubleValue() *
                    Math.log(P_c.get(c).doubleValue()) / log_coef;
                IGw += CurrentPw * ClassProbGivenWord.get(c).doubleValue() *
                    Math.log(ClassProbGivenWord.get(c).doubleValue()) /
                    log_coef;
                IGw += CurrentPnotw *
                    ClassProbGivenNotWord.get(c).doubleValue() *
                    Math.log(ClassProbGivenNotWord.get(c).doubleValue()) /
                    log_coef;
            }
            WordInformationGain.add(new Pair<Double,String>(IGw,word));
        }
        java.util.Collections.sort(WordInformationGain,
                new ReverseComparator<Pair<Double,String>>());
        Set<String> selectedFeatures = new HashSet<String>();
        for (int i = 0; i < M; i++)
        {
            selectedFeatures.add(WordInformationGain.get(i).Second());
        }

        training_set = new HashMap<String,
                     Pair<String, Map<String, Integer>>>();
        test_set = new HashMap<String,
                     Pair<String, Map<String, Integer>>>();
        NarrowFeatureDocSet(DC_training, training_set, selectedFeatures);
        NarrowFeatureDocSet(DC_test, test_set, selectedFeatures);

        return selectedFeatures;
    }

    private void NarrowFeatureDocSet(
            Map<String,Pair<String,Map<String, Integer>>> raw_set,
            Map<String,Pair<String,Map<String, Integer>>> set,
            Set<String> selectedFeatures)
    {
        for (Map.Entry<String,Pair<String,Map<String, Integer>>>
                Doc : raw_set.entrySet())
        {
            Pair<String, Map<String, Integer>> DocPair = Doc.getValue();
            Map<String, Integer> DocFeatures = DocPair.Second();
            Map<String, Integer> newFeatures = new HashMap<String, Integer>();
            for (String feature : selectedFeatures)
            {
                if (DocFeatures.containsKey(feature))
                {
                    newFeatures.put(feature, DocFeatures.get(feature));
                }
            }
            set.put(Doc.getKey(),
                    new Pair<String, Map<String, Integer>> (
                        DocPair.First(),
                        newFeatures));
        }
    }

    private void LoadTrainingAndTest(
        Map<String,Pair<String,Map<String, Integer>>>
            DC,
        Map<String,Pair<String,Map<String, Integer>>>
            DC_training,
        Map<String,Pair<String,Map<String, Integer>>>
            DC_test)
    {
        Random rng = new Random();
        double dice;

        // Choose random K algorithm --
        // Note how many documents I will sift through to create the training
        // set.
        int RestOfTheDocuments = DC.size();

        // Find out how many documents I need in the training set ("K").
        int LeftTrainingToPick = (int)(TRAINING_RATIO * RestOfTheDocuments);

        // Guard against divide by zero
        if (RestOfTheDocuments == 0)
            return;

        for (Map.Entry<String,Pair<String,Map<String,Integer>>>
                Doc : DC.entrySet())
        {
            double ratio = ((double)LeftTrainingToPick) /
                ((double)RestOfTheDocuments);
            dice = rng.nextFloat();
            if (dice < ratio)
            // We pick a document for the training set in this case.
            // decrease the number of documents we still need to find (numerator)
            // and decrease the number of documents we still need to sift
            // through (denominator).
            {
                DC_training.put(
                        Doc.getKey(),
                        Doc.getValue());
                LeftTrainingToPick--;
                RestOfTheDocuments--;
            }
            else
            {
            // We pick a document for NOT the training set (the test set)
            // decrease the number of documents we still need to sift through
            // to find the correct amount of training set documents.
                RestOfTheDocuments--;
                DC_test.put(
                        Doc.getKey(),
                        new Pair<String,Map<String,Integer>>(
                            Doc.getValue().First(),
                            Doc.getValue().Second()));
            }
        }

    }

    // document -> classification
    public String label(Map<String, Integer> document)
    {
        List<Pair<Double,String>> PdcPc =
            new LinkedList<Pair<Double,String>>();

        for (String c : odds.getClassifications())
        {
            double Pc = odds.getClassProbability(c);

            double P_d_given_c_reduce = 1.0;
            for (String word : document.keySet())
            {
                double P_w_given_c = odds.getWordProbability(word,c);
                Integer retrieve = document.get(word);
                if (retrieve == null || retrieve.intValue() == 0)
                    continue;
                P_d_given_c_reduce *= Math.pow(P_w_given_c,retrieve.doubleValue());
            }
            double P_d_given_c = P_d_given_c_reduce;
            PdcPc.add(new Pair<Double,String>(P_d_given_c*Pc,c));
        }
        double PdcPc_sum = 0.0;
        for (Pair<Double,String> d : PdcPc)
        {
            PdcPc_sum += d.First().doubleValue();
        }
        List<Pair<Double,String>> Pdc =
            new LinkedList<Pair<Double,String>>();

        for (Pair<Double,String> n : PdcPc)
        {
            Pdc.add(new Pair<Double,String>(
                        n.First().doubleValue() / PdcPc_sum,
                        n.Second()));
        }
        return Collections.max(Pdc).Second();
    }
}
