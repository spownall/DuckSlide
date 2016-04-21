package Lab_4;

import java.util.ArrayList;

/** This class represents an HTTP Request.
 * It should have string variables 
 */
public class HTTP_Request {

	/** Replace the string URL, with protocol, server address, server path.*/
	protected String protocol;
	protected String serverPath;
	protected String serverAddress;

	protected int maxSize;

	/**Also create an ArrayList to store NameValuePair objects*/
	ArrayList<NameValuePair> queryParameters = new ArrayList<NameValuePair>();


	/** Since you should no longer store the entire string. Instead rebuild the 
	 * URL from the various parts.
	 * @return The combination of protocol,server address, server path, and search parameters
	 */
	public String getURL() { return this.toString(); } //should return toString() instead 


	/**Constructor to call a missing constructor that sets the initial limit for the number of parameters.
	 * 
	 * @param initial The initial search query.
	 */

	public HTTP_Request(String initial) throws MalformedQueryException 
	{
		this(initial, 0);  //Create this constructor. -1 means no limit to number of parameters
	}

	public HTTP_Request(String searchQuery, int i) throws MalformedQueryException{

		maxSize = i;

		String delimeter1 = "://";
		String delimeter2 = "/";
		String delimeter3 = "?"; // is the parameter loop start point
		String delimeter4 = "&";
		String delimeter5 = "=";

		int position1 = 0;
		int position2 = 0;
		int position3 = 0; // is the parameter loop start point
		int position4 = 0;
		int position5 = 0;


		// determines position of ://
		position1 = searchQuery.indexOf(delimeter1); // determines the position of the protocol delimeter

		if (position1 == -1) {
			throw new MalformedQueryException("The protocol could not be determined.");
		}

		protocol = searchQuery.substring(0, (position1)); // retrieves the protocol string from the url

		//determines position of /
		position2 = searchQuery.indexOf(delimeter2, (position1+delimeter1.length())); // determines the position of the server address delimeter


		if (position2 == -1) {

			serverAddress = searchQuery.substring(position1+delimeter1.length());
			serverPath = null;

			// throw new MalformedQueryException("The protocol could not be determined.");
			// server path & query parameters ARE ABOUT TO BE CREATED, and then set to null:
		} else {
			serverAddress = searchQuery.substring(position1+delimeter1.length(), position2);
			//serverPath = searchQuery.substring(position2, position3);

			// determines position of "?"
			position3 = searchQuery.indexOf(delimeter3, position2+delimeter2.length());

			if (position3 == -1) {

				serverPath = searchQuery.substring(position2);

			} else {

				serverPath = searchQuery.substring(position2, position3);

				// after the question mark
				Boolean flag = true;

				int count = 0;  // so that the array doesn't go over maxSize

				while (flag){
					
					// determines position of the "=" sign
					position4 = searchQuery.indexOf(delimeter5, position3+delimeter3.length());
					
					if (position4 == -1){
						flag = false;
					
					} else {
						
						// determines position of the "&" sign
						position5 = searchQuery.indexOf(delimeter4, position4+delimeter5.length());

						if (position5 == -1){
					
							String str1 = searchQuery.substring(position3+delimeter5.length(), position4);
							String str2 = searchQuery.substring(position4+delimeter5.length());
							
							NameValuePair nvp1 = new NameValuePair(str1, str2);
							
							queryParameters.add(nvp1);
							
							flag = false;
						
						} else {
						
							String str1 = searchQuery.substring(position3+delimeter5.length(), position4);
							String str2 = searchQuery.substring(position4+delimeter4.length(), position5);
							
							NameValuePair nvp1 = new NameValuePair(str1, str2);
							
							queryParameters.add(nvp1);
							
							position3 = position5; // increments the loop start position
							
							count++;
							
							if (count == maxSize){
							
								throw new MalformedQueryException("The parameter array size was not large "
										+ "enough to accomodate all of the paramters!");
							}
						}
					}
				}

			}

		}

	}

	/**Returns a string that reconstructs the URL
	 * @return the combination of protocol, server address, server path, and query string.
	 */
	public String toString()
	{
		return getProtocol() + "://" + getServerAddress() + getServerPath() + getSearchParameters();
	}

	
	public String getProtocol() {
		if (protocol == null){
			return "";
		}
		return protocol;
	}

	
	public String getServerAddress() {
		if (serverAddress == null){
			return "";
		}
		return serverAddress;
	}

	
	public String getServerPath() {
		if (serverPath == null){
			return "";
		}
		return serverPath;
	}

	
	public String getSearchParameters() {

		String str = "";
		if (queryParameters == null){
			return str;
		}

		for (int i=0; i < queryParameters.size(); i++) {
			NameValuePair nvp = queryParameters.get(i);
			str += (nvp.name + "=" + nvp.value);
			if (i < queryParameters.size()-1){
				str += "&";
			}
		}
		return str;
	}

	
	public String includesParam(String string) {

		for (int i=0; i < queryParameters.size(); i++){
			NameValuePair nvp = queryParameters.get(i);
			if (nvp.name.equals(string)){
				return "YES";
			}
		}
		// the parameter was not found
		return "NO!";
	}

	
	public String getValueForParam(String string) throws ParameterNotFoundException {

		for (int i = 0; i < queryParameters.size(); i++) {
			NameValuePair parameter = queryParameters.get(i);
			if (string.equals(parameter.name)) {
				return parameter.value;
			}
		}

		throw new ParameterNotFoundException("The parameter was not found!"); 
	}

	
	public void setValueForParam(String string, String string2) throws ParameterNotFoundException {

		for (int i = 0; i < queryParameters.size(); i++) {
			NameValuePair parameter = queryParameters.get(i);
			if (string.equals(parameter.name)) {
				parameter.value = string2;
				return;
			}
		}
		throw new ParameterNotFoundException("The parameter name " + string + " was not found."); 
	}

	
	public void addParameter(String string, String string2) throws ParameterArrayFullException {
		
		if (maxSize == queryParameters.size()) {
		
			throw new ParameterArrayFullException("The parameter array is full!");
		}

		NameValuePair parameter = new NameValuePair(string, string2);
		queryParameters.add(parameter);

	}

	/** Create a private inner class called NameValuePair: */
	class NameValuePair {
		public String name;
		public String value;

		public NameValuePair(String string1, String string2) {
			this.name = string1;
			this.value = string2;
		}
	}
}
