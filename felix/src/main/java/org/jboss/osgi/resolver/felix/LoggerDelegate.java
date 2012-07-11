/*
 * #%L
 * JBossOSGi Resolver Felix
 * %%
 * Copyright (C) 2010 - 2012 JBoss by Red Hat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
