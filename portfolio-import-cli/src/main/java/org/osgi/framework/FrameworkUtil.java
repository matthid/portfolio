package org.osgi.framework;

/**
 * Minimal stand-in for the OSGi {@link FrameworkUtil} used when running the
 * headless import tool on a plain classpath instead of inside an OSGi
 * framework. {@link #getBundle(Class)} returns a stub bundle so that code
 * such as the PDFBox adapters can query a bundle version.
 */
public final class FrameworkUtil
{
    private static final class StubBundleContext implements BundleContext
    {
        @Override
        public String getProperty(String key)
        {
            return System.getProperty(key);
        }
    }

    private static final class StubBundle implements Bundle
    {
        @Override
        public Version getVersion()
        {
            return new Version("3.0.8"); //$NON-NLS-1$
        }

        @Override
        public String getSymbolicName()
        {
            return "name.abuchen.portfolio"; //$NON-NLS-1$
        }

        @Override
        public long getBundleId()
        {
            return 0;
        }

        @Override
        public int getState()
        {
            return Bundle.ACTIVE;
        }

        @Override
        public <A> A adapt(Class<A> adapterType)
        {
            return null;
        }

        @Override
        public BundleContext getBundleContext()
        {
            return new StubBundleContext();
        }

        @Override
        @SuppressWarnings("rawtypes")
        public java.util.Dictionary getHeaders()
        {
            return new java.util.Hashtable();
        }
    }

    private static final Bundle STUB_BUNDLE = new StubBundle();

    private FrameworkUtil()
    {
        // utility class
    }

    public static Bundle getBundle(Class<?> classFromBundle)
    {
        return STUB_BUNDLE;
    }
}
