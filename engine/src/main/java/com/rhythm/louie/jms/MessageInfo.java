/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
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
 */
package com.rhythm.louie.jms;

import com.rhythm.louie.jms.JmsProtos.*;

/**
 *
 * @author cjohnson
 */
public class MessageInfo {
    private final MessageBPB bundle;
    private final MessageAction action;
    private int contextIndex = -1;
    
    public MessageInfo(MessageBPB bundle) {
        this.bundle = bundle;
        action = MessageAction.typeFromAction(bundle.getAction());
    }
    
    public MessageBPB getBundle() {
        return bundle;
    }
    
    public MessageAction getAction() {
        return action;
    }
    
    public int getContentCount() {
        return bundle.getContentCount();
    }
    
    public int getContentIndex() {
        return contextIndex;
    }
    
    public boolean hasMoreContent() {
        return bundle.getContentCount()>(contextIndex+1);
    }
    
    public ContentPB getNextContent() throws Exception {
        return bundle.getContent(++contextIndex);
    }

    public ContentPB getCurrentContent() throws Exception {
        return bundle.getContent(contextIndex);
    }
}
