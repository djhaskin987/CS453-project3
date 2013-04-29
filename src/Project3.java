import java.util.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class Project3
{
    private static long LoadDocumentCollection(
            Map<String,Pair<String,Map<String, Integer>>> DC,
            String CorpusDirName,
            String CorpusBaseDirName,
            long numDocs,
            StopWords sw)
    {
        File corpusDir = new File(CorpusDirName);
        File[] listOfFiles = corpusDir.listFiles();
        for (int i = 0; i < listOfFiles.length; i++)
        {
            String RelativePath = CorpusDirName + "/" +
                listOfFiles[i].getName();

            if (listOfFiles[i].isDirectory())
            {
                numDocs = LoadDocumentCollection(DC,RelativePath,
                        CorpusBaseDirName,numDocs,sw);
            }
            else
            {
                String FileName = CorpusDirName + "/" +
                    listOfFiles[i].getName();
                List<String> tokList =
                    Tokenizer.tokens(RelativePath, sw);
                String FileNameSansBase =
                    FileName.replaceFirst("^" +
                            (CorpusBaseDirName.replaceAll("[.]","[.]")),"");
                String RawClass =
                    FileNameSansBase.replaceFirst("/[^/]*$","");
                String Class =
                    RawClass.replaceAll("/+",".").replaceFirst("^[.]+","");

                String BaseFileName =
                    FileNameSansBase.replaceAll("[^/]*/","");

                Map<String, Integer>
                    WordCounts = new HashMap<String,Integer>();

                for (String token : tokList)
                {
                    Utilities.MapIncrementCount(WordCounts, token);
                }

                Pair<String,
                    Map<String, Integer>> DocumentStats =
                        new Pair<String, Map<String, Integer>>(
                                Class, WordCounts);
                DC.put(BaseFileName, DocumentStats);
                numDocs++;
                if (numDocs % 200 == 0)
                {
                    System.out.print("Loaded " + numDocs +
                                " Documents.\r");
                }
            }
        }
        System.out.println("                                \rDone.");
        return numDocs;
    }

    public static void report(Map<String,Pair<String,Map<String,Integer>>>
            DC, int numFeatures)
    {
        DecimalFormat percent = new DecimalFormat("##.##%");

        MNB_classification cl;
        MNB_evaluation eval;

        double trainSecondsSum = 0.0;
        double testSecondsSum = 0.0;
        double accSum = 0.0;
        if (numFeatures > 0)
        {
            System.out.println("Reporting results for ***" + numFeatures +
                    " FEATURES***");
        }
        else
        {
            System.out.println("Reporting results for using ***ALL FEATURES***");
        }
        for (int i = 1; i <= 5; i++)
        {
            System.out.println("Run #" + i + " ==============================");
            System.out.println("Initializing...");
            cl = new MNB_classification(DC);
            if (numFeatures > 0)
            {
                cl.featureSelection(numFeatures);
            }
            eval = new MNB_evaluation(cl);

            System.out.println("Done initializing.");
            long startTime = System.currentTimeMillis();
            System.out.println("Training...");
            cl.train();
            System.out.println("Done training.");
            long endTime = System.currentTimeMillis();
            long trainDuration = (endTime - startTime);
            double trainSeconds = (double)trainDuration / 1000D;
            trainSecondsSum += trainSeconds;

            System.out.println("Training time: " +
                String.format("%d:%02d:%02d.%03d",
                    trainDuration / 3600000,
                    trainDuration / 60000 % 60,
                    trainDuration / 1000 % 60,
                    trainDuration % 1000));
            System.out.println();
            System.out.println("Testing with all features...");

            System.out.println("Determining Accuracy...");
            startTime = System.currentTimeMillis();
            double acc = eval.accuracyMeasure(cl.getTestSet());
            endTime = System.currentTimeMillis();
            long evalDuration = (endTime - startTime);
            accSum += acc;
            System.out.println("Done measuring Accuracy.");
            double testSeconds = (double)evalDuration / 1000D;

            System.out.println("Test time: " +
                String.format("%d:%02d:%02d.%03d",
                    evalDuration / 3600000,
                    evalDuration / 60000 % 60,
                    evalDuration / 1000 % 60,
                    evalDuration % 1000));
            testSecondsSum += testSeconds;

            System.out.println("Accuracy: " + percent.format(acc));

        }
        long trainDurationAverage = (long)((trainSecondsSum / 5.0) * 1000.0);
        long testDurationAverage = (long)((testSecondsSum / 5.0) * 1000.0);
        System.out.println("=====================================");

        System.out.println("Average training time: " +
                String.format("%d:%02d:%02d.%03d",
                    trainDurationAverage / 3600000,
                    trainDurationAverage / 60000 % 60,
                    trainDurationAverage / 1000 % 60,
                    trainDurationAverage % 1000));
        System.out.println("Average test time: " +
                String.format("%d:%02d:%02d.%03d",
                    testDurationAverage / 3600000,
                    testDurationAverage / 60000 % 60,
                    testDurationAverage / 1000 % 60,
                    testDurationAverage % 1000));
        System.out.println("Average accuracy: " +
                percent.format(accSum / 5.0));
    }

    public static void main(String [] args)
    {
        String corpusLocation;
        if (args.length == 0)
        {
            corpusLocation = "../data/20NG";
        }
        else
        {
            corpusLocation = args[0];
        }
        StopWords sw = new StopWords("../data/stopwords");
        Map<String,List<String>> CorpusTokens =
            new HashMap<String,List<String>>();
        System.out.println("Tokenizing corpus found under " + corpusLocation +
                "...");
        Map<String,Pair<String,Map<String, Integer>>>
            DC = new HashMap<String,Pair<String,Map<String,Integer>>>();
        LoadDocumentCollection(DC,corpusLocation,corpusLocation, 0L, sw);
        report(DC,0);
        report(DC,6200);
        report(DC,12400);
        report(DC,18600);
        report(DC,24800);
    }
}
