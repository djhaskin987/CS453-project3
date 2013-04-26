import java.io.File;
import java.io.IOException;
import java.util.*;

public class MNB_classification
{
    public static double TRAINING_RATIO = .8;

    Map<String,Pair<String,Map<String, Integer>>>
        DC;
    Map<String,Pair<String,Map<String, Integer>>>
        DC_test;
    Map<String,Pair<String,Map<String, Integer>>>
        DC_training;
    Map<String,Pair<String,Map<String, Integer>>>
        DC_test;
    Map<String,Pair<String,Map<String, Integer>>>
        DC_training;
    Set<String> Classifications;
    Set<String> Vocabulary;

    public MNB_classification(String CorpusDirName, String StopwordsFile)
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
        int LeftTrainingToPick = (int)(TRAINING_RATIO * denominator);

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
                        Doc.getValue());
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
            new MNB_classification("../test/s9test", "../data/stopwords");
        System.out.println("Tokens:");
        System.out.println(c.DC);
        System.out.println("TRAINING");
        System.out.println(c.DC_training);
        System.out.println("TEST");
        System.out.println(c.DC_test);
        System.out.println("CLASSES");
        System.out.println(c.Classifications);
    }
}
