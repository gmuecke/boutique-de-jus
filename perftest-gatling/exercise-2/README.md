Exercise 2
==========
(Test Mission2 given to you by using the CCD IS E(M)ARI heuristic)

Context
---------------
The juice shop 
We want to go online as soon as possible! 
Do we have performance issues that could hinder us from doing so?

Criterias
---------------
- Can you provide us with at least two user scenarios which cover the most critical flows (hint: consider
 our business goals) of the juice shop?
- What are the response times for the different pages (load: 1user)?

Design
---------------
Identify and model at least two different user scenarios. Maybe you wanna document them somewhere ;-)

Install (Setup)
---------------
Already done in exercise 1

Script
---------------
- Record each user scenario separately (recommended)
- Create a Scala Object `Pages`
    - add the `http()` pages to the `Pages` object
- Create a Scala Object `Scenarios`
    - define the workflow chains (`exec(page1).exec(page2)`) to the `Scenarios` object
- Create a Simulation
    - define a Scenario (`scenario()`) that combines all your workflows and make a percentage split

Execute (only)
---------------
- Run the scenarios

Analyze
---------------
Have a look at the gatling reports for the reponse time.

Report
---------------
-

Iterate
---------------
We're not done yet, there is one more Test Missions to go.
