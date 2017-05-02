package com.oneops.mybatis.action;

import com.oneops.mybatis.Stats;

import java.util.Map;
import java.util.Properties;

public abstract class JSONStringAction implements Action {
    abstract void process(String jsonString);

    @Override
    public void process(Map<String, Stats> data) {
        StringBuilder builder = new StringBuilder("{");

        for (String key:data.keySet()){
            if (builder.length()>1){
                builder.append(",");
            }
            builder.append("\"").append(key).append("\":").append(data.get(key));
        }
        builder.append("}");
        process(builder.toString());
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
