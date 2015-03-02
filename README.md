# Louie
Louie is a service networking framework designed to minimize the effort needed to efficiently access information from varying sources and locations.  Services are constructed in modular layers that allow for an adaptable network that
scales as needed. Service clients are automatically generated for various languages, allowing a single server to feed data to all of your applications with a unified API.

## Why
Louie was created to solve a few critical issues faced by Rhythm & Hues' international visual effects pipeline. Among these were data locality, global consistency, and fast, multi-language client support. Louie leverages Google's Protocol Buffers as it's common objects, and by auto-generating corresponding clients in Python, you can create an efficient multi-language service-based API.

## Getting Started
* **Step 1:** [Read up](https://developers.google.com/protocol-buffers/docs/overview) and [install](https://developers.google.com/protocol-buffers/docs/downloads) Google's Protocol Buffers (protobuf)
* **Step 2:** Read up on [Louie](http://louie.rhythm.com)
* **Step 3:** Download and install [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* **Step 4:** Download and install [Maven](http://maven.apache.org/download.cgi)
* **Step 5:** Download and install [Netbeans with Glassfish](https://netbeans.org/downloads/)
* **Step 6:** Clone the repo, and then build it!
```
~/louie> mvn clean install 
```

