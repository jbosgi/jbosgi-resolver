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

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.resolver.FelixResolveContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.resource.Wiring;

public class Util
{
    public static String getSymbolicName(Resource resource)
    {
        List<Capability> caps = resource.getCapabilities(null);
        for (Capability cap : caps)
        {
            if (cap.getNamespace().equals(IdentityNamespace.IDENTITY_NAMESPACE))
            {
                return cap.getAttributes().get(IdentityNamespace.IDENTITY_NAMESPACE).toString();
            }
        }
        return null;
    }

    public static Version getVersion(Resource resource)
    {
        List<Capability> caps = resource.getCapabilities(null);
        for (Capability cap : caps)
        {
            if (cap.getNamespace().equals(IdentityNamespace.IDENTITY_NAMESPACE))
            {
                return (Version)
                    cap.getAttributes().get(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE);
            }
        }
        return null;
    }

    public static boolean isFragment(Resource resource)
    {
        Capability icap = resource.getCapabilities(IdentityNamespace.IDENTITY_NAMESPACE).get(0);
        String type = (String) icap.getAttributes().get(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE);
        return IdentityNamespace.TYPE_FRAGMENT.equals(type);
    }

    public static boolean isOptional(Requirement req)
    {
        String resolution = req.getDirectives().get(Constants.RESOLUTION_DIRECTIVE);
        return Constants.RESOLUTION_OPTIONAL.equals(resolution);
    }

    public static List<Requirement> getDynamicRequirements(List<Requirement> reqs)
    {
        List<Requirement> result = new ArrayList<Requirement>();
        if (reqs != null)
        {
            for (Requirement req : reqs)
            {
                String resolution = req.getDirectives().get(Constants.RESOLUTION_DIRECTIVE);
                if ((resolution != null) && resolution.equals("dynamic"))
                {
                    result.add(req);
                }
            }
        }
        return result;
    }

    public static Capability getSatisfyingCapability(
        FelixResolveContext env, Resource br, Requirement req)
    {
        Wiring wiring = env.getWirings().get(br);
        List<Capability> caps = (wiring != null)
            ? wiring.getResourceCapabilities(null)
            : br.getCapabilities(null);
        if (caps != null)
        {
            for (Capability cap : caps)
            {
                if (cap.getNamespace().equals(req.getNamespace())
                    && env.matches(req, cap))
                {
                    return cap;
                }
            }
        }
        return null;
    }
}