package eightPuzzle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

public class EightPuzzle {
	private static Random r = new Random();
	private static Scanner kb = new Scanner(System.in);
	
	// priority queues to store puzzles using h1 and h2 to compare
	private static Queue<Puzzle> queueH1;
	private static Queue<Puzzle> queueH2;

	// store explored puzzles
	private static Map<String, Integer> explored = new HashMap<String, Integer>();
	
	public static void main(String[] args) {
		System.out.println("This program will solve a 8-Puzzle by using an A* heuristic.\n");
		
		// create queues with associated comparators
		queueH1 = new PriorityQueue<>(priorityComparatorH1);
		queueH2 = new PriorityQueue<>(priorityComparatorH2);
		
		showMenu();
	}
	
	private static void showMenu(){
		String input;
		
		do{
			System.out.println("(1) Generate sample puzzle\n"
					 		 + "(2) Enter your own puzzle\n"
					 		 + "(3) Generate 100 puzzles and export result data to file\n"
					 		 + "(4) Exit\n"
					 		 + "Select option by number: ");
			input = kb.next();
			if(Pattern.matches("[1-3]", input)) {
				break;
			}else {
				System.out.println("Incorrect input. Enter a valid option.\n");
			}
		}while(true);
		System.out.println();
		switch(Integer.parseInt(input)){
			case 1:
				runSamplePuzzle();
				break;
			case 2:
				runUserPuzzle();
				break;
			case 3:
				runTestCases();
				break;
		}
		System.out.println();
		do{
			System.out.println("Enter 'X' to exit or 'O' for another option: ");
			input = kb.next();
			if(Pattern.matches("[XxOo]", input)) {
				break;
			}else {
				System.out.println("Incorrect input. Enter a valid option.");
			}
		}while(true);
		System.out.println();
		switch(input.charAt(0)){
			case 'X':
			case 'x':
				System.out.println("Thank you, come again.");
				break;
			case 'O':
			case 'o':
				showMenu();
				break;
		}
	}
	
	private static void runSamplePuzzle() {
		System.out.println("SAMPLE TEST CASE:\n");
		Puzzle puzzle = createSolvablePuzzle();
		
		// solve puzzle using h1
		System.out.println("Solving Puzzle using H1");
		Puzzle puzzleH1 = puzzle;
		printSolution(solvePuzzle(puzzleH1, queueH2, new int[1]));
		
		// solve puzzle using h2
		System.out.println("Solving Puzzle using H2");
		Puzzle puzzleH2 = puzzle;
		printSolution(solvePuzzle(puzzleH2, queueH2, new int[1]));
	}
	
	private static void runUserPuzzle() {
		// test user input of puzzle
		Puzzle puzzle, puzzleH1, puzzleH2;
		String loop;
		do {
			// create solvable puzzle
			do {
				System.out.println("Enter an 8-puzzle (every digit 0-8 in any order): ");
				String s;
				do{
					s = kb.next();
					String pattern = "[0-8]{9}";
					if(!Pattern.matches(pattern, s) || containsDuplicateTiles(s)) {
						System.out.println("Incorrect input. Please input a correct puzzle.");
					}else {
						break;
					}
				}while(true);
				int[] tiles = new int[9];
				for (int j = 0; j < tiles.length; j++) {
					tiles[j] = Character.getNumericValue(s.charAt(j));
				}
				puzzle = new Puzzle(tiles, 0);
				
				// ask user to re-enter a puzzle if puzzle is not solvable or already at goal state
				if(!puzzle.isSolvable()){
					System.out.println("Puzzle is not solvable. Please input another puzzle.");
				}else if(puzzle.isGoalState()) {
					System.out.println("Puzzle is already solved. Please input another puzzle.");
				}else {
					break;
				}
			}while(true);
			
			// solve using h1
			System.out.println("Solving Puzzle using H1");
			puzzleH1 = puzzle;
			printSolution(solvePuzzle(puzzleH1, queueH1, new int[1]));
			
			// solve using h2
			System.out.println("Solving Puzzle using H2");
			puzzleH2 = puzzle;
			printSolution(solvePuzzle(puzzleH2, queueH2, new int[1]));

			System.out.println("Another puzzle? (Y or N): ");
			loop = kb.next();
		} while (loop.equals("Y") || loop.equals("y"));
	}
	
	// solve puzzle using priority queue with heuristic and explored set
	private static Puzzle solvePuzzle(Puzzle puzzle, Queue<Puzzle> queue, int [] nodesGen) {
		Puzzle p = puzzle;
		explored.put(Arrays.toString(p.getTiles()), 0);
		do {
			nodesGen[0] += generateNextPuzzles(p, queue, explored);
			p = queue.poll();
		} while (!p.isGoalState());
		
		explored.clear();
		queue.clear();
		
		return p;
	}
	
	private static Puzzle createSolvablePuzzle() {
		Map<Integer, Integer> mapTiles = new HashMap<Integer, Integer>();
		int[] tiles;
		int newTile;
		Puzzle startPuzzle;
		do {
			tiles = new int[9];
			mapTiles.clear();
			
			//generate 9 distinct tiles
			for (int j = 0; j < tiles.length; j++) {
				do {
					newTile = r.nextInt(9);
				} while (mapTiles.containsKey(newTile));
				mapTiles.put(newTile, 1);
				tiles[j] = newTile;
			}
			startPuzzle = new Puzzle(tiles, 0);
			// loop if puzzle is solvable or already in goal state
		} while (!startPuzzle.isSolvable() || startPuzzle.isGoalState());
		
		return startPuzzle;
	}
	
	private static boolean containsDuplicateTiles(String s) {
		Map<Character,Integer> chars = new HashMap<>();
		for(int i=0;i<s.length();i++) {
			if(chars.containsKey(s.charAt(i))) {
				return true;
			}else {
				chars.put(s.charAt(i), 0);
			}
		}
		return false;
	}
	
	// run 100 test cases to output and output results to a file
	private static void runTestCases() {		
		// store count of solved depths, up to depth 30
		int[] depthH1 = new int[30];
		int[] depthH2 = new int[30];

		// store count of number of nodes generated at each depth
		int[] nodesGenH1 = new int[30];
		int[] nodesGenH2 = new int[30];
		
		// variables for calculating process runtime
		long startTime, endTime, totalTime;

		// store average solved runtimes
		float[] averageRuntimeH1 = new float[30];
		float[] averageRuntimeH2 = new float[30];
		
		int [] nodesGen = new int[1];
		
		Puzzle currentPuzzle, tempPuzzle, solvedPuzzle;
		
		System.out.println("Generating and solving 100 puzzles...");
		for (int testcase = 0; testcase < 100; testcase++) {
			if(testcase % 10 == 0) {
				System.out.println("...");
			}
			currentPuzzle = createSolvablePuzzle();
			
			// start record nodes and time
			nodesGen[0] = 0;
			startTime = System.nanoTime();
	
//			System.out.println("Solving Puzzle using H1");
			tempPuzzle = currentPuzzle;
			solvedPuzzle = solvePuzzle(tempPuzzle, queueH1, nodesGen);
//			printSolution(solvedPuzzle);
			
			// end record time
			endTime = System.nanoTime();
			totalTime = endTime - startTime;
			averageRuntimeH1[solvedPuzzle.getDepth() - 1] += totalTime;
			// end record depth
			depthH1[solvedPuzzle.getDepth() - 1]++;
			// end record nodes
			nodesGenH1[solvedPuzzle.getDepth() - 1] += nodesGen[0];
	
			// start record nodes and time
			nodesGen[0] = 0;
			startTime = System.nanoTime();
	
//			System.out.println("Solving Puzzle using H2");
			tempPuzzle = currentPuzzle;
			solvedPuzzle = solvePuzzle(tempPuzzle, queueH2, nodesGen);
//			printSolution(solvedPuzzle);
			
			// end record time
			endTime = System.nanoTime();
			totalTime = endTime - startTime;
			averageRuntimeH2[solvedPuzzle.getDepth() - 1] += totalTime;
			// end record depth
			depthH2[solvedPuzzle.getDepth() - 1]++;
			// end record nodes
			nodesGenH2[solvedPuzzle.getDepth() - 1] += nodesGen[0];
		}
		System.out.println("Puzzles solved.");
		System.out.println("Calculating output data...");
		// calculate average
		for (int i = 0; i < 30; i++) {
			if (depthH1[i] != 0) {
				averageRuntimeH1[i] /= depthH1[i];
				nodesGenH1[i] /= depthH1[i];
			}
			if (depthH2[i] != 0) {
				averageRuntimeH2[i] /= depthH2[i];
				nodesGenH2[i] /= depthH2[i];
			}
		}
		System.out.println("Calculations completed.");
		// load results into String
		String fileString = "";
		fileString += "Using A* Search Algorithm on an 8-Puzzle\n,# of Cases,,Nodes Generated,,Average Runtime,,\n";
		fileString += "Depth,H1,H2,H1,H2,H1,H2\n";
		for (int i = 0; i < 30; i++) {
			fileString += ((i + 1) + "," + depthH1[i] + "," + depthH2[i] + "," + nodesGenH1[i] + "," + nodesGenH2[i]
					+ "," + averageRuntimeH1[i] + "," + averageRuntimeH2[i] + "\n");
		}
		outputToFile(fileString);
	}
	
	// print solution of puzzle nodes starting at the solution leaf node and moving up to the root puzzle
	// root puzzle will have parent NULL
	private static void printSolution(Puzzle p) {
		int endDepth = p.getDepth();
		String puzzlesInOrder = "";
		do {
			if(p.getDepth() % 4 == 0) {
				puzzlesInOrder = "\nDepth" + p.getDepth() + "-" + p.getTileString() + " -> " + puzzlesInOrder;
			}else {
				puzzlesInOrder = "Depth" + p.getDepth() + "-" + p.getTileString() + " -> " + puzzlesInOrder;
			}
			p = p.getParent();
		} while (p != null);
		System.out.println(puzzlesInOrder + "Solved Puzzle at Depth " + endDepth + "\n");
	}

	private static void outputToFile(String fileString) {
		BufferedWriter bw = null;
		try {
			File file = new File(".\\output\\100TestCases.csv");

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			bw.write(fileString);
			String workingDir = System.getProperty("user.dir");
			System.out.println("Write to file successful at to "+workingDir+"\\output\\100TestCases.csv");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
			} catch (Exception ex) {
				System.out.println("Error in closing the BufferedWriter" + ex);
			}
		}
	}
	
	// puzzle comparator for heuristic 1
	private static Comparator<Puzzle> priorityComparatorH1 = new Comparator<Puzzle>() {
		public int compare(Puzzle p1, Puzzle p2) {
			return (int) ((p1.getDepth() + findH1(p1.getTiles())) - (p2.getDepth() + findH1(p2.getTiles())));
		}
	};
	
	// puzzle comparator for heuristic 2
	private static Comparator<Puzzle> priorityComparatorH2 = new Comparator<Puzzle>() {
		public int compare(Puzzle p1, Puzzle p2) {
			return (int) ((p1.getDepth() + findH2(p1.getTiles())) - (p2.getDepth() + findH2(p2.getTiles())));
		}
	};
	
	private static void generateNextHelper(int[][] newPuzzleTiles, int index, Puzzle parent, Map<String, Integer> explored,
			Queue<Puzzle> q) {
		Puzzle tempPuzzle = new Puzzle(newPuzzleTiles[index], parent.getDepth() + 1);
		tempPuzzle.setParent(parent);
		String puzzleAsString = Arrays.toString(tempPuzzle.getTiles());
		if (tempPuzzle.isSolvable() && !explored.containsKey(puzzleAsString)) {
			explored.put(puzzleAsString, 0);
			q.add(tempPuzzle);
		}
	}
	
	// return number of puzzle nodes generated, possible 2,3,4
	// return -1 if no puzzles generated
	private static int generateNextPuzzles(Puzzle p, Queue<Puzzle> q, Map<String, Integer> explored) {
		int[][] newPuzzleTiles;

		for (int i = 0; i < p.getTiles().length; i++) {
			if (p.getTiles()[i] == 0) {
				switch (i) {
				case 0: {
					// 2 new puzzle nodes
					newPuzzleTiles = create2Puzzles(p, 0, 1, 3);
					for (int j = 0; j < newPuzzleTiles.length; j++) {
						generateNextHelper(newPuzzleTiles, j, p, explored, q);
					}
					return 2;
				}
				case 2: {
					// 2 new puzzle nodes
					newPuzzleTiles = create2Puzzles(p, 2, 1, 5);
					for (int j = 0; j < newPuzzleTiles.length; j++) {
						generateNextHelper(newPuzzleTiles, j, p, explored, q);
					}
					return 2;
				}
				case 6: {
					// 2 new puzzle nodes
					newPuzzleTiles = create2Puzzles(p, 6, 3, 7);
					for (int j = 0; j < newPuzzleTiles.length; j++) {
						generateNextHelper(newPuzzleTiles, j, p, explored, q);
					}
					return 2;
				}
				case 8: {
					// 2 new puzzle nodes
					newPuzzleTiles = create2Puzzles(p, 8, 5, 7);
					for (int j = 0; j < newPuzzleTiles.length; j++) {
						generateNextHelper(newPuzzleTiles, j, p, explored, q);
					}
					return 2;
				}
				case 1: {
					// 3 new puzzle nodes
					newPuzzleTiles = create3Puzzles(p, 1, 0, 2, 4);
					for (int j = 0; j < newPuzzleTiles.length; j++) {
						generateNextHelper(newPuzzleTiles, j, p, explored, q);
					}
					return 3;
				}
				case 3: {
					// 3 new puzzle nodes
					newPuzzleTiles = create3Puzzles(p, 3, 0, 4, 6);
					for (int j = 0; j < newPuzzleTiles.length; j++) {
						generateNextHelper(newPuzzleTiles, j, p, explored, q);
					}
					return 3;
				}
				case 5: {
					// 3 new puzzle nodes
					newPuzzleTiles = create3Puzzles(p, 5, 2, 4, 8);
					for (int j = 0; j < newPuzzleTiles.length; j++) {
						generateNextHelper(newPuzzleTiles, j, p, explored, q);
					}
					return 3;
				}
				case 7: {
					// 3 new puzzle nodes
					newPuzzleTiles = create3Puzzles(p, 7, 4, 6, 8);
					for (int j = 0; j < newPuzzleTiles.length; j++) {
						generateNextHelper(newPuzzleTiles, j, p, explored, q);
					}
					return 3;
				}
				case 4: {
					// 4 new puzzle nodes
					newPuzzleTiles = create4Puzzles(p, 4, 1, 3, 5, 7);
					for (int j = 0; j < newPuzzleTiles.length; j++) {
						generateNextHelper(newPuzzleTiles, j, p, explored, q);
					}
					return 4;
				}
				}
				break;
			}
		}
		return -1;
	}
	
	// return 2 possible puzzles from 2 possible tile moves in 2 directions
	// X O O	O O X	O O O	O O O
	// O O O	O O O	O O O	O O O
	// O O O	O O O	X O O	O O X
	private static int[][] create2Puzzles(Puzzle p, int current, int first, int second) {
		int[][] newPuzzleTiles = new int[2][];
		
		newPuzzleTiles[0] = new int[p.getTiles().length];
		// swap tiles
		newPuzzleTiles[0][current] = p.getTiles()[first];
		newPuzzleTiles[0][first] = p.getTiles()[current];
		// copy the rest of tiles
		for (int j = 0; j < p.getTiles().length; j++) {
			if (!(j == current || j == first))
				newPuzzleTiles[0][j] = p.getTiles()[j];
		}
		
		newPuzzleTiles[1] = new int[p.getTiles().length];
		// swap tiles
		newPuzzleTiles[1][current] = p.getTiles()[second];
		newPuzzleTiles[1][second] = p.getTiles()[current];
		// copy the rest of tiles
		for (int j = 0; j < p.getTiles().length; j++) {
			if (!(j == current || j == second))
				newPuzzleTiles[1][j] = p.getTiles()[j];
		}
		
		return newPuzzleTiles;
	}

	// return 3 new puzzles from 3 possible tile moves in 3 directions
	// O X O	O O O	O O O	O O O
	// O O O	X O O	O O O	O O X
	// O O O	O O O	O X O	O O O
	private static int[][] create3Puzzles(Puzzle p, int current, int first, int second, int third) {
		int[][] newPuzzleTiles = new int[3][];
		
		newPuzzleTiles[0] = new int[p.getTiles().length];
		// swap tiles
		newPuzzleTiles[0][current] = p.getTiles()[first];
		newPuzzleTiles[0][first] = p.getTiles()[current];
		// copy the rest of tiles
		for (int j = 0; j < p.getTiles().length; j++) {
			if (!(j == current || j == first))
				newPuzzleTiles[0][j] = p.getTiles()[j];
		}
		
		newPuzzleTiles[1] = new int[p.getTiles().length];
		// swap tiles
		newPuzzleTiles[1][current] = p.getTiles()[second];
		newPuzzleTiles[1][second] = p.getTiles()[current];
		// copy the rest of tiles
		for (int j = 0; j < p.getTiles().length; j++) {
			if (!(j == current || j == second))
				newPuzzleTiles[1][j] = p.getTiles()[j];
		}
		
		newPuzzleTiles[2] = new int[p.getTiles().length];
		// swap tiles
		newPuzzleTiles[2][current] = p.getTiles()[third];
		newPuzzleTiles[2][third] = p.getTiles()[current];
		// copy the rest of tiles
		for (int j = 0; j < p.getTiles().length; j++) {
			if (!(j == current || j == third))
				newPuzzleTiles[2][j] = p.getTiles()[j];
		}
		
		return newPuzzleTiles;
	}
	
	// return 4 puzzles for 4 possible tile moves in each direction
	// O O O
	// O X O
	// O O O
	private static int[][] create4Puzzles(Puzzle p, int current, int first, int second, int third, int fourth) {
		int[][] newPuzzleTiles = new int[4][];
		
		newPuzzleTiles[0] = new int[p.getTiles().length];
		// swap tiles
		newPuzzleTiles[0][current] = p.getTiles()[first];
		newPuzzleTiles[0][first] = p.getTiles()[current];
		// copy the rest of tiles
		for (int j = 0; j < p.getTiles().length; j++) {
			if (!(j == current || j == first))
				newPuzzleTiles[0][j] = p.getTiles()[j];
		}
		
		newPuzzleTiles[1] = new int[p.getTiles().length];
		// swap tiles
		newPuzzleTiles[1][current] = p.getTiles()[second];
		newPuzzleTiles[1][second] = p.getTiles()[current];
		// copy the rest of tiles
		for (int j = 0; j < p.getTiles().length; j++) {
			if (!(j == current || j == second))
				newPuzzleTiles[1][j] = p.getTiles()[j];
		}
		
		newPuzzleTiles[2] = new int[p.getTiles().length];
		// swap tiles
		newPuzzleTiles[2][current] = p.getTiles()[third];
		newPuzzleTiles[2][third] = p.getTiles()[current];
		// copy the rest of tiles
		for (int j = 0; j < p.getTiles().length; j++) {
			if (!(j == current || j == third))
				newPuzzleTiles[2][j] = p.getTiles()[j];
		}
		
		newPuzzleTiles[3] = new int[p.getTiles().length];
		// swap tiles
		newPuzzleTiles[3][current] = p.getTiles()[fourth];
		newPuzzleTiles[3][fourth] = p.getTiles()[current];
		// copy the rest of tiles
		for (int j = 0; j < p.getTiles().length; j++) {
			if (!(j == current || j == fourth))
				newPuzzleTiles[3][j] = p.getTiles()[j];
		}
		
		return newPuzzleTiles;
	}
	
	// return heuristic for the number of misplaced tiles
	private static int findH1(int[] tiles) {
		int h1 = 0;
		for (int i = 0; i < tiles.length; i++) {
			if (tiles[i] != 0 && tiles[i] != i)
				h1++;
		}
		return h1;
	}
	
	// return heuristic for the sum of the distance of each tile from their correct positions
	private static int findH2(int[] tiles) {
		int h2 = 0;
		for (int i = 0; i < tiles.length; i++) {
			if (tiles[i] != 0) {
				// calculate column moves
				int columnDiff = Math.abs((tiles[i] % 3) - (i % 3));
				// calculate row moves
				int rowDiff = Math.abs((tiles[i] / 3) - (i / 3));
				h2 += columnDiff + rowDiff;
			}
		}
		return h2;
	}
}

class Puzzle {
	private int[] tiles;
	private int depth;
	private Puzzle parent;
	
	// copy constructor
	public Puzzle(Puzzle p) {
		tiles = new int[p.getTiles().length];
		for (int i = 0; i < p.getTiles().length; i++) {
			tiles[i] = p.getTiles()[i];
		}
		depth = p.getDepth();
		parent = p.getParent();
	}
	
	// constructor of a puzzle from a tile set and depth
	public Puzzle(int[] t, int d) {
		tiles = new int[t.length];
		for (int i = 0; i < tiles.length; i++) {
			tiles[i] = t[i];
		}
		depth = d;
		parent = null;
	}
	
	// return pointer to parent puzzle
	public Puzzle getParent() {
		return parent;
	}
	
	// set parent puzzle (previous state of puzzle)
	public void setParent(Puzzle parent) {
		this.parent = parent;
	}
	
	// return an array of the puzzle's tiles
	public int[] getTiles() {
		return tiles;
	}
	
	//return tiles integers in 3x3 format
	public String getTileString() {
//		String s = "";
//		for (int i = 0; i < tiles.length; i++) {
//			switch(i){
//				case 3:
//				case 6:
//					s += ("\n"+Integer.toString(tiles[i])+" ");
//					break;
//				default:
//					s += Integer.toString(tiles[i])+" ";
//					break;
//			}
//		}
		
		String s = "[";
		for (int i = 0; i < tiles.length; i++) {
			s += Integer.toString(tiles[i]);
		}
		s += "]";
		return s;
	}
	
	public int getDepth() {
		return depth;
	}

	// return true if number of inversions is even
	// number of inversions is equal to the sum of N
	// where N is the number of tiles with numbers less than the current tile
	public boolean isSolvable() {
		int inversions = 0;
		for (int i = 0; i < tiles.length; i++) {
			// do not include empty tile (tile 0) as part of calculations
			if (tiles[i] != 0) {
				for (int j = i + 1; j < tiles.length; j++) {
					// do not include empty tile (tile 0) as part of calculations
					if (tiles[j] != 0) {
						if (tiles[i] > tiles[j]) {
							inversions++;
						}
					}
				}
			}
		}
		if ((inversions % 2) == 0) {
			return true;
		}
		return false;
	}

	public boolean isGoalState() {
		// return false if any tile is not in the correct spot
		for (int i = 0; i < tiles.length; i++) {
			if (tiles[i] != i) {
				return false;
			}
		}
		return true;
	}
}