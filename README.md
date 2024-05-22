This is a fork of GAIGResearch/TableTopGames and is used for generating Dominion playtraces used in owenant/DominionPlayTraceClustering. 

In order to use this code, first follow the base instructions provided at GAIGResearch/TableTopGames to build TAG. Once the project has been built, tournaments can be executed by running the class DominionPlayTraceGenerator found in src/main/java/games/dominion/metrics.

The function runTournament() has been set-up to use specific TAG listeners needed to generate data for card count and N-Gram based playtraces. This function can also be altered to change the tournament parameters. In particular, different types of AI agent can be added to the agents list to change agents used in a tournament. Additionally, choosing between SD and FG1E kingdom card sets can be changed by providing different classes to create the params object (both possibilities are in the code). GamesPerMatchup can also be altered to change the total number of games played in the tournament.

