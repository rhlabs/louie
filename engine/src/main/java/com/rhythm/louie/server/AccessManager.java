/*
 * AccessManager.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.server;

import java.util.*;

import org.jdom2.Element;

/**
 *
 * @author eyasukoc
 */
public class AccessManager {

    private static final Set<String> adminUsers = new HashSet<>();
    private static final Map<String,Set<String>> groupUsers = new HashMap<>(); // group : list of users
    private static final Map<String,Set<String>> serviceUsers = new HashMap<>();// service : list of users (superset of groupUsers sets)
    
    private static final String GROUP_NAME = "name";
    private static final String ADMIN = "admin";
    private static final String WILDCARD = "%";
    
    private static boolean wild = false;
    
    protected static void loadGroups(Element groups) {
        for (Element group : groups.getChildren()) {
            String name = group.getAttributeValue(GROUP_NAME).toLowerCase();
            if (ADMIN.equals(name)) {
                adminUsers.clear(); //to allow re-loading from a default set by a specific louie.xml impl
                wild = false;
                for (Element user : group.getChildren()) {
                    String u = user.getTextTrim();
                    if (WILDCARD.equals(u)) {
                        wild = true;
                        break;
                    }
                    adminUsers.add(user.getTextTrim());
                }
            } else {
                Set<String> users = new HashSet<>();
                for (Element user : group.getChildren()) {
                    users.add(user.getTextTrim());
                }
                groupUsers.put(name,users);
            }
        }
    }
    
    /**
     * Requires loadGroups has already been executed!
     * @param service
     * @param serviceGroup 
     */
    protected static void loadServiceAccess(String service, Element serviceGroup) {
        Set<String> superset = new HashSet<>();
        for (Element child : serviceGroup.getChildren()) {
            String group = child.getTextTrim().toLowerCase();
            Set<String> users = groupUsers.get(group);
            if (users != null) {
                superset.addAll(users);
            }
        }
        serviceUsers.put(service, superset);
    }
    
    public static boolean isAdminUser(String user) {
        if (wild == true) {
            return true;
        }
        return adminUsers.contains(user);
    }
    
    public static boolean isUserInGroup(String user, String group) {
        Set<String> users = groupUsers.get(group.toLowerCase());
        if (users != null) {
            return users.contains(user);
        }
        return false;
    }
    
    /**
     * Returns accessibility based on restricted methods for a given service.
     * HOWEVER! If a method is configured as restricted but no groups/users config is provided for 
     * that service, the relevant methods will be inaccessible to everyone!
     * Admin users can also access all restricted methods.
     * @param user The user requesting to use the method
     * @param service The service providing the relevant method
     * @return true if accessible, false if not, or missing configs
     */
    public static boolean canUserAccessService(String user, String service) {
        Set<String> users = serviceUsers.get(service);
        if (users != null) {
            return users.contains(user);
        } 
        return adminUsers.contains(user);
    }
    
}
