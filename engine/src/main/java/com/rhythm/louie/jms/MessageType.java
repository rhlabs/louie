/*
 * MessageType.java
 * 
 * Copyright (c) 2012 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.jms;

/**
 *
 * @author sfong
 */
public enum MessageType {
    DDR("type LIKE '%rh.pb.ddr.%'"),
    EMPLOYEE("type LIKE '%rh.pb.employee.%'"),
    SCENE("type LIKE '%rh.pb.scene.%'"),
    STAGE("type LIKE '%rh.pb.stage.%'"),
    JOB("type LIKE '%rh.pb.job.%'"),
    SETDATA("type LIKE '%rh.pb.setdata.%'");
    
    private final String clause;
    
    MessageType(String clause) {
        this.clause = clause;
    }
    
    public String getClause() {
        return clause;
    }
    
    @Override
    public String toString() {
        return clause;
    }
}
