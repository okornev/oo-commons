package com.oneops.mybatis.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggerAction extends JSONStringAction{
    private Log logger = LogFactory.getLog("com.oneops.mybatis.StatsPlugin");

    @Override
    void process(String jsonString) {
        logger.info(jsonString);
    }
}
