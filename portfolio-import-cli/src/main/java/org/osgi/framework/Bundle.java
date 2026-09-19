package org.osgi.framework;

/**
 * Minimal bundle stub interface returned by {@link FrameworkUtil} when
 * running outside an OSGi framework. It must remain an interface because the
 * PDFBox adapters invoke methods on it with {@code invokeinterface}. Only
 * the few members actually used by the headless import code path carry a
 * meaningful implementation.
 */
public interface Bundle
{
    int ACTIVE = 32;
    int INSTALLED = 2;
    int RESOLVED = 4;
    int STARTING = 8;
    int STOPPING = 16;
    int UNINSTALLED = 1;

    Version getVersion();

    String getSymbolicName();

    long getBundleId();

    int getState();

    <A> A adapt(Class<A> adapterType);

    BundleContext getBundleContext();

    @SuppressWarnings("rawtypes")
    java.util.Dictionary getHeaders();
}
