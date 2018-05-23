The Boutique-de-Jus web-shop
============================

This web application contains the front of the boutique web-shop and most
of the application logic. It'll use some backend services (where we can hide
additional bugs).

Use Cases
--------------

The shop implements the following use cases (for keeping track during development)

- Welcome Page
- Login using an property-file based authentication
  - see login.conf for JAAS LoginModule configuration
  - see users.property for user-repository data
  - see jetty.xml for web server security configuration
  - see web.xml for webb app security configuration
- Registration for new users 
