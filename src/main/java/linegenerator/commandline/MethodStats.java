package linegenerator.commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import linegenerator.core.Method;
import linegenerator.core.MethodLibraryReader;
import linegenerator.core.TextMethodLibraryReader;
import linegenerator.core.exceptions.MethodDefinitionException;

public class MethodStats {
	
	private final static String BELL_BOARD_URL = "http://www.bb.ringingworld.co.uk"; //$NON-NLS-1$
	private static final String BELL_BOARD_SEARCH_PAGE = "search.php"; //$NON-NLS-1$
	private final static Pattern NUM_PERFORMANCE_PATTERN = Pattern.compile(".*Found (\\d+) performances that match your search.*"); //$NON-NLS-1$

	public static void main(String[] args) {

		MethodLibraryReader libraryReader = new TextMethodLibraryReader();
		Method[] methods;
		Properties prop;
		String methodName;
		
		String startMethod = "Shotley Bridge Surprise Minor";
		boolean start = false;
		
		try {
			methods = libraryReader.read(new File("All\\allmeths_Minor.txt"));
			for (Method m : methods) {
				if (m.toString().equals(startMethod)) {
					start = true;
				}
				if (start) {
					System.out.print(m.toString() + " ... ");
					prop = new Properties();
					methodName = m.getName();
					if (m.getMethodType().shouldDisplayNameInMethod()) {
						methodName += " ";
						for (String w : m.getMethodType().toString().split(" ")) {
							methodName += w.charAt(0) + "*";
						}
					}
					methodName += " ";
					methodName += m.getStage();
					prop.put("title", methodName);
					System.out.println(search(prop));
				}
			}
		} catch (MethodDefinitionException | IOException e) {
			e.printStackTrace();
			return;
		}
		
	}
	
	public static int search (final Properties criteria) throws IOException
	{
		// Construction GET query string using search criteria
		StringBuffer searchString = new StringBuffer();
		Enumeration<Object> variables = criteria.keys();
		String key;
		String value;
		while (variables.hasMoreElements())
		{
			key = (String) variables.nextElement();
			value = URLEncoder.encode(criteria.getProperty(key), "UTF-8"); //$NON-NLS-1$
			if (!value.equals("")) //$NON-NLS-1$
			{
				searchString.append(String.format("%s=%s", key, value)); //$NON-NLS-1$
				searchString.append("&"); //$NON-NLS-1$
			}
		}
		searchString.deleteCharAt(searchString.length() - 1);

		// Connect to Bell Board and perform search
		final URL bellBoardURL = new URL(
				String.format("%s/%s?%s", BELL_BOARD_URL, BELL_BOARD_SEARCH_PAGE, searchString)); //$NON-NLS-1$
		final BufferedReader reader = new BufferedReader(new InputStreamReader(bellBoardURL.openStream()));

		// Extract ID of each found performance
		String line;
		Matcher m;
		while ((line = reader.readLine()) != null)
		{
			m = NUM_PERFORMANCE_PATTERN.matcher(line);
			if (m.matches())
			{
				reader.close();
				return Integer.parseInt(m.group(1));
			}
		}

		reader.close();
		return 0;
	}

}
