GoGameServer
============
This is a game platform which acts like a coordinator who assigns connected clients to a particular game. The go Game can only played by two players, but it can be watched by multiple clients. 
What is Go Game: the game is played by two players who alternatively place black and white stones.  The object of the game is to use one’s stones to surround a larger portion of the board than the opponent. Let’s say a block of black stones are surrounds by white stones, then these block of stones are captured. The white player gets points.

============
Instruction to run
============
1. Run the GameServer: java GameServer ipAddress port# name
============
2. Run humanPlayer, machinePlayer, or observer:
============
  java HumanPlayer/MachinePlayer/Observer ipAddressOfServer port#OfServer name
============
  For example: java HumanPlayer 192.168.1.100 8888 Andrew
============