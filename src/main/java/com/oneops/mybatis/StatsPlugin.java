package com.oneops.mybatis;


import com.oneops.mybatis.action.Action;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component("statsplugin")
@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}), @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class StatsPlugin implements Interceptor, DisposableBean{
    private Log logger = LogFactory.getLog(StatsPlugin.class);
    private Map<String, Stats> map = new HashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
 

    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = invocation.proceed();
        try {
            String id = ((MappedStatement) invocation.getArgs()[0]).getId();
            map.computeIfAbsent(id, t -> new Stats()).addTime(System.currentTimeMillis() - start);
        } catch (Exception e){
            logger.warn(e, e);
        }
        return proceed;
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }


    @PreDestroy
    public void destroy() throws Exception {
        scheduler.shutdown();
    }


    public void setProperties(Properties properties) {
        long interval = Long.parseLong(properties.getProperty("interval", "60"));

        try {
            final Action action = (Action) (Class.forName(properties.getProperty("action", "com.oneops.mybatis.action.LoggerAction")).newInstance());
            action.setProperties(properties);
            scheduler.scheduleAtFixedRate(() -> {
                action.process(map);
                map.clear();
            }, interval, interval, TimeUnit.SECONDS);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            logger.error("Error instantiating logger action", e);
            e.printStackTrace();
        }
    }
}
