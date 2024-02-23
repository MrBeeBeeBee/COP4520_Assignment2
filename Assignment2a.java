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

	public synchronized void eatCupcake(int guestId)
	{
        if (cupcakePresent && reorderCount != (numGuests - 1))
		{
            System.out.println("Guest " + guestId + ": Eating cupcake.");
            cupcakePresent = false;
        }
    }

    public synchronized void reorderCupcake()
	{
        if (!cupcakePresent && reorderCount != (numGuests - 1))
		{
            System.out.println("Orderer (Guest 0): Reordering cupcake.");
            cupcakePresent = true;
            reorderCount++;
			System.out.println("Cupcake has been reordered " + reorderCount + " times.");
        }

		if(reorderCount == (numGuests - 1))
		{
			allVisited = true;
		}
    }

    public synchronized void enterLabyrinth(int guestId) 
	{
        System.out.println("Guest " + guestId + ": Entering labyrinth.");
        if (!cupcakePresent && guestId != 0)
		{
            // Consuming Guests leave if cupcake is not present
            System.out.println("Guest " + guestId + ": Cupcake not present. Leaving labyrinth.");
            return;
        }
		if (cupcakePresent && guestId == 0)
		{
            // Ordering Guest leaves if cupcake is present
            System.out.println("Guest " + guestId + ": Cupcake is present. Not ordering new cupcake.");
            return;
        }
        // Simulate labyrinth exploration
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
        if (!labyrinth.allVisited())
		{
            labyrinth.enterLabyrinth(guestId);
            if(guestId != 0 && !this.cupcakeEaten)
			{
				labyrinth.eatCupcake(guestId);
				this.cupcakeEaten = true;
			}

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
        long startTime = System.currentTimeMillis();
        int numGuests = 12;
        MinotaurLabyrinth labyrinth = new MinotaurLabyrinth(numGuests);

        // Create a list of guest IDs
        List<Integer> guestIds = new ArrayList<>();
        for (int i = 0; i < numGuests; i++)
		{
            guestIds.add(i);
        }

        // Start guest threads
        while (!labyrinth.allVisited()) 
		{
            Collections.shuffle(guestIds); // Shuffle guest IDs for random order
            for (int guestId : guestIds) 
			{
                new Thread(new Guest(labyrinth, guestId, false)).start();
            }
        }

        // Write to file
        try (FileWriter writer = new FileWriter("LabyrinthOutput.txt"))
		{
            writer.write("All guests have gone through the labyrinth.\n");
            long elapsedTime = System.currentTimeMillis() - startTime;
            writer.write("Elapsed time: " + elapsedTime + " milliseconds\n");
        } 
		catch (IOException e) 
		{
            e.printStackTrace();
        }

		System.out.println("All done!");
    }
}
