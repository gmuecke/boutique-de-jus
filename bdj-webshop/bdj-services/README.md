This module contains business services for the web shop. Services are accessed through an interface.
The web application obtains the services via JNDI lookup. The services are created either by the
 web server or by the ServiceInitializer that creates and binds the services during web application
 initialization. Using this mechanism, various performance characteristics can be injected into the
 application.
 

