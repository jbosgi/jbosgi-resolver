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

package org.jboss.osgi.resolver.felix;

import static org.jboss.osgi.resolver.internal.ResolverLogger.LOGGER;

import org.jboss.osgi.resolver.internal.ResolverLogger;

/**
 * An integration with the resolver Logger.
 *
 * This Logger delegates framework log messages to JBoss Logging.
 *
 * @author thomas.diesler@jboss.com
 * @since 14-Feb-2012
 */
public class LoggerDelegate extends org.apache.felix.resolver.Logger {

	public LoggerDelegate() {
	    super(getLevel(ResolverLogger.LOGGER));
	}

	private static int getLevel(ResolverLogger logger) {
        if (logger.isDebugEnabled())
            return LOG_DEBUG;
        if (logger.isInfoEnabled())
            return LOG_INFO;
        else
            return LOG_WARNING;
    }


	@Override
	public void doLog(int level, String msg, Throwable throwable) {
		switch (level) {
		case LOG_ERROR:
		    LOGGER.error(msg, throwable);
			break;
		case LOG_WARNING:
		    LOGGER.warn(msg, throwable);
			break;
		case LOG_INFO:
		    LOGGER.info(msg, throwable);
			break;
		case LOG_DEBUG:
		    LOGGER.debug(msg, throwable);
			break;
		}
	}
}