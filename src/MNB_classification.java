import java.io.File;
import java.io.IOException;
import java.util.*;

public class MNB_classification
{
    Map<String,Pair<String,Map<String, Integer>>>
        RawDocumentCollection;

    public MNB_classification(String CorpusDirName, String StopwordsFile)
    {
        Map<String,List<String>> CorpusTokens =
            new HashMap<String,List<String>>();
        StopWords sw = new StopWords(StopwordsFile);
        LoadCorpusTokens(CorpusTokens, CorpusDirName, sw);
        // ID -> (Class,(Tokens -> Count))
        RawDocumentCollection = ClassifyTokens(CorpusTokens, CorpusDirName);
    }

    Map<String, Pair<String, Map<String, Integer>>>
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
            new MNB_classification("../test", "../data/stopwords");
        System.out.println("Tokens:");
        System.out.println(c.RawDocumentCollection);
    }
}
