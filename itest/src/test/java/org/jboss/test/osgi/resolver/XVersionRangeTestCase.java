/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.osgi.resolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jboss.osgi.resolver.XVersionRange;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 * Unit tests for the {@link XVersionRange} class
 * 
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class XVersionRangeTestCase {
    @Test
    public void testParseVersionRange1() {
        XVersionRange vr = XVersionRange.parse("1.6.0");
        assertEquals(Version.parseVersion("1.6.0"), vr.getFloor());
        assertTrue(vr.isFloorInclusive());
        Assert.assertNull(vr.getCeiling());
    }

    @Test
    public void testParseVersionRange2() {
        XVersionRange vr = XVersionRange.parse(" 1.6.0 ");
        assertEquals(Version.parseVersion("1.6.0"), vr.getFloor());
        assertTrue(vr.isFloorInclusive());
        assertNull(vr.getCeiling());
    }

    @Test
    public void testParseVersionRange3() {
        XVersionRange vr = XVersionRange.parse("[1,2]");
        assertEquals(Version.parseVersion("1"), vr.getFloor());
        assertTrue(vr.isFloorInclusive());
        assertEquals(Version.parseVersion("2"), vr.getCeiling());
        assertTrue(vr.isCeilingInclusive());
    }

    @Test
    public void testParseVersionRange4() {
        XVersionRange vr = XVersionRange.parse(" ( 1,2 ) ");
        assertEquals(Version.parseVersion("1"), vr.getFloor());
        assertFalse(vr.isFloorInclusive());
        assertEquals(Version.parseVersion("2"), vr.getCeiling());
        assertFalse(vr.isCeilingInclusive());
    }
}
