Exercise 1
==========

Set-up your local environment to record and replay a specific gatling load simulation.

Setup
--------
- Install Java JDK 8
- Install Scala SDK
- Install IntelliJ Community Edition
- Install IntelliJ Scala Support
- Import this module

IntelliJ:
 - Add Framework Support... > Scala 

Optional: Download Gatling

Record
------
- Start the Gatling recorder
   - via commandline or via maven plugin
   - configure Proxy and SUT ports
- Option 1: Configure your browser to use the proxy (port as configured before)
- Start the Boutique Bootstrap
  - Start the DB Server
  - Start the WebServer
- Record a simple script
- Option 2: Store the HAL transcript from the browser and use recorder in HAL mode

Replay
------
- Open the recorded simulation in IntelliJ
- Make modifications (e.g. add some pause, inject more users) if necessary
- Run the simulation using maven (adapt the pom.xml if necessary)

Reports
------
- Have a look at the reports. What can you get from them?