Exercise 3 (Load Modelling)
==========
(Test Mission3 given to you by using the CCD IS E(M)ARI heuristic)

Context
---------------
The juice shop / We wanna go online as soon as possible! / Do we have performance issues that could hinder us from doing so?

Criterias
---------------
Can you provide us with the following information:
- Can we handle a total distribution of 100k user/h, where we have a success rate (submitted orders) of 10%?
- Can we achieve a turnover rate of 500k$/day (15Mio$/month)?
- Can we achieve an overall response time per page of 1.5s - 4s?

Design
---------------
Model your new/existing test-scenarios by thinking of:
- How can we test for this business criterias?
- TODO

Maybe you wanna document them somewhere ;)

Install (Setup)
---------------
Already done in excercise 1

Script
---------------
- Reuse one scenario from exercise 2 (or use the ones provided)
- Inject load
    - Closed loop (numbers of users, using "during")
    - Open loop (constant rate)
    
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