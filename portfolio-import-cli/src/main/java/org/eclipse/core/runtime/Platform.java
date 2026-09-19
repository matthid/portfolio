package org.eclipse.core.runtime;

import org.osgi.framework.Bundle;

/**
 * Minimal replacement for the Eclipse runtime {@link Platform} when running
 * the headless import tool on a plain classpath. The real implementation
 * requires a running OSGi framework; this stub answers the few calls made by
 * the import code path (logging and the operating system check used when
 * locking the saved file) and fails fast for everything else.
 */
public final class Platform
{
    public static final String OS_LINUX = "linux"; //$NON-NLS-1$
    public static final String OS_MACOSX = "macosx"; //$NON-NLS-1$
    public static final String OS_WIN32 = "win32"; //$NON-NLS-1$

    private static final ILog LOG = new ILog()
    {
        @Override
        public void log(IStatus status)
        {
            // intentionally silent: no OSGi log available
        }

        @Override
        public void addLogListener(ILogListener listener)
        {
            // not supported
        }

        @Override
        public void removeLogListener(ILogListener listener)
        {
            // not supported
        }

        @Override
        public Bundle getBundle()
        {
            return null;
        }
    };

    private Platform()
    {
        // utility class
    }

    public static ILog getLog(Bundle bundle)
    {
        return LOG;
    }

    public static String getOS()
    {
        return OS_LINUX;
    }

    public static IPath getStateLocation(Bundle bundle)
    {
        throw new IllegalStateException("no OSGi runtime available"); //$NON-NLS-1$
    }
}
