Exercise 3 (Load Modelling)
==========
(Test Mission3 given to you by using the CCD IS E(M)ARI heuristic)

Context
---------------
The juice shop
 We want to go online as soon as possible! 
 Do we have performance issues that could hinder us from doing so?

Criterias
---------------
Can you provide us with the following information:
- Can we handle a total of 100k user/h, where we have a conversion rate (submitted orders) of 10%?
- Can we achieve an overall response time per page of 1.5s - 4s?
- Can you give us any recommendations regarding capacity? And why?

Design
---------------
Model a load profile that supports testing our business requirements.

Install (Setup)
---------------
Already done in exercise 1

Script
---------------
- Reuse your scenarios from exercise 2 (or use the ones provided)
- Inject load
    
Execute and Monitor
---------------
- Run the scenarios and monitor the application with VisualVM

Analyze
---------------
Analyze the test results in the team and gather your toughts what's happening here.

Report
---------------
Prepare for a short debriefing by using the PROOF heuristic:
- Past: What have you recorded and why?
- Results: Have a look at the generated Gatling reports. What can you get from them?
- Obstacles: Anything that got in your way while testing? 
- Outlook: Is there anything left that you think needs to be done before doing an next iteration?
- Feelings: How do you feel regarding your performance test-session? Are you satisfied or would you change something before doing an other iteration?
