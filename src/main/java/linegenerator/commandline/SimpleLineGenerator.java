package linegenerator.commandline;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import linegenerator.core.AbstractComposition;
import linegenerator.core.AbstractCompositionVisitor;
import linegenerator.core.AbstractMethodGenerator;
import linegenerator.core.CompositionReader;
import linegenerator.core.CompositionWriter;
import linegenerator.core.IMethodLibraryReaderListener;
import linegenerator.core.LineFactory;
import linegenerator.core.Method;
import linegenerator.core.MethodDescriptionFactory;
import linegenerator.core.MethodGridFactory;
import linegenerator.core.MethodLibraryReader;
import linegenerator.core.NeatCompositionWriter;
import linegenerator.core.TextMethodLibraryReader;
import linegenerator.core.exceptions.CompositionDefinitionException;
import linegenerator.core.exceptions.FalseGridException;
import linegenerator.core.exceptions.InvalidPlaceNotationException;
import linegenerator.core.exceptions.MethodDefinitionException;
import linegenerator.core.exceptions.MethodGenerationException;

public class SimpleLineGenerator implements IMethodLibraryReaderListener {

    private static final String ARGUMENT_METHODS = "methods";
    private static final String ARGUMENT_COMPOSITIONS = "compositions";
    private static final String ARGUMENT_OUTPUT_DIRECTORY = "outputdirectory";
    private static final String ARGUMENT_GENERATE_LINES = "generatelines";
    private static final String ARGUMENT_LINES_PROPERTIES_FILE = "lineproperties";
    private static final String ARGUMENT_GENERATE_DESCRIPTIONS = "generatedescriptions";
    private static final String ARGUMENT_GENERATE_BOOK_TEXT = "generatebooktext";
    private static final String ARGUMENT_GENERATE_GRID = "generategrid";
    private static final String ARGUMENT_OVERWRITE = "overwrite";
    private static final String ARGUMENT_VERBOSE = "verbose";
    
    private static final Logger s_Logger = Logger.getLogger("SimpleLineGenerator");
    
    private File m_OutputDirectory;
    private boolean m_Overwrite;
    private Method[] m_MethodLibrary;
	private String m_LinePropertiesFile;

    /**
     * @param pr_Arguments
     */
    public static void main(String[] pr_Arguments) {
        
        System.out.println("====================================");
        System.out.println("=          LINE GENERATOR          =");
        System.out.println("====================================");
        System.out.println("= Version 1.0.7");
        System.out.println("====================================\n");
        
        boolean generateLines = false;
        boolean generateDescriptions = false;
        boolean generateBookText = false;
        boolean generateGrid = false;
        boolean overwriteFiles = false;
        boolean verbose = false;
        String methodDefinitionFile = null;
        String outputDirectory = null;
        String compositionFile = null;
        String linePropertiesFile = "LineFactory.properties";
        
        String argument;
        for (int i = 0; i < pr_Arguments.length; i++) {
            argument = pr_Arguments[i];
            if (argument.startsWith("-")) {
                argument = argument.substring(1).toLowerCase();
                if (argument.equals(ARGUMENT_METHODS)) {
                    if (i + 1 < pr_Arguments.length) {
                        methodDefinitionFile = pr_Arguments[++i];
                    }
                } else if (argument.equals(ARGUMENT_OUTPUT_DIRECTORY)) {
                    if (i + 1 < pr_Arguments.length) {
                        outputDirectory = pr_Arguments[++i];
                    }
                } else if (argument.equals(ARGUMENT_COMPOSITIONS)) {
                    if (i + 1 < pr_Arguments.length) {
                        compositionFile = pr_Arguments[++i];
                    }
                } else if (argument.equals(ARGUMENT_GENERATE_LINES)) {
                    generateLines = true;
                } else if (argument.equals(ARGUMENT_LINES_PROPERTIES_FILE)) {
                    if (i + 1 < pr_Arguments.length) {
                    	linePropertiesFile = pr_Arguments[++i];
                    }
                } else if (argument.equals(ARGUMENT_GENERATE_DESCRIPTIONS)) {
                    generateDescriptions = true;
                } else if (argument.equals(ARGUMENT_GENERATE_BOOK_TEXT)) {
                    generateBookText = true;
                } else if (argument.equals(ARGUMENT_GENERATE_GRID)) {
                    generateGrid = true;
                } else if (argument.equals(ARGUMENT_OVERWRITE)) {
                    overwriteFiles = true;
                } else if (argument.equals(ARGUMENT_VERBOSE)) {
                	verbose = true;
                }
                
            }
        }
        
        final SimpleLineGenerator generator = new SimpleLineGenerator(outputDirectory, linePropertiesFile, overwriteFiles);

        if (methodDefinitionFile != null) {
            generator.readMethodDefinitions(methodDefinitionFile);
        } else {
            System.out.println("ERROR: You must specify a method definition file.\n");
            generator.outputUsage();
            return;
        }
        
        if (!generateLines && !generateDescriptions && !generateBookText && !generateGrid && compositionFile == null) {
            generator.outputUsage();
            return;
        } else if (outputDirectory == null && (generateLines || generateDescriptions || generateBookText || generateGrid)) {
            System.out.println("ERROR: You must specify an output directory when generating files.\n");
            generator.outputUsage();
            return;
        } else if (generateBookText && (compositionFile == null)) {
            System.out.println("ERROR: You must specify a composition file when generating book text.\n");
            generator.outputUsage();
            return;
        }
        
        if (compositionFile != null) {
            generator.processCompositions(new File(compositionFile), verbose);
        }
        
        if (generateLines) {
            generator.generateLines();
        }
        
        if (generateDescriptions) {
            generator.generateDescriptions();
        }
        
        if (generateGrid) {
            generator.generateGrid();
        }
        
        System.out.println("\n====================================");
        
    }

    private void outputUsage() {
        
        System.out.println("Usage:\n");
        System.out.println("-" + ARGUMENT_METHODS + " <file>");
        System.out.println("\tPath to the method definition file.");
        System.out.println("-" + ARGUMENT_OUTPUT_DIRECTORY + " <directory>");
        System.out.println("\tThe directory into which the lines and descriptions should be saved.");
        System.out.println("-" + ARGUMENT_GENERATE_LINES);
        System.out.println("\tGenerate method lines.");
        System.out.println("-" + ARGUMENT_GENERATE_DESCRIPTIONS);
        System.out.println("\tGenerate method descriptions.");
        System.out.println("-" + ARGUMENT_GENERATE_BOOK_TEXT);
        System.out.println("\tGenerate text for InDesign.");
        System.out.println("-" + ARGUMENT_OVERWRITE);
        System.out.println("\tOverwite any existing files.");
        System.out.println("-" + ARGUMENT_COMPOSITIONS + " <directory>");
        System.out.println("\tThe directory from which to prove compositions.");       
        System.out.println("-" + ARGUMENT_VERBOSE);
        System.out.println("\tWhether to print all output.");       
        
    }

    public SimpleLineGenerator(final String pr_OutputDirectory, final String pr_LinePropertiesFile, final boolean pr_OverwriteFiles) {
        m_MethodLibrary = new Method[0];
        m_Overwrite = pr_OverwriteFiles;
        m_LinePropertiesFile = pr_LinePropertiesFile;
        if (pr_OutputDirectory != null) {
            m_OutputDirectory = new File(pr_OutputDirectory);
        } else {
            m_OutputDirectory = null;
        }
    }
    
    private void readMethodDefinitions(final String pr_File) {
        
        System.out.println("Reading method definition file...");
        MethodLibraryReader libraryReader = new TextMethodLibraryReader();
        libraryReader.addListener(this);
        try {
            m_MethodLibrary = libraryReader.read(new File(pr_File));
        } catch (MethodDefinitionException e) {
            System.err.println(e.getMessage());
            return;
        } catch (IOException e) {
            System.err.println("Unable to read method definitions file: " + e.getMessage());
            return;
        }
        
    }

    @Override
    public void methodLibraryErrorFound(String pr_Message) {
        System.err.println(pr_Message);
    }

    @Override
    public void methodLibraryWarningFound(String pr_Message) {
        System.out.println("[WARNING] " + pr_Message);
    }
    
    private void generateLines() {
        System.out.println("Generating method lines...");
        generate(new LineFactory(m_OutputDirectory, m_LinePropertiesFile), m_Overwrite);
    }
    
    private void generateDescriptions() {
        System.out.println("Generating method descriptions...");
        generate(new MethodDescriptionFactory(m_OutputDirectory), m_Overwrite);
    }
    
    private void generateGrid() {
        System.out.println("Generating method grids...");
        generate(new MethodGridFactory(m_OutputDirectory), m_Overwrite);
    }
    
    private void processCompositions(final File pr_CompositionFile, boolean pr_Verbose) {
      
        if (pr_CompositionFile.isDirectory()) {
            for (File f : pr_CompositionFile.listFiles()) {
                processCompositions(f, pr_Verbose);
            }
        } else {
            final ArrayList<AbstractComposition> provedCompositions = proveCompositions(pr_CompositionFile, pr_Verbose);
            if (provedCompositions.size() > 0) {
                final AbstractCompositionVisitor writer = new CompositionWriter(m_MethodLibrary, provedCompositions.toArray(new AbstractComposition[provedCompositions.size()]));
                try {
                    writer.visit(pr_CompositionFile);
                } catch (CompositionDefinitionException e) {
                    System.err.println(MessageFormat.format(Messages.getString("SimpleLineGenerator.CompositionDefinitionError"), pr_CompositionFile)); //$NON-NLS-1$
                    s_Logger.log(Level.SEVERE, MessageFormat.format("An error was found in the composition text.", pr_CompositionFile), e); //$NON-NLS-1$
                } catch (IOException e) {
                    System.err.println(MessageFormat.format(Messages.getString("SimpleLineGenerator.CompositionIOError"), pr_CompositionFile)); //$NON-NLS-1$
                    s_Logger.log(Level.SEVERE, MessageFormat.format("An error occurred writing to file for composition.", pr_CompositionFile), e); //$NON-NLS-1$
                }
            }
            if (m_OutputDirectory != null) {
                final NeatCompositionWriter bookTextWriter = new NeatCompositionWriter(m_MethodLibrary, m_Overwrite);
                try {
                    bookTextWriter.visit(pr_CompositionFile, new File(m_OutputDirectory.getAbsolutePath() + File.separatorChar + pr_CompositionFile.getName()));
                } catch (CompositionDefinitionException e) {
                    System.err.println(MessageFormat.format(Messages.getString("SimpleLineGenerator.CompositionDefinitionError"), pr_CompositionFile)); //$NON-NLS-1$
                    s_Logger.log(Level.SEVERE, MessageFormat.format("An error was found in the composition text.", pr_CompositionFile), e); //$NON-NLS-1$
                } catch (IOException e) {
                    System.err.println(MessageFormat.format(Messages.getString("SimpleLineGenerator.CompositionIOError"), pr_CompositionFile)); //$NON-NLS-1$
                    s_Logger.log(Level.SEVERE, MessageFormat.format("An error occurred writing to file for composition.", pr_CompositionFile), e); //$NON-NLS-1$
                }   
            }
        }
    }
    
    private ArrayList<AbstractComposition> proveCompositions(final File pr_CompositionFile, boolean pr_Verbose) {
        
        ArrayList<AbstractComposition> compositions = new ArrayList<AbstractComposition>();
        
        System.out.println();
        System.out.println("------------------------------------");
        System.out.println();
        System.out.println("Compositions File: " + pr_CompositionFile);

        CompositionReader reader = new CompositionReader(m_MethodLibrary);
        try {
            reader.visit(pr_CompositionFile);
        } catch (CompositionDefinitionException e) {
            System.err.println(e.getMessage());
            return compositions;
        } catch (IOException e) {
            System.err.println(MessageFormat.format(Messages.getString("SimpleLineGenerator.CompositionIOError"), pr_CompositionFile)); //$NON-NLS-1$
            return compositions;
        }
        
        int totalCompositions = 0;
        int provedCompositions = 0;
        for(AbstractComposition comp : reader.getCompositions()) {
            totalCompositions++;
            try {
                if (comp.isTrue()) {
                    provedCompositions++;
                }
            } catch (CompositionDefinitionException e) {
                System.err.println(MessageFormat.format("Composition #{0}: {1}", totalCompositions, e.getMessage()));
            } catch (FalseGridException e) {
                System.err.println(MessageFormat.format("Composition #{0}: {1}", totalCompositions, e.getMessage()));
                if (pr_Verbose) {
	                System.err.println("Grid so far:");
	                System.err.println(e.getGrid().toString());
                }
            } catch (Error e) {
                System.err.println(MessageFormat.format("Composition #{0}: An unexpected error has occured: {1}", totalCompositions, e.toString()));
                e.printStackTrace();
            }
        }
        
        if (provedCompositions < totalCompositions) {
            System.out.println(MessageFormat.format("{0} of {1} composition(s) proved.", provedCompositions, totalCompositions));
        } else {
            System.out.println(MessageFormat.format("All {0} composition(s) proved.", totalCompositions));
        }
        
        return reader.getCompositions();
    }
    
    
//    private void generateBookText(final File pr_CompositionFile, final boolean pr_Overwrite) {
//        System.out.println("Generating book text...");
//        
//        try {
//            final NeatCompositionWriter writer = new NeatCompositionWriter(m_MethodLibrary, m_OutputDirectory);
//            writer.visit(pr_CompositionFile);
//        } catch (CompositionDefinitionException e) {
//            System.err.println(MessageFormat.format(Messages.getString("SimpleLineGenerator.CompositionDefinitionError"), pr_CompositionFile)); //$NON-NLS-1$
//            s_Logger.log(Level.SEVERE, MessageFormat.format("An error was found in the composition text.", pr_CompositionFile), e); //$NON-NLS-1$
//        } catch (IOException e) {
//            System.err.println(MessageFormat.format(Messages.getString("SimpleLineGenerator.CompositionIOError"), pr_CompositionFile)); //$NON-NLS-1$
//            s_Logger.log(Level.SEVERE, MessageFormat.format("An error occurred writing to file for composition.", pr_CompositionFile), e); //$NON-NLS-1$
//        }
//        
//    }
//    
//    private void proveAndUpdate(final File pr_CompositionFile) {
//        
//        System.out.println("Proving compositions...");
//        ArrayList<AbstractComposition> provedCompositions = proveCompositions(pr_CompositionFile);
//
//        System.out.println("\n====================================");
//        System.out.println("\nUpdating compositions...");
//
//        if (pr_CompositionFile.isDirectory()) {
//            for (File f : pr_CompositionFile.listFiles()) {
//                compositions.addAll(proveCompositions(f));
//            }
//        } else {
//        }
//        
//    }
//    
//    private ArrayList<AbstractComposition> updateCompositions(final File pr_CompositionFile) {
//        final CompositionWriter writer = new CompositionWriter(m_MethodLibrary, provedCompositions.toArray(new AbstractComposition[provedCompositions.size()]));
//        try {
//            writer.visit(pr_CompositionFile);
//        } catch (CompositionDefinitionException e) {
//            System.err.println(MessageFormat.format(Messages.getString("SimpleLineGenerator.CompositionDefinitionError"), pr_CompositionFile)); //$NON-NLS-1$
//            s_Logger.log(Level.SEVERE, MessageFormat.format("An error was found in the composition text.", pr_CompositionFile), e); //$NON-NLS-1$
//        } catch (IOException e) {
//            System.err.println(MessageFormat.format(Messages.getString("SimpleLineGenerator.CompositionIOError"), pr_CompositionFile)); //$NON-NLS-1$
//            s_Logger.log(Level.SEVERE, MessageFormat.format("An error occurred writing to file for composition.", pr_CompositionFile), e); //$NON-NLS-1$
//        }
//    }
//    
//    private ArrayList<AbstractComposition> proveCompositions(final File pr_CompositionFile) {
//        
//        ArrayList<AbstractComposition> compositions = new ArrayList<AbstractComposition>();
//        
//        if (pr_CompositionFile.isDirectory()) {
//            for (File f : pr_CompositionFile.listFiles()) {
//                compositions.addAll(proveCompositions(f));
//            }
//        } else {
//            
//            System.out.println();
//            System.out.println("------------------------------------");
//            System.out.println();
//            System.out.println("Compositions File: " + pr_CompositionFile);
//
//            CompositionReader reader = new CompositionReader(m_MethodLibrary);
//            try {
//                reader.visit(pr_CompositionFile);
//            } catch (CompositionDefinitionException e) {
//                System.err.println(e.getMessage());
//                return compositions;
//            } catch (IOException e) {
//                System.err.println(MessageFormat.format(Messages.getString("SimpleLineGenerator.CompositionIOError"), pr_CompositionFile)); //$NON-NLS-1$
//                return compositions;
//            }
//            
//            int totalCompositions = 0;
//            int provedCompositions = 0;
//            for(AbstractComposition comp : reader.getCompositions()) {
//                totalCompositions++;
//                try {
//                    if (comp.isTrue()) {
//                        provedCompositions++;
//                    }
//                } catch (CompositionDefinitionException e) {
//                    System.err.println(MessageFormat.format("Composition #{0}: {1}", totalCompositions, e.getMessage()));
//                } catch (FalseGridException e) {
//                    System.err.println(MessageFormat.format("Composition #{0}: {1}", totalCompositions, e.getMessage()));               
//                }
//            }
//            
//            if (provedCompositions < totalCompositions) {
//                System.out.println(MessageFormat.format("{0} of {1} composition(s) proved.", provedCompositions, totalCompositions));
//            } else {
//                System.out.println(MessageFormat.format("All {0} composition(s) proved.", totalCompositions));
//            }
//            
//            compositions.addAll(reader.getCompositions());
//        }
//        
//        return compositions;
//    }
    
    private void generate(final AbstractMethodGenerator pr_Factory, final boolean pr_Overwrite) {
        
        for(Method method : m_MethodLibrary) {
            try {
                if (pr_Factory.generate(method, pr_Overwrite)) {
                    System.out.println(" - " + method.toString());
                }
            } catch (MethodGenerationException e) {
                System.err.println(MessageFormat.format(Messages.getString("SimpleLineGenerator.MethodGenerationError"), method)); //$NON-NLS-1$
                s_Logger.log(Level.SEVERE, MessageFormat.format("An error occurred generating the grid for {0}. See log for details.", method), e); //$NON-NLS-1$
            } catch (InvalidPlaceNotationException e) {
                System.err.println(MessageFormat.format(Messages.getString("SimpleLineGenerator.PlaceNotationError"), method, e.getMessage())); //$NON-NLS-1$
                s_Logger.log(Level.SEVERE, MessageFormat.format("Invalid place notation found for method {0}: {1}.", method), e); //$NON-NLS-1$
            } catch (IOException e) {
                System.err.println(MessageFormat.format(Messages.getString("SimpleLineGenerator.LineIOError"), method)); //$NON-NLS-1$
                s_Logger.log(Level.SEVERE, MessageFormat.format("An error occurred writing to file for method {0}.", method), e); //$NON-NLS-1$
            }
        }
    }

}
