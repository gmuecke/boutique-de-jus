Exercise 1 (Getting started with Record & Replay)
==========
(Test Mission1 given to you by using the CCD IS EARI heuristic)

Context
---------------
The juice shop.
 We want to go online as soon as possible!
 Do we have performance issues that could hinder us from doing so?

Criterias
---------------
Set-up your local environment to record and replay a specific gatling load simulation.

Design
---------------
Record a first very simple flow for load testing.

Install (Setup)
---------------
- Install Java JDK 11
- Install Scala SDK
- Install IntelliJ Community Edition
- Install IntelliJ Scala Plugin (Preferences/Plugins/Install JetBrains plugin<... -> search for 'Scala')
- Install (unzip) VisualVM

- Unzip the boutique-de-jus-master.zip (this contains the sources)
- Unzip the local local-repo.zip (this contains all maven dependencies)
- Open IntelliJ and -> File/New/Project from existing sources...
- Go to Maven Projects menu (on the right navigation pane)
- Go to boutique-de-jus (root)/Lifecycle and run the maven command 'install'
- Go to bdj-boostrap/Plugins/exec and run exec:exec (to start the shop)
 - Start the Boutique Bootstrap
   - Start the DB Server
   - Start the WebServer
- Open a browser at http://localhost:8080

Script (Record)
---------------
- Start the Gatling recorder -> go to module getting-started/Plugins/gatling and run gatling:recorder
- Browser: configure Proxy and SUT ports
- Option 1: Configure your browser to use the proxy (port as configured before)
- Record a simple script
- Go to module perftest-gatling and right click on getting-started
 - Add Framework Support... > Scala
- Go to src/test and right click on scala
- (Option 2 for https: Store the HAL transcript from the browser and use recorder in HAL mode)

Execute and Monitor
---------------
- Open the recorded simulation in IntelliJ
- Make some modifications (e.g. add some pause, inject more users)
- Start VisualVM and monitor 
- Run the simulation using maven (adapt the pom.xml if necessary)

Analyze
---------------
Have a look at the gatling reports. What can you get from them?

Report
---------------
-
 
Iterate
---------------
We're not done yet, there are two more Test Missions to go.
