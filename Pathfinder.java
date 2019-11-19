// Deshawn Brooks
// cop 3502 Summer 2019 Dr. Szumlanski
// Pathfinder assignment 6

import java.io.*;
import java.util.*;

public class Pathfinder
{
	// a hashset that contains all the paths
	private static HashSet<String> allPaths = new HashSet<>();

	// Used to build the paths
	private static StringBuilder builder = new StringBuilder();

	// Used to toggle "animated" output on and off (for debugging purposes).
	private static boolean animationEnabled = true;

	// "Animation" rate (frames per second).
	private static double frameRate = 4.0;

	// Setters. Note that for testing purposes you can call enableAnimation()
	// from your backtracking method's wrapper method if you want to override
	// the fact that the test cases are disabling animation. Just don't forget
	// to remove that method call before submitting!
	public static void enableAnimation() { Pathfinder.animationEnabled = true; }
	public static void disableAnimation() { Pathfinder.animationEnabled = false; }
	public static void setFrameRate(double fps) { Pathfinder.frameRate = frameRate; }

	// Maze constants.
	private static final char WALL       = '#';
	private static final char PERSON     = '@';
	private static final char EXIT       = 'e';
	private static final char BREADCRUMB = '.';  // visited
	private static final char SPACE      = ' ';  // unvisited

	// Takes a 2D char maze and returns true if it can find a path from the
	// starting position to the exit. Assumes the maze is well-formed according
	// to the restrictions above.
	public static HashSet<String> findPaths(char [][] maze)
	{
		int height = maze.length;
		int width = maze[0].length;

		// The visited array keeps track of visited positions. It also keeps
		// track of the exit, since the exit will be overwritten when the '@'
		// symbol covers it up in the maze[][] variable. Each cell contains one
		// of three values:
		//
		//   '.' -- visited
		//   ' ' -- unvisited
		//   'e' -- exit

		char [][] visited = new char[height][width];
		for (int i = 0; i < height; i++)
			Arrays.fill(visited[i], SPACE);

		// Find starting position (location of the '@' character).
		int startRow = -1;
		int startCol = -1;

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				if (maze[i][j] == PERSON)
				{
					startRow = i;
					startCol = j;
				}
			}
		}


		// Let's goooooooo!!
		findPaths(maze, visited, startRow, startCol, height, width);

		return allPaths;
	}

	private static boolean findPaths(char [][] maze, char [][] visited,
									 int currentRow, int currentCol,
									 int height, int width)
	{

		// This conditional block prints the maze when a new move is made.
		if (Pathfinder.animationEnabled)
		{
			printAndWait(maze, height, width, "Searching...", Pathfinder.frameRate);
		}

		// if we found an exit add path to hashset
		if (visited[currentRow][currentCol] == 'e')
		{
			if (Pathfinder.animationEnabled)
			{
				for (int i = 0; i < 3; i++)
				{
					maze[currentRow][currentCol] = '*';
					printAndWait(maze, height, width, "Hooray!", Pathfinder.frameRate);

					maze[currentRow][currentCol] = PERSON;
					printAndWait(maze, height, width, "Hooray!", Pathfinder.frameRate);
				}
			}

			// add path to hashset, while at the same time removing the last character since
			// we don't want to include that final space in final result
			allPaths.add(builder.substring(0, builder.length() - 1));

			return true;
		}

		// Moves: left, right, up, down
		int [][] moves = new int[][] {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

		for (int i = 0; i < moves.length; i++)
		{
			int newRow = currentRow + moves[i][0];
			int newCol = currentCol + moves[i][1];

			// Check move is in bounds, not a wall, and not marked as visited.
			if (!isLegalMove(maze, visited, newRow, newCol, height, width))
				continue;
			else
				switch (i)
				{
					// When we found a valid move, add it to the path
					case 0:
						builder.append("l" + " ");
						break;
					case 1:
						builder.append("r" + " ");
						break;
					case 2:
						builder.append("u" + " ");
						break;
					case 3:
						builder.append("d" + " ");
						break;
				}


			// Change state. Before moving the person forward in the maze, we
			// need to check whether we're overwriting the exit. If so, save the
			// exit in the visited[][] array so we can actually detect that
			// we've gotten there.
			//
			// NOTE: THIS IS OVERKILL. We could just track the exit position's
			// row and column in two int variables. However, this approach makes
			// it easier to extend our code in the event that we want to be able
			// to handle multiple exits per maze.

			if (maze[newRow][newCol] == EXIT)
				visited[newRow][newCol] = EXIT;

			maze[currentRow][currentCol] = BREADCRUMB;
			visited[currentRow][currentCol] = BREADCRUMB;
			maze[newRow][newCol] = PERSON;

			// Perform recursive descent.
			if(findPaths(maze, visited, newRow, newCol, height, width) && i > 3)
				return true;


			// Undo state change. Note that if we return from the previous call,
			// we know visited[newRow][newCol] did not contain the exit, and
			// therefore already contains a breadcrumb, so I haven't updated
			// that here.
			maze[newRow][newCol] = SPACE;
			maze[currentRow][currentCol] = PERSON;

			// Here, we are picking up the bread crumbs as we backtrack so that we can 
			// go through every possible path 
			visited[currentRow][currentCol] = SPACE;

			// If we went down a wrong path, Remove move from path
			if(builder.length() - 2 >= 0)
				builder.setLength(builder.length() - 2);

			// This conditional block prints the maze when a move gets undone
			// (which is effectively another kind of move).
			if (Pathfinder.animationEnabled)
			{
				printAndWait(maze, height, width, "Backtracking...", frameRate);
			}
		}
		return false;
	}

	// Returns true if moving to row and col is legal (i.e., we have not visited
	// that position before, and it's not a wall).
	private static boolean isLegalMove(char [][] maze, char [][] visited,
									   int row, int col, int height, int width)
	{
		// Editied this function to stay within the bounds of the maze even if 
		// The input file is not standardized.
		if(row < 0 || col < 0 || row > height - 1 || col > width - 1)
			return false;

		if (maze[row][col] == WALL || visited[row][col] == BREADCRUMB)
			return false;

		return true;
	}

	// This effectively pauses the program for waitTimeInSeconds seconds.
	private static void wait(double waitTimeInSeconds)
	{
		long startTime = System.nanoTime();
		long endTime = startTime + (long)(waitTimeInSeconds * 1e9);

		while (System.nanoTime() < endTime);
	}

	// Prints maze and waits. frameRate is given in frames per second.
	private static void printAndWait(char [][] maze, int height, int width,
									 String header, double frameRate)
	{
		if (header != null && !header.equals(""))
			System.out.println(header);

		if (height < 1 || width < 1)
			return;

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				System.out.print(maze[i][j]);
			}

			System.out.println();
		}

		System.out.println();
		wait(1.0 / frameRate);
	}

	// Read maze from file. This function dangerously assumes the input file
	// exists and is well formatted according to the specification above.
	private static char [][] readMaze(String filename) throws IOException
	{
		Scanner in = new Scanner(new File(filename));

		int height = in.nextInt();
		int width = in.nextInt();

		char [][] maze = new char[height][];

		// After reading the integers, there's still a new line character we
		// need to do away with before we can continue.

		in.nextLine();

		for (int i = 0; i < height; i++)
		{
			// Explode out each line from the input file into a char array.
			maze[i] = in.nextLine().toCharArray();
		}

		return maze;
	}
}
