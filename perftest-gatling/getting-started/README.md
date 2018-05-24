Getting Started
==========

Context
---------------
The juice shop / We wanna go online as soon as possible! / Do we have performance issues that could hinder us from doing so?

Criterias
---------------
Set-up your local environment to record and replay a specific gatling load simulation.

Design
---------------
Record a first very simple flow for load testing.

Install (Setup)
---------------
- Start the Boutique Bootstrap
   - Start the DB Server
   - Start the WebServer
- Open a browser at http://localhost:8080
- Start the Recorder:
    - RHS Maven Project (Alt+9)
    - getting-started > Plugins > gatling > gatling:recorder 

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
