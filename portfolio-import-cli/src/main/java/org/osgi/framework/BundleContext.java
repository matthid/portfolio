package org.osgi.framework;

/**
 * Minimal bundle-context stub interface; see {@link FrameworkUtil}. Must be
 * an interface because Eclipse runtime code invokes methods on it with
 * {@code invokeinterface}.
 */
public interface BundleContext
{
    String getProperty(String key);
}
