import java.io.File;
import java.io.IOException;
import java.util.*;

public class MNB_classification
{
    public static double TRAINING_RATIO = .8;

    MNB_probability odds;

    Map<String,Pair<String,Map<String, Integer>>>
        DC;
    Map<String,Pair<String,Map<String, Integer>>>
        DC_test;
    Map<String,Pair<String,Map<String, Integer>>>
        DC_training;
    Map<String,Pair<String,Map<String, Integer>>>
        test_set;
    Map<String,Pair<String,Map<String, Integer>>>
        training_set;
    Set<String> Classifications;
    Set<String> Vocabulary;

    public MNB_classification(String CorpusDirName, String StopwordsFile,
            boolean FeatureSelectionApplied, int M)
    {
        Map<String,List<String>> CorpusTokens =
            new HashMap<String,List<String>>();
        Classifications = new HashSet<String>();
        Vocabulary = new HashSet<String>();
        StopWords sw = new StopWords(StopwordsFile);
        LoadCorpusTokens(CorpusTokens, CorpusDirName, sw);
        // ID -> (Class,(Tokens -> Count))
        DC = ClassifyTokens(CorpusTokens, CorpusDirName);
        DC_test =
            new HashMap<String,Pair<String,Map<String,Integer>>>();
        DC_training =
            new HashMap<String,Pair<String,Map<String,Integer>>>();
        LoadTrainingAndTest(DC,
                DC_training,
                DC_test);

        if (FeatureSelectionApplied)
        {
            Set<String> selectedFeatures = featureSelection(M);
            training_set = new HashMap<String,
                         Pair<String, Map<String, Integer>>>();
            test_set = new HashMap<String,
                         Pair<String, Map<String, Integer>>>();
            NarrowFeatureDocSet(DC_training, training_set, selectedFeatures);
            NarrowFeatureDocSet(DC_test, test_set, selectedFeatures);
        }
        else
        {
            training_set = DC_training;
            test_set = DC_test;
        }
        odds = new MNB_probability(training_set);
        odds.computeWordProbability();
        odds.computeClassProbability();

    }


    // document -> classification
    public String label(Map<String, Integer> document)
    {
        List<Double> PdcPc = new LinkedList<Double>();

        for (String c : Classifications)
        {
            double Pc = odds.getClassProbability(c);

            double P_d_given_c_reduce = 1.0;
            for (String word : Vocabulary)
            {
                double P_w_given_c = odds.getWordProbability(word,c);
                Integer retrieve = document.get(word);
                double tf_wd = 0.0;
                if (retrieve != null)
                {
                    tf_wd = retrieve.doubleValue();
                }
                P_d_given_c_reduce *= Math.pow(P_w_given_c,tf_wd);
            }
            double P_d_given_c = P_d_given_c_reduce;
            PdcPc.add(P_d_given_c*Pc);
        }
        double PdcPc_sum = 0.0;
        for (Double d : PdcPc)
        {
            PdcPc_sum += d.doubleValue();
        }
        List<Double> Pcd = new LinkedList<Double>();
        for (Double n : PdcPc)
        {
            Pcd.add(n / PdcPc_sum);
        }
        return Collections.max(Pcd);
    }

    private Set<String> featureSelection(
            int M)
    {
        // Get P(c)
        Map<String, Integer> DC_ClassCounts = new HashMap<String,Integer>();
        Map<String, Integer> DC_WordCounts = new HashMap<String,Integer>();
        Map<String, Integer> DC_NotWordCounts = new HashMap<String,Integer>();

        // word -> (class -> count)
        Map<String,
            Map<String, Integer>> DC_WordClassCounts =
                Utilities.<String,String,Integer>MapMapInit(Vocabulary);
        Map<String,
            Map<String, Integer>> DC_NotWordClassCounts =
                Utilities.<String,String,Integer>MapMapInit(Vocabulary);

        for (String w : Vocabulary)
        {
            DC_WordClassCounts.put(w, new HashMap<String, Integer>());
            DC_NotWordClassCounts.put(w, new HashMap<String, Integer>());
        }

        for (Map.Entry<String,Pair<String,Map<String,Integer>>>
                entry : DC_training.entrySet())
        {
            String ObservedClass = entry.getValue().First();
            Set<String> ObservedWords = entry.getValue().Second().keySet();
            for (String word : Vocabulary)
            {
                Map<String, Integer> ClassCountsGivenWord =
                    DC_WordClassCounts.get(word);
                Map<String, Integer> ClassCountsGivenNotWord =
                    DC_NotWordClassCounts.get(word);
                if (ObservedWords.contains(word))
                {
                    Utilities.MapIncrementCount(
                            DC_WordCounts,
                            word);
                    Utilities.MapIncrementCount(
                            ClassCountsGivenWord,
                            ObservedClass);
                }
                else
                {
                    Utilities.MapIncrementCount(
                            DC_NotWordCounts,
                            word);
                    Utilities.MapIncrementCount(
                            ClassCountsGivenNotWord,
                            ObservedClass);
                }
                DC_WordClassCounts.remove(word);
                DC_NotWordClassCounts.remove(word);
                DC_WordClassCounts.put(word, ClassCountsGivenWord);
                DC_NotWordClassCounts.put(word, ClassCountsGivenNotWord);

            }
            Utilities.MapIncrementCount(DC_ClassCounts,
                    ObservedClass);
        }

        Map<String, Double> P_c = new HashMap<String,Double>();
        for (String c : Classifications)
        {
            Integer Count = DC_ClassCounts.get(c);
            if (Count == null)
            {
                Count = new Integer(0);
                DC_ClassCounts.put(c, Count);
            }
            P_c.put(c, Count.doubleValue() /
                (double) DC_training.size());
        }

        Map<String, Double> P_w = new HashMap<String,Double>();
        for (String word : Vocabulary)
        {
            Integer Count = DC_WordCounts.get(word);
            if (Count == null)
            {
                Count = new Integer(0);
                DC_WordCounts.put(word, Count);
            }
            P_w.put(word, Count.doubleValue() /
                (double) DC_training.size());
        }

        Map<String, Double> P_not_w = new HashMap<String,Double>();
        for (String word : Vocabulary)
        {
            Integer Count = DC_NotWordCounts.get(word);
            if (Count == null)
            {
                Count = new Integer(0);
                DC_NotWordCounts.put(word, Count);
            }
            P_not_w.put(word, Count.doubleValue() /
                (double) DC_training.size());
        }

        Map<String, Map<String, Double>>
            P_c_given_w =
            Utilities.<String,String,Double>MapMapInit(Vocabulary);
        Map<String, Map<String, Double>>
            P_c_given_not_w =
            Utilities.<String,String,Double>MapMapInit(Vocabulary);

        for (String word : Vocabulary)
        {
            Map<String, Double> ClassProbsGivenWord =
                new HashMap<String,Double>();
            Map<String, Double> ClassProbsGivenNotWord =
                new HashMap<String,Double>();

            Map<String, Integer>
                WordClassCounts = DC_WordClassCounts.get(word);
            Map<String, Integer>
                NotWordClassCounts = DC_NotWordClassCounts.get(word);
            if (WordClassCounts == null)
            {
                WordClassCounts = new HashMap<String,Integer>();
            }
            if (NotWordClassCounts == null)
            {
                NotWordClassCounts = new HashMap<String,Integer>();
            }
            Integer WordDocCount =
                DC_WordCounts.get(word);
            Integer NotWordDocCount =
                DC_NotWordCounts.get(word);

            for (String c : Classifications)
            {
                Integer ClassCountGivenWord = WordClassCounts.get(c);
                if (ClassCountGivenWord == null)
                {
                    ClassCountGivenWord = new Integer(0);
                    WordClassCounts.put(c,ClassCountGivenWord);
                }
                ClassProbsGivenWord.put(
                        c,
                        ClassCountGivenWord.doubleValue() /
                        WordDocCount.doubleValue());

                Integer ClassCountGivenNotWord = NotWordClassCounts.get(c);
                if (ClassCountGivenNotWord == null)
                {
                    ClassCountGivenNotWord = new Integer(0);
                    NotWordClassCounts.put(c,ClassCountGivenNotWord);
                }
                ClassProbsGivenNotWord.put(
                        c,
                        ClassCountGivenNotWord.doubleValue() /
                        NotWordDocCount.doubleValue());
            }

            P_c_given_w.put(word, ClassProbsGivenWord);
            P_c_given_not_w.put(word, ClassProbsGivenNotWord);
        }

        System.out.println("P(c): " + P_c);
        System.out.println("P(w): " + P_w);
        System.out.println("P(~w): " + P_not_w);
        System.out.println("P(c|w): " + P_c_given_w);
        System.out.println("P(c|~w): " + P_c_given_not_w);

        // NOW, compute information gain
        List<Pair<Double, String>> WordInformationGain =
            new ArrayList<Pair<Double, String>>();
        for (String word : Vocabulary)
        {
            double IGw = 0.0;
            double log_coef = Math.log(2.0);
            double CurrentPw = P_w.get(word).doubleValue();
            double CurrentPnotw = P_not_w.get(word).doubleValue();

            Map<String, Double> ClassProbGivenWord =
                P_c_given_w.get(word);
            Map<String, Double> ClassProbGivenNotWord =
                P_c_given_not_w.get(word);

            for (String c : Classifications)
            {
                System.out.println("IGw first: " + IGw);
                IGw -= P_c.get(c).doubleValue() *
                    Math.log(P_c.get(c).doubleValue()) / log_coef;
                System.out.println("IGw next: " + IGw);
                IGw += CurrentPw * ClassProbGivenWord.get(c).doubleValue() *
                    Math.log(ClassProbGivenWord.get(c).doubleValue()) /
                    log_coef;
                System.out.println("IGw again: " + IGw);
                IGw += CurrentPnotw *
                    ClassProbGivenNotWord.get(c).doubleValue() *
                    Math.log(ClassProbGivenNotWord.get(c).doubleValue()) /
                    log_coef;
                System.out.println("IGw finally: " + IGw);
            }
            WordInformationGain.add(new Pair<Double,String>(IGw,word));
        }
        System.out.println("RANKS:" +
                WordInformationGain);
        java.util.Collections.sort(WordInformationGain,
                new ReverseComparator<Pair<Double,String>>());
        Set<String> returned = new HashSet<String>();
        for (int i = 0; i < M; i++)
        {
            returned.add(WordInformationGain.get(i).Second());
        }
        System.out.println("Narrowed features down to: " +
                returned);
        return returned;
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
                            "", // blank out the class
                            Doc.getValue().Second()));
            }
        }

    }

    private Map<String, Pair<String, Map<String, Integer>>>
        ClassifyTokens(Map<String, List<String>> CorpusTokens,
                String CorpusDirName)
    {
        // ID -> (Class,(Tokens -> Count))
        Map<String, Pair<String, Map<String, Integer>>> returned =
            new HashMap<String,Pair<String,Map<String,Integer>>>();
        for (Map.Entry<String, List<String>> FileTokens :
                CorpusTokens.entrySet())
        {
            String FileName = FileTokens.getKey();
            String FileNameSansBase =
                FileName.substring(CorpusDirName.length() + 1);

            String RawClass =
                FileNameSansBase.replaceFirst("/[^/]*$","");
            String Class =
                RawClass.replace('/','.');
            Classifications.add(Class);

            String BaseFileName =
                FileNameSansBase.replaceAll("[^/]*/","");

            Map<String, Integer>
                WordCounts = new HashMap<String,Integer>();

            for (String token : FileTokens.getValue())
            {
                Vocabulary.add(token);
                Integer count = WordCounts.get(token);
                if (count == null)
                {
                    count = new Integer(1);
                    WordCounts.put(token, count);
                }
                else
                {
                    WordCounts.remove(token);
                    WordCounts.put(token, new Integer(
                                count.intValue() + 1));
                }
            }

            Pair<String,
                Map<String, Integer>> DocumentStats =
                    new Pair<String, Map<String, Integer>>(
                            Class,
                            WordCounts
                            );
            returned.put(
                    BaseFileName,
                    DocumentStats);
        }
        return returned;
    }

    private void LoadCorpusTokens(Map<String, List<String>> CorpusTokens,
            String CorpusDirName, StopWords sw)
    {
        System.out.println("Tokenizing corpus found under " + CorpusDirName +
                "...");
        File corpusDir = new File(CorpusDirName);
        File[] listOfFiles = corpusDir.listFiles();
        for (int i = 0; i < listOfFiles.length; i++)
        {
            String RelativePath = CorpusDirName + "/" +
                listOfFiles[i].getName();

            if (listOfFiles[i].isDirectory())
            {
                LoadCorpusTokens(CorpusTokens,RelativePath,sw);
            }
            else
            {
                String FileName = listOfFiles[i].getName();
                List<String> tokList =
                    Tokenizer.tokens(RelativePath, sw);
                CorpusTokens.put(RelativePath, tokList);
            }
        }
        System.out.println("Done.");
    }

    public static void main(String args[])
    {
        MNB_classification c =
            new MNB_classification("../test/s9test", "../data/stopwords",
                    false,
                    2);
        System.out.println("Tokens:");
        System.out.println(c.DC);
        System.out.println("RAW TRAINING");
        System.out.println(c.DC_training);
        System.out.println("RAW TEST");
        System.out.println(c.DC_test);
        System.out.println("TRAINING");
        System.out.println(c.training_set);
        System.out.println("TEST");
        System.out.println(c.test_set);
        System.out.println("CLASSES");
        System.out.println(c.Classifications);
        System.out.println("VOCAB");
        System.out.println(c.Vocabulary);
    }
}
