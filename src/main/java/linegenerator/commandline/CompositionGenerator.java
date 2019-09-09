package linegenerator.commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import linegenerator.core.IMethodLibraryReaderListener;
import linegenerator.core.Method;
import linegenerator.core.TextMethodLibraryReader;
import linegenerator.core.MethodType;
import linegenerator.core.SimpleComposition;
import linegenerator.core.Stage;
import linegenerator.core.exceptions.CompositionDefinitionException;
import linegenerator.core.exceptions.DoesNotEndInRoundsException;
import linegenerator.core.exceptions.FalseGridException;
import linegenerator.core.exceptions.MethodDefinitionException;

public class CompositionGenerator implements IMethodLibraryReaderListener {
    
    private static final int MAX_CHANGES = 5300;
	private static final String PLAIN_CALL = "p";
	private static final String BOB_CALL = "-";
	private static final String SINGLE_CALL = "s";
	private static final String ARGUMENT_METHODS = "methods";
    private static final String ARGUMENT_OUTPUT_FILE = "outputfile";
    private static final String ARGUMENT_PEAL_PROVER_OUTPUT = "forpealprover";
    
    private BufferedReader m_Reader = new BufferedReader(new InputStreamReader(System.in));
    
    private Method[] m_MethodLibrary;
    private File m_OutputFile;
	private boolean m_ForPealProver;

    /**
     * @param args
     */
    public static void main(final String[] pr_Arguments) {

        String methodDefinitionFile = null;
        String outputFile = null;
        boolean forPealProver = false;
        
        String argument;
        for (int i = 0; i < pr_Arguments.length; i++) {
            argument = pr_Arguments[i];
            if (argument.startsWith(BOB_CALL)) {
                argument = argument.substring(1).toLowerCase();
                if (argument.equals(ARGUMENT_METHODS)) {
                    if (i + 1 < pr_Arguments.length) {
                        methodDefinitionFile = pr_Arguments[++i];
                    }
                } else if (argument.equals(ARGUMENT_OUTPUT_FILE)) {
                    if (i + 1 < pr_Arguments.length) {
                        outputFile = pr_Arguments[++i];
                    }
	            } else if (argument.equals(ARGUMENT_PEAL_PROVER_OUTPUT)) {
	                if (i + 1 < pr_Arguments.length) {
	                	forPealProver = pr_Arguments[++i].toLowerCase().matches("true|yes");
	                }
	            }
            }
        }
        
        if (outputFile == null || methodDefinitionFile == null) {
        	System.err.println("No output file or method library provided.");
        	outputUsage();
        } else {
	        try {
				new CompositionGenerator(outputFile, methodDefinitionFile, forPealProver).run();
			} catch (MethodDefinitionException | IOException e) {
				System.err.println(e.getMessage());
				outputUsage();
			}
        }
        
        System.out.println("\n====================================");
        
    }
    
    public static void outputUsage() {
        
        System.out.println("Usage:\n");
        System.out.println(BOB_CALL + ARGUMENT_METHODS + " <file>");
        System.out.println("\tPath to the method definition file.");
        System.out.println(BOB_CALL + ARGUMENT_OUTPUT_FILE);
        System.out.println("\tThe file to write true compositions to.");
        
    }
    
    public CompositionGenerator(final String pr_OutputFile, final String pr_MethodLibraryPath, final boolean pr_ForPealProver) throws MethodDefinitionException, IOException {
        System.out.println("Reading method definition file...");
        TextMethodLibraryReader libraryReader = new TextMethodLibraryReader();
        m_MethodLibrary = libraryReader.read(new File(pr_MethodLibraryPath));
    	m_OutputFile = new File(pr_OutputFile);
    	m_ForPealProver = pr_ForPealProver;
    }
    
    public void run()
    {
        System.out.println("====================================");
        System.out.println("=      COMPOSITION GENERATOR       =");
        System.out.println("====================================");
        System.out.println("= Version 1.0.0");
        System.out.println("====================================\n");

        final String methodName = getInput("Name");
        final Stage stage = Stage.getStage(getInput("Stage (number of bells)"));
        
        java.util.List<Method> methods = new ArrayList<Method>();
        for (Method m : m_MethodLibrary) {
            if (m.getName().equals(methodName) && 
                m.getStage() == stage) {
                methods.add(m);
            }
        }
        
        Method method = null;
        if (methods.size() > 1) {
        	final MethodType type = MethodType.getMethodType(getInput("Type"));
        	for (Method m : methods) {
        		if (m.getMethodType() == type) {
        			method = m;
        			break;
                }
        	}
        } else if (methods.size() == 1) {
        	method = methods.get(0);
        }
        
        if (method == null) {
        	System.err.println("Unknown method.");
        	return;
        }
        
        System.out.println(String.format("\nGenerating composition for %s", method));
        
        HashMap<String, Method> map = new HashMap<>();
        map.put("", method);
        
        SimpleComposition comp = new SimpleComposition(map, 0);
        comp.setFirstMethod(method);
        
        
        ArrayList<String> calls = new ArrayList<String>();
        calls.add(PLAIN_CALL);
        calls.add(BOB_CALL);
        if (method.getSingleLeadEnd() != null) {
        	calls.add(SINGLE_CALL);
        }

        @SuppressWarnings("unchecked")
		List<String> compList = (List<String>) calls.clone();
        ArrayList<String> newCompList;
        final Set<SimpleComposition> trueComps = new HashSet<>();
        String currentCallList = "";
        
        try {

        	if (!m_ForPealProver) {
        		writeMessage("[" + method.getName() + "," + method.getStage().getBells() + "]\n", true);
        	} else {
        		writeMessage("! Generated compositions...", true);
        	}
        	
        	while (compList.size() > 0) {
        		
        		newCompList = new ArrayList<String>();

        		for (String callList : compList) {
		        	
	        		for (String call : calls) {
			        	
				        try {
				        	
				        	currentCallList = callList + call;
//				        	System.out.println(String.format("Trying composition %s...", currentCallList));
					    	comp = new SimpleComposition(map, 0);
					    	comp.setPadPlainLeads(false);
					        comp.setFirstMethod(method);
					    	comp.addRows(currentCallList);
					    	
				        	if (comp.isTrue()) {
				        		trueComps.add(comp);
				        		System.out.println(String.format("Found #%d: %d changes (%s). [%d branches remaining]", trueComps.size(), comp.getChanges(), currentCallList, compList.size()));
				        		
				        		if (!m_ForPealProver) {
				        			writeMessage(comp.toString() + 
				        					     "# Peal Prover: " + currentCallList.replaceAll("\\-", " b").replaceAll("p", " p").replaceAll("s", " s") + "\n");
				        		} else {
				        			writeMessage("!c1 = {" + currentCallList.replaceAll("\\-", " b").replaceAll("p", " p").replaceAll("s", " s") + " }");
				        		}
				        		
				        	}
				        	
				        } catch (DoesNotEndInRoundsException e) {
//				        	System.out.println(String.format("Composition %s does not yet end in rounds.", currentCallList));
				        	if (comp.getChanges() > MAX_CHANGES) {
				        		System.out.println(String.format("Number of changes exceeds 5300, giving up composition %s", currentCallList));
				        	} else {
//					        	System.out.println("Maybe next time...");
					        	newCompList.add(currentCallList);
				        	}
				        } catch (FalseGridException e) {
//				        	System.out.println(String.format("Composition %s is false. Forgetting it :-(", currentCallList));
						}
				    	
			        }
	        		
	        	}
	        	
	        	compList = newCompList;
	        	
	        	System.out.println(String.format("%d more branches to try...", compList.size()));
        	}

        	if (m_ForPealProver) {
        		writeMessage("composition = { c1 }");
        	}
        	
	    } catch (CompositionDefinitionException e) {
	    	System.err.println("Composition genearator made unknown call!");
	    } catch (IOException e) {
	    	System.err.println("Error writing to composition file: " + e.getMessage());
	    }
    }
    
    private void writeMessage(final String pr_Message, final boolean pr_Overwrite) throws IOException
    {
    	FileWriter writer = new FileWriter(m_OutputFile, !pr_Overwrite);
		writer.write(pr_Message + "\n");
		writer.close();
    }
    
    private void writeMessage(final String pr_Message) throws IOException
    {
    	writeMessage(pr_Message, false);
    }
    
    private String getInput(final String pr_Prompt)
    {
        System.out.print(pr_Prompt + ": ");
        try {
            return m_Reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        return null;
    }

    @Override
    public void methodLibraryErrorFound(String pr_Message) {
        System.err.println(pr_Message);
    }

    @Override
    public void methodLibraryWarningFound(String pr_Message) {
        System.out.println("[WARNING] " + pr_Message);
    }

}
