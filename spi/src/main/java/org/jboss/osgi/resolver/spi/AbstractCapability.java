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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.osgi.resolver.XAttachmentSupport;
import org.jboss.osgi.resolver.XAttributeSupport;
import org.jboss.osgi.resolver.XCapability;
import org.jboss.osgi.resolver.XDirectiveSupport;
import org.jboss.osgi.resolver.XModule;
import org.jboss.osgi.resolver.XRequirement;

/**
 * The abstract implementation of a {@link XCapability}.
 *
 * @author thomas.diesler@jboss.com
 * @since 02-Jul-2010
 */
class AbstractCapability extends AbstractElement implements XCapability
{
   private XModule module;
   private XDirectiveSupport directives;
   private XAttributeSupport attributes;
   private XAttachmentSupport attachments;

   public AbstractCapability(AbstractModule module, String name, Map<String, String> dirs, Map<String, Object> atts)
   {
      super(name);
      this.module = module;
      
      if (dirs != null)
         directives = new DirectiveSupporter(dirs);
      if (atts != null)
         attributes = new AttributeSupporter(atts);
   }

   @Override
   public XModule getModule()
   {
      return module;
   }
   
   @Override
   public Set<XRequirement> getWiredRequirements()
   {
      if (getModule().isResolved() == false)
         return null;
      
      // The resolver may be null if this capability has already been removed from the resolver
      AbstractResolver resolver = (AbstractResolver)getModule().getResolver();
      if (resolver == null)
         return Collections.emptySet();
      
      return resolver.getWiredRequirements(this);
   }

   @Override
   public Object getAttribute(String key)
   {
      if (attributes == null)
         return null;
      
      return attributes.getAttribute(key);
   }

   @Override
   public Map<String, Object> getAttributes()
   {
      if (attributes == null)
         return Collections.emptyMap();
      
      return attributes.getAttributes();
   }

   @Override
   public String getDirective(String key)
   {
      if (directives == null)
         return null;
      
      return directives.getDirective(key);
   }

   @Override
   public Map<String, String> getDirectives()
   {
      if (directives == null)
         return Collections.emptyMap();
      
      return directives.getDirectives();
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
}