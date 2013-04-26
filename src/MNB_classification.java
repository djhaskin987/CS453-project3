import java.io.File;
import java.io.IOException;
import java.util.*;

public class MNB_classification
{
    Map<String,List<String>> CorpusTokens;

    public MNB_classification(String CorpusDirName)
    {
        CorpusTokens = new HashMap<String,List<String>>();
        LoadCorpusTokens(CorpusDirName);
    }

    private void LoadCorpusTokens(String CorpusDirName)
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
                LoadCorpusTokens(RelativePath);
            }
            else
            {
                String FileName = listOfFiles[i].getName();
                List<String> tokList =
                    Tokenizer.tokens(RelativePath);
                CorpusTokens.put(RelativePath, tokList);
            }
        }
        System.out.println("Done.");
    }

    public static void main(String args[])
    {
        MNB_classification c = new MNB_classification("../test");
        System.out.println("Tokens:");
        System.out.println(c.CorpusTokens);
    }
}
