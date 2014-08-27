/*
 * ServiceKey.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.topology;

/**
 *
 * @author eyasukoc
 */
public class ServiceKey {
    
    private final String service;                                                                                                                                                                                                            
    private final String variant;
    private final String location;

    public static ServiceKey getServiceKey(String service, String variant, String location) { //better double check this thing
        return new ServiceKey(service,variant,location);
    }

    public ServiceKey(String service, String variant, String location) {
        this.service = service;
        this.variant = variant;
        this.location = location;
    }

    public String getService() {
        return service;
    }

    public String getVariant() {
        return variant;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.service != null ? this.service.hashCode() : 0);
        hash = 59 * hash + (this.variant != null ? this.variant.hashCode() : 0);
        hash = 59 * hash + (this.location != null ? this.location.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServiceKey other = (ServiceKey) obj;
        if ((this.service == null) ? (other.service != null) : !this.service.equals(other.service)) {
            return false;
        }
        if ((this.variant == null) ? (other.variant != null) : !this.variant.equals(other.variant)) {
            return false;
        }
        if ((this.location == null) ? (other.location != null) : !this.location.equals(other.location)) {
            return false;
        }
        return true;
    }

}
