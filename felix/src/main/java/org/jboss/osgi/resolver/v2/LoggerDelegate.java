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
package org.jboss.osgi.resolver.v2;

//$Id: FelixIntegration.java 84730 2009-02-25 12:57:23Z thomas.diesler@jboss.com $

import org.jboss.logging.Logger;

/**
 * An integration with the resolver Logger.
 * 
 * This Logger delegates framework log messages to JBoss Logging.
 * 
 * @author thomas.diesler@jboss.com
 * @since 14-Feb-2012
 */
public class LoggerDelegate implements org.apache.felix.resolver.Logger {

    // Provide logging
    private static final Logger log = Logger.getLogger(LoggerDelegate.class);

    public LoggerDelegate() {
    }

    @Override
    public void log(int level, String msg) {
        log(level, msg, null);
    }

    @Override
    public void log(int level, String msg, Throwable throwable) {
        if (level == LOG_DEBUG) {
            log.debug(msg, throwable);
        } else if (level == LOG_INFO) {
            log.info(msg, throwable);
        } else if (level == LOG_WARNING) {
            log.warn(msg);
            if (throwable != null)
                log.debug(msg, throwable);
        } else if (level == LOG_ERROR) {
            log.error(msg, throwable);
        }
    }
}