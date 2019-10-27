# optimal-lineup

Finds the best fantasy football weekly lineup based on salary constraints and projections.

## Beginning of a new season

* Set up the database using the Rails project (ff-draft). Create a symbolic link to the database.
* Update the database to create all the needed tables using the REPL
```
(lineup.services.schedule/populate-schedule)
(lineup.services.schedule/populate-teams)
```

## Weekly

* Find a FanDuel contest that includes all the games for the week to capture salaries.
* Download that as a CSV and then use the rails project to populate the salary information.
* Then use the REPL in this project to find the optimal lineups.
* Use the comment block at the bottom of lineup.services.team to get the best lineups for each game type.
