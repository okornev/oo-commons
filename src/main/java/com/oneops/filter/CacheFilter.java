/*******************************************************************************
 *
 *   Copyright 2016 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
package com.oneops.filter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.util.domain.CmsVar;
import org.apache.log4j.Logger;

import javax.servlet.*;
import java.io.IOException;

import static java.lang.Long.parseLong;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A base {@link Filter} class to check oneops metadata cache.
 *
 * @author Suresh G
 */
public abstract class CacheFilter implements Filter {

    private long lastUpdatedTs;
    private boolean cacheEnabled;
    private CmsCmManager cmManager;
    private LoadingCache<String, Long> varCache;
    private static final String MD_CACHE_STATUS_VAR = "MD_UPDATE_TIMESTAMP";
    protected final Logger logger = Logger.getLogger(this.getClass());

    /**
     * Initialize the cache filter.
     *
     * @param cacheEnabled set to true of md caching is enabled.
     * @param ttl          cache ttl
     * @param cacheSize    maximum cache size.
     * @param cmMgr        {@link CmsCmManager}
     */
    public CacheFilter(boolean cacheEnabled, long ttl, long cacheSize, CmsCmManager cmMgr) {
        this.cacheEnabled = cacheEnabled;
        this.cmManager = cmMgr;
        this.lastUpdatedTs = 0;

        // Initialize the var cache if it's enabled.
        if (cacheEnabled) {
            logger.info("Creating var cache with TTL: " + ttl + "sec and cacheSize: " + cacheSize);
            this.varCache = CacheBuilder.newBuilder()
                    .maximumSize(cacheSize)
                    .expireAfterWrite(ttl, SECONDS)
                    .initialCapacity(1)
                    .build(new CacheLoader<String, Long>() {
                        @Override
                        public Long load(String key) throws Exception {
                            CmsVar cacheStatus = cmManager.getCmSimpleVar(key);
                            if (cacheStatus != null) {
                                return parseLong(cacheStatus.getValue());
                            }
                            return 0L;
                        }
                    });
        } else {
            logger.warn("Filter/MdCache cache is disabled.");
        }
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing cache filter.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (cacheEnabled && varCache != null) {
            long updateTs = varCache.getUnchecked(MD_CACHE_STATUS_VAR);
            if (updateTs > lastUpdatedTs) {
                lastUpdatedTs = updateTs;
                onUpdate(MD_CACHE_STATUS_VAR);
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("Shutting down the cache filter.");
    }

    /**
     * Calls for any var cache status update when executing this filter.
     *
     * @param varName cache status variable name.
     */
    public abstract void onUpdate(String varName);

}
