import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.concurrent.ThreadLocalRandom;

class MinotaurLabyrinth
{
    private boolean cupcakePresent;
    private int numGuests;
    private int reorderCount;
	private boolean allVisited;

    public MinotaurLabyrinth(int numGuests)
	{
        this.numGuests = numGuests;
        cupcakePresent = true;
        reorderCount = 0;
		allVisited = false;
    }

	// Guest eats a cupcake if they aren't the reordering guest and haven't eaten a cupcake yet
	public synchronized void eatCupcake(int guestId)
	{
        if (cupcakePresent && reorderCount != (numGuests - 1))
		{
            System.out.println("Guest " + guestId + " is eating a cupcake.");
            cupcakePresent = false;
        }
    }

	// Guest orders a new cupcake if they are the reordering guest
    public synchronized void reorderCupcake()
	{
        if (!cupcakePresent && reorderCount != (numGuests - 1))
		{
            System.out.println("Guest 0 is reordering the cupcake.");

			//Reset the cupcake state and increment the number of times the cupcake has been reordered
            cupcakePresent = true;
            reorderCount++;

			System.out.println("The cupcake has been reordered " + reorderCount + " times.");
        }

		if(reorderCount == (numGuests - 1))
		{
			// A workaround for privatized allVisited access
			allVisited = true;
		}
    }

	// Guest enters the labyrinth, remarking on if the cupcake was present or not
	// Mostly for notation purposes
    public synchronized void enterLabyrinth(int guestId) 
	{
        System.out.println("Guest " + guestId + " is entering the labyrinth.");
        if (!cupcakePresent && guestId != 0)
		{
            // Consuming Guests leave if cupcake is not present
            System.out.println("Guest " + guestId + " found no cupcake and is leaving the labyrinth.");
            return;
        }
		if (cupcakePresent && guestId == 0)
		{
            // Ordering Guest leaves if cupcake is present
            System.out.println("Guest " + guestId + " found a cupcake and doesn't need to reorder it");
            return;
        }

        // The guest wanders through the labyrinth for a spell
        try
		{
            Thread.sleep(ThreadLocalRandom.current().nextLong(500, 2000));
        }
		catch (InterruptedException e)
		{
            e.printStackTrace();
        }
    }

	public synchronized boolean allVisited()
	{
		// A workaround for privatized allVisited access
		return allVisited;
	}
}

class Guest implements Runnable
{
    private MinotaurLabyrinth labyrinth;
    private int guestId;
	private boolean cupcakeEaten;

    public Guest(MinotaurLabyrinth labyrinth, int guestId, boolean cupcakeEaten)
	{
        this.labyrinth = labyrinth;
        this.guestId = guestId;
		this.cupcakeEaten = cupcakeEaten;
    }

    @Override
    public void run() 
	{
		// Only operates as long as the Ordering Guest hasn't determined
		// all guests have gone through the labyrinth
        if (!labyrinth.allVisited())
		{
			// Guest enters the labyrinth
			// Guest eats a cupcake if they aren't the Ordering Guest and haven't eaten a cupcake yet
            labyrinth.enterLabyrinth(guestId);
            if(guestId != 0 && !this.cupcakeEaten)
			{
				labyrinth.eatCupcake(guestId);
				this.cupcakeEaten = true;
			}
			// Guest attempts to order a new cupcake if they are the Ordering Guest
			else
			{
				labyrinth.reorderCupcake();
			}
        }
    }
}

public class Assignment2a 
{
    public static void main(String[] args) 
	{
		// Start tracking the time for the end report
        long startTime = System.currentTimeMillis();

        int numGuests = 12;
        MinotaurLabyrinth labyrinth = new MinotaurLabyrinth(numGuests);

        // Start guest threads with shuffled guest IDs
        List<Integer> guestIds = new ArrayList<>();
        for (int i = 0; i < numGuests; i++)
		{
            guestIds.add(i);
        }

        // Only operates as long as the Ordering Guest hasn't determined
		// all guests have gone through the labyrinth
        while (!labyrinth.allVisited()) 
		{
            Collections.shuffle(guestIds);
            for (int guestId : guestIds) 
			{
                new Thread(new Guest(labyrinth, guestId, false)).start();
            }
        }

		// Calculate the duration of the program
		long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Write the duration to an output file
        try (FileWriter writer = new FileWriter("LabyrinthOutput.txt"))
		{
            writer.write("All guests have gone through the labyrinth, and the party ends.\n");
            writer.write("Total execution time: " + totalTime + " milliseconds");
        } 
		catch (IOException e) 
		{
            e.printStackTrace();
        }

    }
}
