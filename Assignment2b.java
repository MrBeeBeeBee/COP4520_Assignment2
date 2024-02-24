import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;

import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Showroom
{
    private Lock lock;
    private Condition available;
    private boolean isAvailable;
    public int numGuests;
    private boolean[] hasEntered;
    private int numGuestsSeen;
    private boolean allGuestsEntered;

    public Showroom(int numGuests)
	{
        lock = new ReentrantLock();
        available = lock.newCondition();
        isAvailable = true;
        this.numGuests = numGuests;
        hasEntered = new boolean[numGuests];
        numGuestsSeen = 0;
        allGuestsEntered = false;
    }

    public void enterShowroom(int guestId)
	{
		// Flip the sign on the showroom to BUSY
        lock.lock();
        try
		{
            while (!isAvailable)
			{
				// Wait until the showroom is available
                System.out.println("The showroom is busy, Guest " + guestId + " will come back later.");
                available.await();
            }

			// Enter the showroom
            System.out.println("The showroom is free, Guest " + guestId + " is entering the showroom.");
            isAvailable = false;
        }
		catch (InterruptedException e)
		{
            e.printStackTrace();
        }
		finally
		{
            lock.unlock();
        }
    }

    public void exitShowroom(int guestId)
	{
        lock.lock();
        try
		{
            System.out.println("Guest " + guestId + " is exiting showroom.");
            isAvailable = true;
            if (!hasEntered[guestId])
			{ 
				// Check if this specific guest has entered before
                numGuestsSeen++;
				// Mark guest as having entered before
                hasEntered[guestId] = true;
                System.out.println(numGuestsSeen + " unique guests have now seen the showroom.");
                if (numGuestsSeen == numGuests)
				{
                    allGuestsEntered = true;
                    System.out.println("Every guest has entered the showroom.");
                }
            }
			// Signal to all waiting guests that the showroom is available
            available.signal();
        }
		finally
		{
            lock.unlock();
        }
		// Flip the sign on the showroom to AVAILABLE
    }

    public synchronized boolean allGuestsEntered()
	{
		// A workaround for privatized allGuestEntered access
        return allGuestsEntered;
    }
}

class ShowroomGuest implements Runnable
{
    private Showroom showroom;
    private int guestId;

    public ShowroomGuest(Showroom showroom, int guestId)
	{
        this.showroom = showroom;
        this.guestId = guestId;
    }

    @Override
    public void run()
	{
		// Only run this if not all guests have seen inside the showroom
        while (!showroom.allGuestsEntered())
		{
			// The guest views the vase for a spell
            showroom.enterShowroom(guestId);

            try
			{
				// Randomly determine the time the guest is viewing the vase
                Thread.sleep(ThreadLocalRandom.current().nextLong(1000, 5000));
            }
			catch (InterruptedException e)
			{
                e.printStackTrace();
            }

			// The guest leaves the showroom
            showroom.exitShowroom(guestId);

            // Shuffle guest IDs for revisits so the order changes
            List<Integer> guestIds = new ArrayList<>();

            for (int i = 0; i < showroom.numGuests; i++)
			{
                guestIds.add(i);
            }

            Collections.shuffle(guestIds);

            // The guest decided to go back to the party for a spell and
			// come back to the showroom later
            try
			{
				// Randomly determine the time the guest is walking around the party
                Thread.sleep(ThreadLocalRandom.current().nextLong(2000, 10000));
            }
			catch (InterruptedException e)
			{
                e.printStackTrace();
            }
        }
    }
}

public class Assignment2b
{
    public static void main(String[] args)
	{
		// Start tracking the time for the end report
		long startTime = System.currentTimeMillis();

        int numGuests = 12;
        Showroom showroom = new Showroom(numGuests);

        // Start guest threads with shuffled guest IDs
        List<Integer> guestIds = new ArrayList<>();
        for (int i = 0; i < numGuests; i++)
		{
            guestIds.add(i);
        }

        Collections.shuffle(guestIds);
        Thread[] guestThreads = new Thread[numGuests];

        for (int i = 0; i < numGuests; i++)
		{
            int guestId = guestIds.get(i);
            try
			{
                // Delay the start of each thread by a random time to induce randomness
                Thread.sleep(ThreadLocalRandom.current().nextLong(1000, 3000));
            }
			catch (InterruptedException e)
			{
                e.printStackTrace();
            }
            guestThreads[i] = new Thread(new ShowroomGuest(showroom, guestId));
            guestThreads[i].start();
        }

        // Wait for all guest threads to finish
        for (Thread thread : guestThreads)
		{
            try
			{
                thread.join();
            }
			catch (InterruptedException e)
			{
                e.printStackTrace();
            }
        }

		// Calculate the duration of the program
		long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Write the duration to an output file
        try (FileWriter writer = new FileWriter("ShowroomOutput.txt"))
		{
			writer.write("All guests have viewed the showroom, and the party ends.\n");
            writer.write("Total execution time: " + totalTime + " milliseconds");
        }
		catch (IOException e)
		{
            e.printStackTrace();
        }
    }
}
