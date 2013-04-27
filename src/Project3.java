import java.util.*;
import java.io.File;
import java.io.IOException;

public class Project3
{
    private static void LoadCorpusTokens(Map<String, List<String>> CorpusTokens,
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

    private static Map<String, Pair<String, Map<String, Integer>>>
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

            String BaseFileName =
                FileNameSansBase.replaceAll("[^/]*/","");

            Map<String, Integer>
                WordCounts = new HashMap<String,Integer>();

            for (String token : FileTokens.getValue())
            {
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

    public static void main(String [] args)
    {
        StopWords sw = new StopWords("../data/stopwords");
        Map<String,List<String>> CorpusTokens =
            new HashMap<String,List<String>>();
        LoadCorpusTokens(CorpusTokens, "../data/20NG", sw);
        Map<String,Pair<String,Map<String, Integer>>>
            DC = ClassifyTokens(CorpusTokens, "../data/20NG");

        MNB_classification cl;
        MNB_evaluation eval;

        double trainSecondsSum = 0.0;
        double testSecondsSum = 0.0;
        double accSum = 0.0;
        for (int i = 1; i <= 5; i++)
        {
            System.out.println("Run #" + i + ".");
            cl = new MNB_classification(DC);
            eval = new MNB_evaluation(cl);

            long startTime = System.currentTimeMillis();
            cl.train();
            long endTime = System.currentTimeMillis();
            double trainSeconds = (endTime - startTime) / 1000D;
            trainSecondsSum += trainSeconds;

            System.out.println("Training time: " + trainSeconds + " seconds");
            System.out.println("Testing with all features...");

            startTime = System.currentTimeMillis();
            double acc = eval.accuracyMeasure(cl.getTestSet());
            accSum += acc;
            endTime = System.currentTimeMillis();
            double testSeconds = (endTime - startTime) / 1000D;

            System.out.println("Test time: " + testSeconds + " seconds");
            testSecondsSum += testSeconds;

            System.out.println("Accuracy: " + acc);

        }
        System.out.println("Average training time: " +
                (trainSecondsSum / 5.0) + " seconds");
        System.out.println("Average test time: " +
                (testSecondsSum / 5.0) + " seconds");
        System.out.println("Average accuracy: " +
                (accSum / 5.0));
    }

}
