package org.osgi.framework;

/**
 * Minimal version stub for {@link FrameworkUtil#getBundle(Class)} usage
 * outside an OSGi framework.
 */
public class Version implements Comparable<Version>
{
    private final String version;

    public Version(String version)
    {
        this.version = version == null ? "0.0.0" : version; //$NON-NLS-1$
    }

    @Override
    public String toString()
    {
        return version;
    }

    @Override
    public int compareTo(Version other)
    {
        return version.compareTo(other.version);
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Version v && version.equals(v.version);
    }

    @Override
    public int hashCode()
    {
        return version.hashCode();
    }
}
