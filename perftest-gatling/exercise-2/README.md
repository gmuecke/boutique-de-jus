Exercise 2
==========

Model different user scenarios.

- Identify three different user workflows in the application
- Record each flow separately
- Create a Scala Object `Pages`
    - add the `http()` pages to the `Pages` object
- Create a Scala Object `Scenarios`
    - define the workflow chains (`exec(page1).exec(page2)`) to the `Scenarios` object
- Create a Simulation
    - define a Scenario (`scenario()`) that combines all the three workflows and make a percentage split