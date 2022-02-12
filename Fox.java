import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a fox.
 * Foxes age, move, eat rabbits, and die.
 * 
 * @author David J. Barnes and Michael Kölling + Reuben Atendido
 * @version 2016.02.29 (2)
 */
public class Fox extends Animal
{
    // Characteristics shared by all foxes (class variables).
    
    // The age at which a fox can start to breed.
    private static final int BREEDING_AGE = 14;
    // The age to which a fox can live.
    private static final int MAX_AGE = 275;
    // The likelihood of a fox breeding.
    private static final double BREEDING_PROBABILITY = 0.09;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 4;
    // The food value of a single rabbit. In effect, this is the
    // number of steps a fox can go before it has to eat again.
    private static final int RABBIT_FOOD_VALUE = 20;
    
    private static final int RACCOON_FOOD_VALUE = 5;
    
    private static final int PIG_FOOD_VALUE = 25;
    
    private static final double FEMALE_PROBABILITY = 0.5;
    
    private static final double DISEASE_PROBABILITY = 0.2;
    
   
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    
    private static final boolean isNocturnal = true;
    // Individual characteristics (instance fields).
    // The fox's age.
    private int age;
    // The fox's food level, which is increased by eating rabbits.
    private int foodLevel;
    
    private boolean isFemale;
    //private Simulator sim;
    
    private int hungerLoss;
    
    /**
     * Create a fox. A fox can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the fox will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Fox(boolean randomAge, Field field, Location location, boolean isFemale)
    {
        super(field, location, isNocturnal, isFemale);
        this.isFemale = isFemale;
        this.hungerLoss = 1;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(RABBIT_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = RABBIT_FOOD_VALUE;
        }
    }
    
    /**
     * This is what the fox does most of the time: it hunts for
     * rabbits. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newFoxes A list to return newly born foxes.
     */
    public void act(List<Animal> newFoxes)
    {   
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            giveBirth(newFoxes);            
            // Move towards a source of food if found.
            Location newLocation = findFood();
            if(newLocation == null) { 
                // No food found - try to move to a free location.
                newLocation = getField().freeAdjacentLocation(getLocation());
            }
            // See if it was possible to move.
            if(newLocation != null) {
                setLocation(newLocation);
            }
            else {
                // Overcrowding.
                setDead();
            }
        }
    }

    /**
     * Increase the age. This could result in the fox's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this fox more hungry. This could result in the fox's death.
     */
    private void incrementHunger()
    {
        foodLevel = foodLevel - hungerLoss;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    private void diseaseEffect()
    {
      hungerLoss = 5;  
    }
    
    /**
     * Look for rabbits adjacent to the current location.
     * Only the first live rabbit is eaten.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        Location foodLocation = null;
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Rabbit ) {
                Rabbit rabbit = (Rabbit) animal;
                if(rabbit.isAlive()) { 
                    rabbit.setDead();
                    foodLevel = RABBIT_FOOD_VALUE;
                    foodLocation = where;
                    if(rabbit.getIsDiseased()){
                        toggleIsDiseased();
                        diseaseEffect();
                        System.out.println("Fox diseased");
                    }
                }
            }
            else if(animal instanceof Pig) {
                Pig pig = (Pig) animal;
                if(pig.isAlive()) {
                    pig.setDead();
                    foodLevel = PIG_FOOD_VALUE;
                    foodLocation = where;
                }
            }
            else if(animal instanceof Raccoon) {
                Raccoon raccoon = (Raccoon) animal;
                if(raccoon.isAlive()){
                    raccoon.setDead();
                    foodLevel = RACCOON_FOOD_VALUE;
                    foodLocation = where;
                }
            }
        }
        return foodLocation;
    }
        
    /**
     * Check whether or not this fox is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newFoxes A list to return newly born foxes.
     */
    private void giveBirth(List<Animal> newFoxes)
    {
        // New foxes are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            if(rand.nextDouble() <= FEMALE_PROBABILITY){
                     Fox young = new Fox(true, field, loc, true);
                     newFoxes.add(young);
            }
            else{
                     Fox young = new Fox(true, field, loc, false);
                     newFoxes.add(young);
            }
        }
    }
        
    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return The number of births (may be zero).
     */
    private int breed()
    {
        int births = 0;
        if(canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            Field field = getField();
            List<Location> adjacent = field.adjacentLocations(getLocation());
            Iterator<Location> it = adjacent.iterator();
            while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if (animal instanceof Fox){
                    Fox fox = (Fox) animal;
                    if(fox.getIsFemale() != getIsFemale()){
                        births = rand.nextInt(MAX_LITTER_SIZE) + 1;
                    }
                }
            }
        }
        return births;
    }

    /**
     * A fox can breed if it has reached the breeding age.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
}