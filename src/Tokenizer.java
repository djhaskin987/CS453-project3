/* import javax.activation.MimetypesFileTypeMap; */
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Tokenizer
{
    private static int shelf = 0;
    private static String NextSentence(Reader reader)
        throws IOException
    {
        int currentChar;
        if (shelf == 0)
        {
            currentChar = reader.read();
        }
        else
        {
            currentChar = shelf;
            shelf = 0;
        }
        if (currentChar < 0)
            return null;

        StringBuilder sentence = new StringBuilder();
        boolean lastCharWasNewline = false;

        while (currentChar >= 0 &&
                currentChar != '!' &&
                currentChar != '?' &&
                currentChar != '.')
        {
            switch (currentChar)
            {
                case '\r':
                    int nextChar = reader.read();
                    if (nextChar == '\n')
                    {
                        nextChar = reader.read();
                    }
                    if (lastCharWasNewline)
                    {
                        shelf = nextChar;
                        return sentence.toString();
                    }
                    else
                    {
                        lastCharWasNewline = true;
                        currentChar = nextChar;
                        continue;
                    }
                case '\n':
                    if (lastCharWasNewline)
                    {
                        return sentence.toString();
                    }
                    else
                    {
                        lastCharWasNewline = true;
                    }
                    break;
                case '\t':
                    if (lastCharWasNewline)

                    {
                        sentence.append(' ');
                    }
                    lastCharWasNewline = false;
                    sentence.append(' ');
                    break;
                default:
                    if (lastCharWasNewline)

                    {
                        sentence.append(' ');
                    }
                    sentence.append((char)currentChar);
                    lastCharWasNewline = false;
                    break;
            }
            currentChar = reader.read();
        }
        while (currentChar >= 0 &&
                (currentChar == '!' ||
                 currentChar == '?' ||
                 currentChar == '.' ||
                 currentChar == '"' ||
                 currentChar == '\'' ||
                 currentChar == ' ' ||
                 currentChar == '\t'))
        {
            if (currentChar == '\t')
            {
                sentence.append(' ');
            }
            else
            {
                sentence.append((char)currentChar);
            }
            currentChar = reader.read();
        }
        if (currentChar == '\r')
        {
            currentChar = reader.read();
        }
        if (currentChar == '\n')
        {
            currentChar = reader.read();
        }
        shelf = currentChar;
        return sentence.toString();
    }

    public static List<String> tokenizeStringCaseSensitive(String line)
    {
        List<String> tokens =
            new LinkedList<String>();
        line = line.replaceFirst("\\p{Punct}+$","");
        line = line.replaceFirst("\\p{Space}+$","");
        line = line.replaceFirst("[^\\p{Graph}]+$","");
        line = line.replaceFirst("^\\p{Punct}+","");
        line = line.replaceFirst("^\\p{Space}+","");
        line = line.replaceFirst("^[^\\p{Graph}]+","");
        String [] lineTokens =
            line.split("([\\p{Punct}-]|[^\\p{Graph}])*[\\p{Space}]+([\\p{Punct}-]|[^\\p{Graph}])*");

        String currentToken;
        for (int i = 0; i < lineTokens.length; i++)
        {
            currentToken = lineTokens[i].trim();
            if (currentToken.length() <= 0)
            {
                continue;
            }
            else
            {
                tokens.add(currentToken);
            }
        }

        return tokens;
    }

    public static List<String> tokenizeString(String line)
    {
        List<String> tokens = tokenizeStringCaseSensitive(line);
        ListIterator<String> tokenStream = tokens.listIterator();
        String CurrentToken;
        while (tokenStream.hasNext())
        {
            CurrentToken = tokenStream.next();
            tokenStream.set(CurrentToken.toLowerCase());
        }
        return tokens;
    }



    public static List<String> tokens(String filename, StopWords stopwords)
    {
        // All lower-cased, stop-worded, tokens.
        List<String> tokens =
            new LinkedList<String>();

        BufferedReader reader = null;

        try
        {
            String currentLine;
            reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(filename),
                        "ISO-8859-1"));
            while ((currentLine = reader.readLine()) != null)
            {
                List<String> LineTokens = tokenizeString(currentLine);
                for (String token : LineTokens)
                {
                    if (!stopwords.contains(token))
                    {
                        tokens.add(token);
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (reader != null)
                    reader.close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        return tokens;
    }

    public static List<String> sentences(String filename)
    {
        shelf  = 0;
        List<String> sentences =
            new LinkedList<String>();
        Reader reader = null;

        try
        {
            reader = new InputStreamReader(
                        new FileInputStream(filename),
                        "ISO-8859-1");
            String sentence = NextSentence(reader);
            while (sentence != null)
            {
                sentences.add(sentence);
                sentence = NextSentence(reader);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (reader != null)
                    reader.close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        return sentences;
    }
}
