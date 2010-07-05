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
package org.jboss.osgi.resolver.spi;

import org.jboss.osgi.resolver.XAttachmentSupport;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XModule;
import org.jboss.osgi.resolver.XRequirement;
import org.jboss.osgi.resolver.XWire;
import org.jboss.osgi.resolver.spi.AbstractElement.AttachmentSupporter;


/**
 * The abstract implementation of a {@link XCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
class AbstractWire implements XWire, XAttachmentSupport
{
   private XModule importer;
   private XRequirement requirement;
   private XModule exporter;
   private XCapability capability;
   private XAttachmentSupport attachments;
   
   AbstractWire(XModule importer, XRequirement requirement, XModule exporter, XCapability capability)
   {
      this.importer = importer;
      this.requirement = requirement;
      this.exporter = exporter;
      this.capability = capability;
   }

   @Override
   public XModule getImporter()
   {
      return importer;
   }

   @Override
   public XRequirement getRequirement()
   {
      return requirement;
   }

   @Override
   public XModule getExporter()
   {
      return exporter;
   }

   @Override
   public XCapability getCapability()
   {
      return capability;
   }

   @Override
   public <T> T addAttachment(Class<T> clazz, T value)
   {
      if (attachments  == null)
         attachments = new AttachmentSupporter();
      
      return attachments.addAttachment(clazz, value);
   }

   @Override
   public <T> T getAttachment(Class<T> clazz)
   {
      if (attachments  == null)
         return null;
      
      return attachments.getAttachment(clazz);
   }

   @Override
   public <T> T removeAttachment(Class<T> clazz)
   {
      if (attachments  == null)
         return null;
      
      return attachments.removeAttachment(clazz);
   }

   @Override
   public String toString()
   {
      return "Wire[" + importer + requirement + " --> " + exporter + capability + "]";
   }
}