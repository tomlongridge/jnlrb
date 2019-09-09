package linegenerator.commandline;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PealProverParser {
    
//    public static final Pattern CHANGES_LINE = Pattern.compile("(?:[\\s]+(?:[\\w]{2})?[\\s]?([0-9ET]+))+");
    public static final Pattern CHANGES_LINE = Pattern.compile("(?:\\s+(?:[\\w]{1,2}\\s+)?([0-9ET]+))(?:\\s+(?:[\\w]{1,2}\\s+)?([0-9ET]+))(?:\\s+(?:[\\w]{1,2}\\s+)?([0-9ET]+))(?:\\s+(?:[\\w]{1,2}\\s+)?([0-9ET]+))?");

    public static void main(String[] pr_Arguments) {
        
        Matcher matcher;
        ArrayList<ArrayList<String>> changes = new ArrayList<ArrayList<String>>();
        
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(pr_Arguments[0]));
            String line;
            while ((line = reader.readLine()) != null) 
            {
                matcher = CHANGES_LINE.matcher(line);
                if (matcher.matches())
                {
                    while (changes.size() < matcher.groupCount())
                    {
                        changes.add(new ArrayList<String>());
                    }
                    for (int i = 1; i <= matcher.groupCount(); i++)
                    {
                        if (matcher.group(i) != null)
                        {
                            changes.get(i - 1).add(matcher.group(i));
                        }
                    }
                }
            }
            
            reader.close();
            
            FileWriter writer = new FileWriter(pr_Arguments[0]);
            String lastChange = "";
            for (ArrayList<String> column : changes)
            {
                for (String change : column)
                {
                    if (!change.equals(lastChange)) {
                        writer.write(change);
                        writer.write("\n");
                    }
                    lastChange = change;
                }
            }
            writer.close();
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}
