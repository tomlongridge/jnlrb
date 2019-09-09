package linegenerator.core;

import java.util.ArrayList;
import java.util.Collections;

import linegenerator.core.exceptions.InvalidPlaceNotationException;

public class Notation extends ArrayList<String> {
    
    /** Character used to denote symmetric place notation. */
    public static final char PLACE_NOTATION_SYMMETRIC = '&';
    
    /** Character used to denote non-symmetric place notation. */
    public static final char PLACE_NOTATION_NONSYMMETRIC = '+';

    /** Regular expression to validate place notation for a method. */
    public static final String METHOD_PLACE_NOTATION_REGEXP = "[\\&\\+]?[\\dETA-ZXx\\.]+";
    
    private static final long serialVersionUID = -2756089727457872546L;
    
    public Notation()
    {
        // Do nothing
    }
    
    public Notation(final String pr_NotationString) throws InvalidPlaceNotationException {
        
        if (!pr_NotationString.matches(METHOD_PLACE_NOTATION_REGEXP)) {
            throw new InvalidPlaceNotationException("The place notation \"" + pr_NotationString + "\" in invalid.");
        }

        final StringBuilder notation = new StringBuilder(pr_NotationString);
        boolean isSymmetric = false;
        if (pr_NotationString.charAt(0) == PLACE_NOTATION_SYMMETRIC ||
            pr_NotationString.charAt(0) == PLACE_NOTATION_NONSYMMETRIC) {
            isSymmetric = pr_NotationString.charAt(0) == PLACE_NOTATION_SYMMETRIC;
            notation.deleteCharAt(0);
        }
        
        addAll(getNotationFromString(notation.toString()));
        
        if (isSymmetric) {
            Notation reversedNotation = (Notation) this.clone();
            Collections.reverse(reversedNotation);
            reversedNotation.remove(0);
            addAll(reversedNotation);
        }
        
    }
    
    public static Notation getNotationFromString(final String pr_NotationString)
    {
        Notation entries = new Notation();
        String currentNotation = null;
        for (char c : pr_NotationString.toCharArray()) {
            if (Stage.isLabel(c)) {
                if (currentNotation == null) {
                    currentNotation = "";
                }
                currentNotation += c;
            } else {
                if (currentNotation != null) {
                    entries.add(currentNotation);
                    currentNotation = null;
                }
                if (c == 'x') {
                    entries.add(null);
                }
            }
        }
        if (currentNotation != null) {
            entries.add(currentNotation);
        }
        
        return entries;
    }
    
    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder();
        String lastChar = null;
        for (String s : this) {
            if (s == null) {
                returnString.append("x");
            } else {
                if (lastChar != null) {
                    returnString.append(".");
                }
                returnString.append(s);
            }
            lastChar = s;
        }
        return returnString.toString();
    }
    
}
