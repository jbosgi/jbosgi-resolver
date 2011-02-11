/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.osgi.metadata;

import java.io.Serializable;

/**
 * Meta data for native code libraries as defined by OSGi R4V42.
 *
 * 3.9 Loading Native Code Libraries http://www.osgi.org/Download/File?url=/download/r4v42/r4.core.pdf
 *
 * @author thomas.diesler@jboss.com
 * @author David Bosschaert
 * @since 21-Jan-2010
 */
public class NativeLibrary implements Serializable {
    /** The serialVersionUID */
    private static final long serialVersionUID = -1637806718398794304L;

    private final String libraryPath;

    /**
     * Create a NativeCode instance with mandatory properties.
     *
     * @param libraryPath The library path
     */
    public NativeLibrary(String libraryPath) {
        if (libraryPath == null)
            throw new IllegalArgumentException("Null library path: " + libraryPath);
        this.libraryPath = libraryPath;
    }

    public String getLibraryPath() {
        return libraryPath;
    }
}
