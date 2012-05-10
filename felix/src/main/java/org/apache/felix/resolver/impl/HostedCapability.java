/*
 * #%L
 * JBossOSGi Resolver Felix
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.resolver.impl;

import java.util.Map;

import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;

public class HostedCapability implements Capability
{
    private final Resource m_host;
    private final Capability m_cap;

    public HostedCapability(Resource host, Capability cap)
    {
        m_host = host;
        m_cap = cap;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final HostedCapability other = (HostedCapability) obj;
        if (m_host != other.m_host && (m_host == null || !m_host.equals(other.m_host)))
        {
            return false;
        }
        if (m_cap != other.m_cap && (m_cap == null || !m_cap.equals(other.m_cap)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 37 * hash + (m_host != null ? m_host.hashCode() : 0);
        hash = 37 * hash + (m_cap != null ? m_cap.hashCode() : 0);
        return hash;
    }

    public Capability getOriginalCapability()
    {
        return m_cap;
    }

    public Resource getResource()
    {
        return m_host;
    }

    public String getNamespace()
    {
        return m_cap.getNamespace();
    }

    public Map<String, String> getDirectives()
    {
        return m_cap.getDirectives();
    }

    public Map<String, Object> getAttributes()
    {
        return m_cap.getAttributes();
    }

// TODO: RFC-112 - Create a Felix type for this.
//    public List<String> getUses()
//    {
//        return m_cap.getUses();
//    }

    @Override
    public String toString()
    {
        if (m_host == null)
        {
            return getAttributes().toString();
        }
        if (getNamespace().equals(PackageNamespace.PACKAGE_NAMESPACE))
        {
            return "[" + m_host + "] "
                + getNamespace()
                + "; "
                + getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE);
        }
        return "[" + m_host + "] " + getNamespace() + "; " + getAttributes();
    }
}