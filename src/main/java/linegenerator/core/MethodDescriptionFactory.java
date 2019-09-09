package linegenerator.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MethodDescriptionFactory extends AbstractMethodGenerator {
    
    public MethodDescriptionFactory(final File pr_OutputDir) {
        super(pr_OutputDir);
    }
    
    public boolean generate(final Method pr_Method, final boolean pr_Overwrite) throws IOException
    {
        final File outputFile = new File(getOutputDirectory().getAbsolutePath() + 
                                         File.separator + 
                                         pr_Method.getFileIdentifier() +
                                         ".desc" +
                                         ".txt");
        if (!pr_Overwrite && outputFile.exists()) {
            return false;
        }

        final FileWriter writer = new FileWriter(outputFile, false);
        writer.write(pr_Method.toString().toUpperCase());
        writer.write("\n");
        writer.write(pr_Method.getDescription());
        writer.write("\n");
        writer.close();
        
        return true;
    }
    
}
