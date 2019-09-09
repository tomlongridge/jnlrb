package linegenerator.core;

import java.io.File;
import java.io.IOException;

import linegenerator.core.exceptions.InvalidPlaceNotationException;
import linegenerator.core.exceptions.MethodGenerationException;

public abstract class AbstractMethodGenerator {
    
    private File m_OutputDir;
    
    public AbstractMethodGenerator(final File pr_OutputDir) {
        m_OutputDir = pr_OutputDir;
    }
    
    public abstract boolean generate(final Method pr_Method, final boolean pr_Overwrite)
        throws IOException, MethodGenerationException, InvalidPlaceNotationException;
    
    protected File getOutputDirectory() {
        if (!m_OutputDir.exists()) {
            m_OutputDir.mkdir();
        }
        return m_OutputDir;
    }
}
