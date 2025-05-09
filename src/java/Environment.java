/*
 * Class that defines the environment.
 * 
 * Written by James P. Biagioni (jbiagi1@uic.edu)
 * for CS511 Artificial Intelligence II
 * at The University of Illinois at Chicago
 * 
 * Last modified 2/19/07 
 * 
 * DISCLAIMER:
 * Elements of this application were borrowed from
 * the client-server implementation of the Wumpus
 * World Simulator written by Kruti Mehta at
 * The University of Texas at Arlington.
 * 
 */

import java.io.BufferedWriter;

class Environment {
	private final char[][][] wumpusWorld;
	private final char[][][] percepts;	
	private final int worldSize;
	private final String bar;
	
	private Agent agent;
	private final BufferedWriter outputWriter;
	
	private final int[] prevAgentPosition;
	private boolean bump;
	private boolean scream;
	
	public Environment(int size, char[][][] world, BufferedWriter outWriter) {
		worldSize = size;
		
		wumpusWorld = new char[worldSize][worldSize][4];
		percepts = new char[worldSize][worldSize][4];
		outputWriter = outWriter;
		
		prevAgentPosition = getAgentLocation();
		
		bump = false;
		scream = false;
		
		// store world definition
		for (int i = 0; i < worldSize; i++) {
			for (int j = 0; j < worldSize; j++) {
                System.arraycopy(world[i][j], 0, wumpusWorld[i][j], 0, 4);
			}
		}
		
		// initialize percept map
		for (int i = 0; i < worldSize; i++) {
			for (int j = 0; j < worldSize; j++) {
				for (int k = 0; k < 4; k++) {
					percepts[i][j][k] = ' ';
				}
			}
		}
		
		setPerceptMap();
		
		// create divider bar for display output
//		bar = "-".repeat((6 * worldSize) - 1);
		bar = "-----+".repeat(worldSize);
	}
	
	public int getWorldSize() {
		return worldSize;
	}
	
	public char getAgentDirection() {
		for (int i = 0; i < worldSize; i++) {
			for (int j = 0; j < worldSize; j++) {
				if (wumpusWorld[i][j][3] == 'A') return 'N';
				if (wumpusWorld[i][j][3] == '>') return 'E';
				if (wumpusWorld[i][j][3] == 'V') return 'S';
				if (wumpusWorld[i][j][3] == '<') return 'W';
			}
		}
		return '@';
	}
	
	public int[] getAgentLocation() {
		int[] agentPos = new int[2];
		
		for (int i = 0; i < worldSize; i++) {
			for (int j = 0; j < worldSize; j++) {
				if (wumpusWorld[i][j][3] != ' ') {
					agentPos[0] = i;
					agentPos[1] = j;
				}
			}
		}
		
		return agentPos;
	}
	
	public void placeAgent(Agent theAgent) {
		wumpusWorld[prevAgentPosition[0]][prevAgentPosition[1]][3] = ' ';
		
		agent = theAgent;
		wumpusWorld[agent.getLocation()[0]][agent.getLocation()[1]][3] = agent.getAgentIcon();
		
		prevAgentPosition[0] = agent.getLocation()[0];
		prevAgentPosition[1] = agent.getLocation()[1];
	}
	
	public void setBump(boolean bumped) {
		bump = bumped;
	}
	
	public boolean getBump() {
		return bump;
	}
	
	public void setScream(boolean screamed) {
		scream = screamed;
	}
	
	public boolean getScream() {
		return scream;
	}
	
	public boolean getBreeze() {
        return percepts[agent.getLocation()[0]][agent.getLocation()[1]][0] == 'B';
	}
	
	public boolean getStench() {
        return percepts[agent.getLocation()[0]][agent.getLocation()[1]][1] == 'S';
	}
	
	public boolean getGlitter() {
        return percepts[agent.getLocation()[0]][agent.getLocation()[1]][2] == 'G';
	}

	public boolean grabGold() {
		if (percepts[agent.getLocation()[0]][agent.getLocation()[1]][2] == 'G') {
			percepts[agent.getLocation()[0]][agent.getLocation()[1]][2] = ' ';
			wumpusWorld[agent.getLocation()[0]][agent.getLocation()[1]][2] = ' ';
			return true;
		}
		return false;
	}
	
	public boolean checkDeath() {
		if (wumpusWorld[agent.getLocation()[0]][agent.getLocation()[1]][0] == 'P') return true;
		else return wumpusWorld[agent.getLocation()[0]][agent.getLocation()[1]][1] == 'W';
    }

	private void updatePerceptsOnWumpusHit(int x, int y) {
		if (x-1 >= 0) percepts[x-1][y][1] = ' ';
		if (x+1 < worldSize) percepts[x+1][y][1] = ' ';
		if (y-1 >= 0) percepts[x][y-1][1] = ' ';
		if (y+1 < worldSize) percepts[x][y+1][1] = ' ';
		
		// printPercepts()
	}
	
	private boolean wumpusHitAlongX(int i) {
		if (wumpusWorld[agent.getLocation()[0]][i][1] == 'W') {
			wumpusWorld[agent.getLocation()[0]][i][1] = '*';
			updatePerceptsOnWumpusHit(agent.getLocation()[0], i);
			return true;
		}
		return false;
	}

	private boolean wumpusHitAlongY(int i) {
		if (wumpusWorld[i][agent.getLocation()[1]][1] == 'W') {
			wumpusWorld[i][agent.getLocation()[1]][1] = '*';
			updatePerceptsOnWumpusHit(i, agent.getLocation()[1]);
			return true;
		}
		return false;
	}
	
	public boolean shootArrow() {
		if (agent.getDirection() == 'N') {
			for (int i = agent.getLocation()[0]; i < worldSize; i++) {
				if (wumpusHitAlongY(i)) return true;
			}
		} else if (agent.getDirection() == 'E') {
			for (int i = agent.getLocation()[1]; i < worldSize; i++) {
				if (wumpusHitAlongX(i)) return true;
			}
		} else if (agent.getDirection() == 'S') {
			for (int i = agent.getLocation()[0]; i >= 0; i--) {
				if (wumpusHitAlongY(i)) return true;
			}
		} else if (agent.getDirection() == 'W') {
			for (int i = agent.getLocation()[1]; i >= 0; i--) {
				if (wumpusHitAlongX(i)) return true;
			}
		}
		return false;
	}

	public void setPerceptMap() {
		// World: Pit,Wumpus,Gold,Agent
		// Percepts: Breeze,Stench,Glitter,Scream
		for (int i = 0; i < worldSize; i++) {
			for (int j = 0; j < worldSize; j++) {
				for (int k = 0; k < 4; k++) {
					if (wumpusWorld[i][j][k] == 'P') {
						if (j-1 >= 0) percepts[i][j-1][k] = 'B';
						if (i+1 < worldSize) percepts[i+1][j][k] = 'B';
						if (j+1 < worldSize) percepts[i][j+1][k] = 'B';
						if (i-1 >= 0) percepts[i-1][j][k] = 'B';
					} else if (wumpusWorld[i][j][k] == 'W') {
						if (j-1 >= 0) percepts[i][j-1][k] = 'S';
						if (i+1 < worldSize) percepts[i+1][j][k] = 'S';
						if (j+1 < worldSize) percepts[i][j+1][k] = 'S';
						if (i-1 >= 0) percepts[i-1][j][k] = 'S';
					} else if (wumpusWorld[i][j][k] == 'G') percepts[i][j][k] = 'G';
				}
			}
		}
	}
	
	public void printPercepts() {
		//System.out.println(" -----------------------");
		System.out.println(" " + bar);
		
		for (int i = worldSize-1; i > -1; i--) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < worldSize; k++) {
					if (j == 0) {
						System.out.print("| " + percepts[i][k][0] + " " + percepts[i][k][1] + " ");	
					} else {
						System.out.print("| " + percepts[i][k][2] + " " + percepts[i][k][3] + " ");
					}
					
					if (k == worldSize-1) {
						System.out.print("|");
					}
				}
				System.out.print("\n");
			}
			//System.out.println(" -----------------------");
			System.out.println(" " + bar);
		}
		System.out.print("\n");
	}
	
	public void printEnvironment() {
		//  +-----+-----+-----+-----+
		//  | P W | P W | P W | P W |
 		//  | G A | G A | G A | G A |
		//  +-----+-----+-----+-----+
		//  | P W | P W | P W | P W |
 		//  | G A | G A | G A | G A |
		//  +-----+-----+-----+-----+
		//  | P W | P W | P W | P W |
 		//  | G A | G A | G A | G A |
		//  +-----+-----+-----+-----+ 23
		//  | P W | P W | P W | P W | A A |
 		//  | G A | G A | G A | G A | A A |
		//  +-----+-----+-----+-----+-----+ 29
		//
		// P,W,G,A
		try {
			//System.out.println("\n -----------------------");
			//outputWriter.write("\n -----------------------" + "\n");
			System.out.println("\n+" + bar); // added a plus @ start
			outputWriter.write("\n+" + bar + "\n"); // added a plus @ start 
			
			for (int i = worldSize-1; i > -1; i--) {
				for (int j = 0; j < 2; j++) {
					for (int k = 0; k < worldSize; k++) {
						if (j == 0) {
							System.out.print("| " + wumpusWorld[i][k][0] + " " + wumpusWorld[i][k][1] + " ");
							outputWriter.write("| " + wumpusWorld[i][k][0] + " " + wumpusWorld[i][k][1] + " ");
						} else {
							System.out.print("| " + wumpusWorld[i][k][2] + " " + wumpusWorld[i][k][3] + " ");
							outputWriter.write("| " + wumpusWorld[i][k][2] + " " + wumpusWorld[i][k][3] + " ");
						}
						
						if (k == worldSize-1) {
							System.out.print("|");
							outputWriter.write("|");
						}
					}
					System.out.print("\n");
					outputWriter.write("\n");
				}
				//System.out.println(" -----------------------");
				//outputWriter.write(" -----------------------" + "\n");
				System.out.println("+" + bar); // added a plus @ start
				outputWriter.write("+" + bar + "\n"); // added a plus @ start
			}
			System.out.print("\n");
			outputWriter.write("\n");
		}
		catch (Exception e) {
			System.out.println("An exception was thrown: " + e);
		}
	}
}