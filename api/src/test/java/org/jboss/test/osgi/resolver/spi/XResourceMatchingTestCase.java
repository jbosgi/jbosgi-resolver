/*
 * #%L
 * JBossOSGi Resolver API
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

package org.jboss.test.osgi.resolver.spi;

import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XResourceBuilder;
import org.jboss.osgi.resolver.XResourceBuilderFactory;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.namespace.BundleNamespace;

/**
 * Unit tests for resource matching
 *
 * @author Thomas.Diesler@jboss.com
 */
public class XResourceMatchingTestCase extends AbstractTestBase {

    @Test
    public void testAttributOnSymbolicNameNoMatch() throws Exception {
        XResourceBuilder cbuilder = XResourceBuilderFactory.create();
        XCapability cap = cbuilder.addCapability(BundleNamespace.BUNDLE_NAMESPACE, "org.jboss.test.cases.repository.tb1");
        cbuilder.getResource();
        //cap.getAttributes().put("foo", "bar");

        XResourceBuilder rbuilder = XResourceBuilderFactory.create();
        Filter filter = FrameworkUtil.createFilter("(&(osgi.wiring.bundle=org.jboss.test.cases.repository.tb1)(foo=bar))");
        XRequirement req = rbuilder.addRequirement("osgi.wiring.bundle", filter);
        rbuilder.getResource();

        Assert.assertFalse("No match", req.matches(cap));
    }

    @Test
    public void testAttributOnSymbolicNameMatch() throws Exception {
        XResourceBuilder cbuilder = XResourceBuilderFactory.create();
        XCapability cap = cbuilder.addCapability(BundleNamespace.BUNDLE_NAMESPACE, "org.jboss.test.cases.repository.tb1");
        cap.getAttributes().put("foo", "bar");
        cbuilder.getResource();

        XResourceBuilder rbuilder = XResourceBuilderFactory.create();
        Filter filter = FrameworkUtil.createFilter("(&(osgi.wiring.bundle=org.jboss.test.cases.repository.tb1)(foo=bar))");
        XRequirement req = rbuilder.addRequirement("osgi.wiring.bundle", filter);
        rbuilder.getResource();

        Assert.assertTrue("Match", req.matches(cap));
    }
}
