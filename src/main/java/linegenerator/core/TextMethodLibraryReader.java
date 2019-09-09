package linegenerator.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

import linegenerator.core.exceptions.InvalidPlaceNotationException;
import linegenerator.core.exceptions.MethodDefinitionException;

public class TextMethodLibraryReader extends MethodLibraryReader {
    
    private static final int METHOD_FIELD_INDEX_NAME = 0;
    private static final int METHOD_FIELD_INDEX_TYPE = 1;
    private static final int METHOD_FIELD_INDEX_STAGE = 2;
    private static final int METHOD_FIELD_INDEX_NOTATION = 3;
    private static final int METHOD_FIELD_INDEX_PLAIN_LEAD_END = 4;
    private static final int METHOD_FIELD_INDEX_BOB_LEAD_END = 5;
    private static final int METHOD_FIELD_INDEX_SINGLE_LEAD_END = 6;
    private static final int METHOD_FIELD_INDEX_START_BELL = 7;
    private static final int METHOD_FIELD_INDEX_START_OFFSET = 8;
    private static final int METHOD_FIELD_CALLING_POSTITION_AMENDMENTS = 9;
    
    private static final int NUM_METHODS_FIELDS_REQUIRED = 5;

    public Method[] read(final File pr_File) throws MethodDefinitionException, IOException
    {
        final ArrayList<Method> readMethods = new ArrayList<Method>();
        final BufferedReader reader = new BufferedReader(new FileReader(pr_File));
        String line = null;
        String[] methodParts;
        String name;
        MethodType type;
        Stage stage;
        int startBell;
        int startOffset;
        String singleLeadEnd;
        String bobLeadEnd;
        while ((line = reader.readLine()) != null) {
            
            line = line.trim();
            if (line.length() == 0 || line.charAt(0) == '#') {
                continue;
            }
            
            startBell = 0;
            startOffset = 0;
            singleLeadEnd = null;
            bobLeadEnd = null;
            
            methodParts = line.split("\\|");
            if (methodParts.length < NUM_METHODS_FIELDS_REQUIRED) {
                errorFound(MessageFormat.format("Method definition has incorrect number of separators ({0}): {1}",
                                                methodParts.length,
                                                line));
                continue;
            }
            
            name = methodParts[METHOD_FIELD_INDEX_NAME];
            
            if (methodParts.length - 1 >= METHOD_FIELD_INDEX_START_OFFSET) {
                try {
                    startOffset = Integer.parseInt(methodParts[METHOD_FIELD_INDEX_START_OFFSET]);
                } catch (NumberFormatException e) {
                    warningFound(MessageFormat.format("A non-numerical start offset was specified for method {0}: {1}. Default value {2} used.",
                                                      name,
                                                      methodParts[METHOD_FIELD_INDEX_START_OFFSET],
                                                      startOffset));
                }
            }
            if (methodParts.length - 1 >= METHOD_FIELD_INDEX_START_BELL) {
                try {
                    startBell = Integer.parseInt(methodParts[METHOD_FIELD_INDEX_START_BELL]);
                } catch (NumberFormatException e) {
                	reader.close();
                    throw new MethodDefinitionException(
                            MessageFormat.format("A non-numerical start bell was specified for method {0}: {1}.",
                                                 name,
                                                 methodParts[METHOD_FIELD_INDEX_START_BELL]));
                }
            }
            if (methodParts.length - 1 >= METHOD_FIELD_INDEX_SINGLE_LEAD_END) {
                singleLeadEnd = methodParts[METHOD_FIELD_INDEX_SINGLE_LEAD_END];
                if (singleLeadEnd.equals("")) {
                    singleLeadEnd = null;
                }
            }
            if (methodParts.length - 1 >= METHOD_FIELD_INDEX_BOB_LEAD_END) {
            	bobLeadEnd = methodParts[METHOD_FIELD_INDEX_BOB_LEAD_END];
                if (bobLeadEnd.equals("")) {
                	bobLeadEnd = null;
                }
            }
            
            type = MethodType.getMethodType(methodParts[METHOD_FIELD_INDEX_TYPE]);
            if (type == null) {
                warningFound(MessageFormat.format("An unknown method type was specified for method {0}: {1}. Assuming Plain method.",
                                                  name,
                                                  methodParts[METHOD_FIELD_INDEX_TYPE]));
                type = MethodType.BOB;
            }
            
            try {
                stage = Stage.getStage(methodParts[METHOD_FIELD_INDEX_STAGE]);
            } catch (NumberFormatException e) {
                errorFound(MessageFormat.format("A non-numerical stage was specified for method {0}: {1}.",
                                                name,
                                                methodParts[METHOD_FIELD_INDEX_START_BELL]));
                continue;
            }
            
            try {
                Method newMethod = new Method(name,
                                              type,
                                              stage,
                                              methodParts[METHOD_FIELD_INDEX_NOTATION],
                                              methodParts[METHOD_FIELD_INDEX_PLAIN_LEAD_END],
                                              bobLeadEnd,
                                              singleLeadEnd,
                                              startBell,
                                              startOffset);
                
                if (methodParts.length - 1 >= METHOD_FIELD_CALLING_POSTITION_AMENDMENTS) {
                    for (String amendment : methodParts[METHOD_FIELD_CALLING_POSTITION_AMENDMENTS].split(",")) {
                        String[] amendmentParts = amendment.split("=");
                        if (amendmentParts.length == 2) {
                            newMethod.addCallingPositionAmendment(amendmentParts[0], amendmentParts[1]);
                        }
                    }
                }
                
                readMethods.add(newMethod);

            } catch (InvalidPlaceNotationException e) {
                errorFound(MessageFormat.format("Invalid place notation found for method {0}: {1}.", name, e.getMessage()));
            }
            
        }
        
        reader.close();
        
        return readMethods.toArray(new Method[readMethods.size()]);

    }
    
}
