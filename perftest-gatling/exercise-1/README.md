Exercise 1 (Getting started with Record & Replay)
==========
(Test Mission1 given to you by using the CCD IS E(M)ARI heuristic)

Context
---------------
The juice shop / We wanna go online as soon as possible! / Do we have performance issue that could hinder us from doing so?

Criterias
---------------
Set-up your local environment to record and replay a specific gatling load simulation.

Design
---------------
Record a first very simple flow for load testing.

Install (Setup)
---------------
- Install Java JDK 8 (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- Install Scala SDK (https://www.scala-lang.org/download)
- Install IntelliJ Community Edition (https://www.jetbrains.com/idea/download)
- Install IntelliJ Scala Plugin (Preferences/Plugins/Install JetBrains plugin... -> search for 'Scala')
- Import this module
- Install VisualVM (https://visualvm.github.io)

IntelliJ:
 - Add Framework Support... > Scala 

Optional: Download Gatling

Script (Record)
---------------
- Start the Gatling recorder
   - via commandline or via maven plugin
   - configure Proxy and SUT ports
- Option 1: Configure your browser to use the proxy (port as configured before)
- Start the Boutique Bootstrap
  - Start the DB Server
  - Start the WebServer
- Record a simple script
- Option 2: Store the HAL transcript from the browser and use recorder in HAL mode

Execute and Monitor
---------------
- Open the recorded simulation in IntelliJ
- Make some modifications (e.g. add some pause, inject more users)
- Start VisualVM and monitor 
- Run the simulation using maven (adapt the pom.xml if necessary)

Analyze
---------------
Analyze the test results in the team and come to a conclusion what the facts are.

Report
---------------
Prepare for a short debriefing by using the PROOF heuristic:
- Past: What have you recorded and why?
- Results: Have a look at the generated Gatling reports. What can you get from them?
- Obstacles: Anything that got in your way while testing? 
- Outlook: Is there anything left that you think needs to be done before doing an next iteration?
- Feelings: How do you feel regarding your performance test-session? Are you satisfied or would you change something before doing an other iteration?

Iterate
---------------
We're not done yet, there are two more Test Missions to go.