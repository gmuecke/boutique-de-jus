Exercise 2 (Model User Scenarios)
==========
(Test Mission2 given to you by using the CCD IS E(M)ARI heuristic)

Context
---------------
The juice shop / We wanna go online as soon as possible! / Do we have performance issues that could hinder us from doing so?

Criterias
---------------
- Can you provide us with 3 user scenarios which cover the most critical flows of the juice shop?
- Are the scripts executable on your machine?
- Can you even make a percentage split of the users?

Design
---------------
Identify and model three different user scenarios. Maybe you wanna document them somewhere ;)

Install (Setup)
---------------
Already done in excercise 1

Script
---------------
- Record each user scenario separately
- Create a Scala Object `Pages`
    - add the `http()` pages to the `Pages` object
- Create a Scala Object `Scenarios`
    - define the workflow chains (`exec(page1).exec(page2)`) to the `Scenarios` object
- Create a Simulation
    - define a Scenario (`scenario()`) that combines all the three workflows and make a percentage split

Execute and Monitor
---------------
TODO

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
We're not done yet, there is one more Test Missions to go.