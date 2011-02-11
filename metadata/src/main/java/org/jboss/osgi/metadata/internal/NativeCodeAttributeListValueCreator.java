/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.osgi.metadata.internal;

import java.util.List;

import org.jboss.osgi.metadata.ManifestParser;
import org.jboss.osgi.metadata.ParameterizedAttribute;

/**
 * Create path attribute list from string attribute for Native Code manifest header. The Native Code header is specific in that it
 * allows duplicate attributes.
 *
 * @author David Bosschaert
 */
public class NativeCodeAttributeListValueCreator extends ParameterizedAttributeListValueCreator {
    @Override
    protected void parseAttribute(String attribute, List<ParameterizedAttribute> list, boolean trace) {
        ManifestParser.parse(attribute, list, false, true);
    }
}
