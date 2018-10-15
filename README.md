# Boutique-de-jus

Is a demo application with several performance flaws and issues intentionally built-in in order to 
practice several techniques in the Java Performance world (Performance Testing, - analysis, tuning, monitoring).
 
The idea is derived from the [OWASP Juice Shop](https://www.owasp.org/index.php/OWASP_Juice_Shop_Project) which 
  is an intentionally insecure webapp for security trainings.
  
Same as the OWASP Juice Shop the name is a literal translation of the German term _Saftladen_, which is a really
badly run place (i.e. a shop, a bar, an office), where nothing really works and everything sucks, a.k.a a dump.

# Setup

## Requirements

- IntelliJ with the Scala support installed (be sure to have the latest version, especially when running on JDK 11)
- OR: JDK8 or JDK11, Scala 2.12.8, Maven 3.5.4 (best to have these in addition to IntelliJ)
- a JVM Monitoring solution (such as VisualVM 1.4.2)

## Build the workshop

    mvn clean install
    
## Start the server via bootstrap

    mvn exec:exec -pl bdj-bootstrap

For Maven offline mode - if you don't have a good network connection and prepared a local
repository, add the ``-o`` argument to the ``mvn`` command.

To use a local repository use the `-Dmaven.repo.local=PathToLocalDir` system property.

For example, building offline from a local repo:

    mvn -Dmaven.repo.local=/local-repo -o clean install
    mvn -Dmaven.repo.local=/local-repo -o exec:exec
 

# Architecture

Given the multitude of problems, the bdj has to consist of multiple microservices (is a thing right now anyway, eh?).
To make things a bit more manageable, there will be a boot strapper, that starts the various (native) processes.
So it's easier to keep track of changes and modify during runtime. 

The following technologies / frameworks will be used to implement the microservices.

The entire shop won't use any Client-side JS technology, the focus is purely on Java. Therefore the shop
uses plain html+css, and a bit of javascript for ajax calls, but thats it. The pages are rendered on server
side. One of the intentions is to have fun with java during the development of the Boutique and be a 
JavaScript FullStick-developer (only stick, no carrots) :)

Frontend:
 - Struts-based MVC running on embedded Jetty
Backend:
 - Product-catalog-Service
    - file storage 
    - vert.x service (async, event loop)
    - HTTP endpoint
 - Order-Service
    - DB storeage, JDBC
    - integration via HTTP or RMI?
 - Help Service
    - File Storage
    - HTTP endpoint
 - User Service
    - File based service
    - thread based

# Bug ideas

- resilience flaws
  - missing circuit break on (randomly) disfunctional backend service
  - missing bulkhead, same DB pool for different service
  - unbalanced capacity, too many front end threads, too few backend threads
- implementation / design flaws
  - service factory anti-pattern (use services for every call 1000x and keep the instance in a soft-reference and recreate synchronously)
  - writing your own logger (using a shared StringBuffer)
  - resource leak
  - too large page responses (no paging, rending large lists)
  - using a HashMap in a thread-shared environment (i.e. in a session when using ajax)
- GC: misconfigured GC defaults
  - too small YG
  - too small Heap
  - too small thread stack
  - too few GC threads
  - too many GC threads
  - too many dynamic classes (MetaSpace grow), i.e. dynamic classes / scripting proxies
- JIT: 
  - disabled JIT
  - too small code cache
  - too many deoptimizations
- Configuration flaws 
  - too few front end threads
- Memory Management flaws
  - loading entire db into memory causing a OO (i.e. the "help" function)
  - large user sessions
  - high object allocation rate
  - allocate off-heap memory and dont' free it
- Asnyc processing flaws
  - blocking an event loop with too much computation
  - no backpressure on RxStreams
  - CompositeFuture without timeout (and one doesn't finish) 
- System interactions flaws
  - too much io (mulitple random disk seeks)
  - too much context switches (overparallelize computation in a thread pool)
- Advanced stealth modes:
  - change behavior when profiler (jms) is connected
  - change behavior when debugger is connected
