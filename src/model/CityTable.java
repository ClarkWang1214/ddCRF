package model;

/**
 * A model to hold the city id and the table_id within the city
 * @author rajarshd
 *
 */
public class CityTable {

	int cityId;
	
	int tableId;
	
	public CityTable(int cityId, int tableId)
	{
		this.cityId = cityId;
		this.tableId = tableId;
	}
	
	public int getCityId() {
		return cityId;
	}
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
	public int getTableId() {
		return tableId;
	}
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}
	
	
}
