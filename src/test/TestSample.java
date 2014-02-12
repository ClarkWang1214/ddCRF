package test;

/**
 * Represents a venue in the test data
 * @author rajarshd
 *
 */
public class TestSample {

  private int cityIndex; //city of the venue
  private int listIndex; //index of the venue within the list of venues in a city
  private double venueCategory; // the observed category of the venue  QUESTION: WHY DOUBLE?

    
  public TestSample(int listIndex, int cityIndex, double venueCategory)
  {
    this.listIndex = listIndex;
    this.cityIndex = cityIndex;
    this.venueCategory = venueCategory;
  }

  public int getCityIndex() { return cityIndex; }
  public int getListIndex() { return listIndex; }
  public Double getVenueCategory() { return venueCategory; }

  public String toJson() {
    return "{" + "cityIndex:" + String.valueOf(cityIndex) + "," +
                 "listIndex:" + String.valueOf(listIndex) + "," +
                 "venueCategory:" + String.valueOf(venueCategory) + "}";
  }
}
