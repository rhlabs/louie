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

/**
 *
 * @author cjohnson
 */
public class MessageContext {
  final private static MessageContext SINGLETON = new MessageContext();
  
  ThreadLocal<MessageInfo> message;
  
  private MessageContext() {
      message = new ThreadLocal<MessageInfo>();
  }
  
  public static MessageInfo getMessage() {
      return SINGLETON.message.get();
  }
    
  public static void setMessage(MessageInfo msg) {
      SINGLETON.message.set(msg);
  }
  
  public static void clearMessage() {
      SINGLETON.message.remove();
  }
}
