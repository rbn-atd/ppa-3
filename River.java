
/**
 * Write a description of class River here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class River extends BiomeFeature
{
    // instance variables - replace the example below with your own

    /**
     * Constructor for objects of class River
     */
    public River(Field field, Location location)
    {
        // initialise instance variables
        super(field, location);
        setLocation(location);
    }

    private void initialiseAnimalMovement()
    {
        canMoveThrough.add(Eagle.class);
        canMoveThrough.add(Bear.class);
    }
    
    protected void setLocation(Location newLocation)
    {
        if(location != null) {
            field.clear(location);
        }
        location = newLocation;
        field.place(this, newLocation);
    }
}
