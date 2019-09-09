package linegenerator.commandline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import linegenerator.core.Method;
import linegenerator.core.MethodLibraryReader;
import linegenerator.core.Stage;
import linegenerator.core.XMLMethodLibraryReader;
import linegenerator.core.exceptions.MethodDefinitionException;

public class XMLMethodLibraryConvertor {
	
    private static final String ARGUMENT_METHODS = "definitions";

	public static void main(String[] pr_Arguments) {

        String methodDefinitionFile = null;
        
        String argument;
        for (int i = 0; i < pr_Arguments.length; i++) {
            argument = pr_Arguments[i];
            if (argument.startsWith("-")) {
                argument = argument.substring(1).toLowerCase();
                if (argument.equals(ARGUMENT_METHODS)) {
                    if (i + 1 < pr_Arguments.length) {
                        methodDefinitionFile = pr_Arguments[++i];
                    }
                }
            }
        }
		
        String outputFile = methodDefinitionFile.substring(0, methodDefinitionFile.lastIndexOf('.'));
		MethodLibraryReader reader = new XMLMethodLibraryReader();
		try {
			Method[] methods = reader.read(new File(methodDefinitionFile));
			
			FileWriter writer = null;
			Stage stage = null;
			for (Method m : methods) {
				if (m.getStage() != stage) {
					if (writer != null) {
						writer.close();
					}
					writer = new FileWriter(outputFile + "_" + m.getStage().toString() + ".txt");
					writer.write("\n#\n# " + m.getStage().toString() + "\n#\n");
					stage = m.getStage();
				}
				writer.write(methodToDefinitionLine(m));
			}
			writer.close();
			
		} catch (MethodDefinitionException | IOException e) {
			e.printStackTrace();
		}

	}
	
	public static String methodToDefinitionLine(final Method method) {
		return String.format("%s|%s|%s|%s|%s|%s|%s\n",
				method.getName(),
				method.getMethodType().getCode(),
				method.getStage().getBells(),
				method.getPlaceNotation()[0],
				method.getLeadEnd()[0],
				method.getBobLeadEnd() == null ? "" : method.getBobLeadEnd()[0],
				method.getSingleLeadEnd() == null ? "" : method.getSingleLeadEnd()[0]);
	}

}
