package linegenerator.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import linegenerator.core.exceptions.MethodDefinitionException;

public abstract class MethodLibraryReader {
    
    private ArrayList<IMethodLibraryReaderListener> m_Listeners;

	public abstract Method[] read(final File pr_File)
			throws MethodDefinitionException, IOException;

	public MethodLibraryReader() {
		m_Listeners = new ArrayList<IMethodLibraryReaderListener>();
	}

	public void addListener(final IMethodLibraryReaderListener pr_Listener) {
		m_Listeners.add(pr_Listener);
	}

	public void errorFound(final String pr_Message) {
		for (IMethodLibraryReaderListener listener : m_Listeners) {
			listener.methodLibraryErrorFound(pr_Message);
		}
	}

	public void warningFound(final String pr_Message) {
		for (IMethodLibraryReaderListener listener : m_Listeners) {
			listener.methodLibraryWarningFound(pr_Message);
		}
	}

}
