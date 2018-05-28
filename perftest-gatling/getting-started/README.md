Getting Started
==========

Context
---------------
The juice shop 

We wanna go online as soon as possible! 

Do we have performance issues that could hinder us from doing so?

Criterias
---------------
Set-up your local environment to record and replay a specific gatling load simulation.

Design
---------------
Record a first very simple flow for load testing.

Install (Setup)
---------------
- Start the Boutique Bootstrap
   - Start the DB Server (first tab)
   - Start the WebServer (second tab)
- Open a browser at http://localhost:8080
- Start the Recorder:
    - Menu: Right-Hand-Side/Maven Project (Alt+9)
    - getting-started > Plugins > gatling > gatling:recorder 

Script (Record)
---------------
- In the Browser's developer tools, select the network view
- clear the history
- open the web shop page
- right click on the request log, save as HAR
- open HAR file with Gatling recorder
- save as Simulation

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
- what are your findings?
 
Iterate
---------------
- extend the scenario until you have sufficient coverage 
