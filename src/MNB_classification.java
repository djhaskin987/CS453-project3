import java.io.File;
import java.io.IOException;
import java.util.*;

public class MNB_classification
{
    Map<String,List<String>> CorpusTokens;

    public MNB_classification(String CorpusDirName, String StopwordsFile)
    {
        CorpusTokens = new HashMap<String,List<String>>();
        StopWords sw = new StopWords(StopwordsFile);
        LoadCorpusTokens(CorpusDirName, sw);
    }

    private void LoadCorpusTokens(String CorpusDirName, StopWords sw)
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
                LoadCorpusTokens(RelativePath,sw);
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
        System.out.println(c.CorpusTokens);
    }
}
