Exercise 1
==========

Set-up your local environment to record and replay gatling load simulations.

Setup
--------

- Install Java JDK 8
- Install Scala SDK
- Install IntelliJ Community Edition
- Install IntelliJ Scala Support
- Import this module

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

- Open the recorded simulation in IntellJ
- make modifications if necessary
- run the simulation using maven (adapt the pom.xml if necessary)
