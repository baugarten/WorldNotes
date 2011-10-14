package com.project.geonotes;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Simple wrapper to abstract communication with BlazingContacts web services
 * @author Sam Pottinger
 */
public class WebWrapper {

        public enum Mode {SHARE_CATEGORY, RETRIEVE_CATEGORY, REGISTER_USER, LOGIN_USER};
        public enum HttpMethod {GET, POST, PUT, DELETE};
        private static final String DEFAULT_HOST ="world-notes.appspot.com/";
        private static final String DEFAULT_SCHEME = "http";
        private static final String CATEGORY_NAME_PARAMETER = "category_name";
        private static final String CATEGORY_ID_PARAMETER = "_id";
        private static final String CATEGORY_PARAMETER = "category";
        private static final String SOURCE_USERNAME_PARAMETER = "source_username";
        private static final String TARGET_USERNAME_PARAMETER = "target_username";
        private static final String PASSWORD_PARAMETER = "password";
        private static final String EMAIL_PARAMETER = "email";
        private static final String DATA_PARAMETER = "data";
        private static final String UPDATE_RESOURCE = "/update";
        private static final String UPDATE_RETRIEVAL_RESOURCE = "/update/download";
        private static final String CATEGORY_RESOURCE = "/category";
        private static final String REGISTRATION_RESOURCE = "/register";
        
        private static final String JSON_NAME_ATTRIBUTE = "category_name";
        private static final String JSON_NOTE_PLACE_ATTRIBUTE = "note_place";
        private static final String JSON_NOTE_NOTE_ATTRIBUTE = "note_note";
        private static final String JSON_NOTE_LAT_ATTRIBUTE = "note_lat";
        private static final String JSON_NOTE_LONG_ATTRIBUTE = "note_long";
        private static final String JSON_DATE_ATTRIBUTE = "expiration";
        private static final String JSON_GROUP_MAX_ATTRIBUTE = "max_members";
        private static final String JSON_MEMBER_COUNT_ATTRIBUTE = "current_users";
        private static final String JSON_CATEGORIES_ATTRIBUTE = "categories";
        private static final String JSON_ERROR_NUM_ATTRIBUTE = "error";
        private static final String JSON_ERROR_MESSAGE_ATTRIBUTE = "error_message";
        private static final String JSON_RESULT = "result";
        
        public static final int NO_GROUP_MAX = -1;
        private static final int NO_ERROR = 0;
        private boolean success = false;
        private String categoryName;
        private String sourceUsername;
        private String targetUsername;
        private String password;
        private String category_id;
        private Mode mode;
        private ArrayList<Category> categories;
        
        /**
         * Retrieves a all categories for a given username
         * 
         * @param newGroupName The name of the group to join
         * @param newPassword The group password to use
         * @param newContact The local user's self-selected contact information
         * @throws JSONException 
         * @throws IOException 
         * @throws URISyntaxException 
         * @throws ClientProtocolException 
         * @throws ServerException 
         */
        //public WebWrapper(String newCategoryName, String targetName, Category sharedCategory) throws ClientProtocolException, URISyntaxException, IOException, JSONException, ServerException
        public WebWrapper(String source_username) throws ClientProtocolException, URISyntaxException, IOException, JSONException, ServerException
        {
                // Save simple attributes
                mode = Mode.RETRIEVE_CATEGORY;
                /*categoryName = newCategoryName;
                password = targetName;
                
                // Upload contact information
                addCategoryInfo(sharedCategory);*/
                this.sourceUsername = source_username;
                
                ArrayList<NameValuePair> parameters;
                parameters = new ArrayList<NameValuePair>(1);
                parameters.add(new BasicNameValuePair(SOURCE_USERNAME_PARAMETER, this.sourceUsername));
                Log.d("WEBWRAPPER", "hey");
                JSONObject result = executeRequest(UPDATE_RETRIEVAL_RESOURCE, HttpMethod.POST, parameters);
                Log.d("WEBWRAPPER", result.toString());
                
                JSONArray categoryList = result.getJSONArray(CATEGORY_NAME_PARAMETER);
                JSONArray usernames = result.getJSONArray(SOURCE_USERNAME_PARAMETER);
                JSONArray notes = result.getJSONArray(JSON_NOTE_NOTE_ATTRIBUTE);
                JSONArray places = result.getJSONArray(JSON_NOTE_PLACE_ATTRIBUTE);
                JSONArray lats = result.getJSONArray(JSON_NOTE_LAT_ATTRIBUTE);
                JSONArray longs = result.getJSONArray(JSON_NOTE_LONG_ATTRIBUTE);
                Log.d("WEBWRAPPER", "Got arrays");
                categories = new ArrayList<Category>();
                for (int k = 0; k < categoryList.length(); k++) {
                	Log.d("WEBWRAPPER", categoryList.getString(k) + " " + stringToArray(notes.getString(k)) + " " + stringToArray(places.getString(k)));
                	String categoryName = categoryList.getString(k);
                	String sourceUser = usernames.getString(k);
                	String[] noteList = stringToArray(notes.getString(k));
                	String[] placeList = stringToArray(places.getString(k));
                	String[] latList = stringToArray(lats.getString(k));
                	String[] longList = stringToArray(longs.getString(k));
                	Category toAdd =new Category(categoryName, placeList, noteList, latList, longList);
                	toAdd.addSourceUser(usernames.getString(k));
                	categories.add(toAdd);
                }
        }
        
        /**
         * Creates and interfaces with a new category share
         * 
         * @param newGroupName The name of the group to join
         * @param newPassword The password to use
         * @param newContact The local user's self-selected contact information
         * @param expiration The Date after which the group will be deleted
         * @param groupMax The maximum number of people allowed in the group
         * @throws JSONException 
         * @throws IOException 
         * @throws URISyntaxException 
         * @throws ClientProtocolException 
         * @throws ServerException 
         */
        public WebWrapper(String source_username, String target_username, Category newCategoryShare) throws ClientProtocolException, URISyntaxException, IOException, JSONException, ServerException
        {
                ArrayList<NameValuePair> parameters;
                
                // Save attributes
                mode = Mode.SHARE_CATEGORY;
                categoryName = newCategoryShare.getName();
                sourceUsername = source_username;
                targetUsername = target_username;
                
                // Create list of parameters
                // TODO: ArrayList may not be the most effective data structure here
                parameters = new ArrayList<NameValuePair>(4);
                //parameters.add(new BasicNameValuePair(EXPIRATION_PARAMETER, dateToISOString(expiration)));
                //parameters.add(new BasicNameValuePair(GROUP_MAX_MEMBERS_PARAMETER, new Integer(groupMax).toString()));
                Log.d("WEBWRAPPER", categoryName);
                parameters.add(new BasicNameValuePair(CATEGORY_NAME_PARAMETER, categoryName));
                parameters.add(new BasicNameValuePair(SOURCE_USERNAME_PARAMETER, sourceUsername));
                parameters.add(new BasicNameValuePair(TARGET_USERNAME_PARAMETER, targetUsername));
                
                parameters.add(new BasicNameValuePair(CATEGORY_PARAMETER, newCategoryShare.toJSON()));
                Log.d("WEBWRAPPER", "Sending request");
                // Create group on server
                executeRequest(UPDATE_RESOURCE, HttpMethod.POST, parameters);
                
                
                // Send contact information to server
                //addCategoryInfo(newCategoryShare);
        }
        /**
         * This sends an http request to register the user
         * @param username: user's username
         * @param password: user's password
         * @param email: user's email
         * @throws ClientProtocolException
         * @throws URISyntaxException
         * @throws IOException
         * @throws JSONException
         * @throws ServerException
         */
        public WebWrapper(String username, String password, String email) throws ClientProtocolException, URISyntaxException, IOException, JSONException, ServerException {
        	mode = Mode.REGISTER_USER;
        	ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>(3);
        	parameters.add(new BasicNameValuePair(SOURCE_USERNAME_PARAMETER, username));
        	parameters.add(new BasicNameValuePair(PASSWORD_PARAMETER, password));
        	parameters.add(new BasicNameValuePair(EMAIL_PARAMETER, email));
        	Log.d("WebWrapper", "Made request, sending it");
        	JSONObject result = executeRequest(REGISTRATION_RESOURCE, HttpMethod.POST, parameters);
        	Log.d("WebWrapper", "Request to Register sent");
        	String tmp = result.getString("success");
        	if (tmp.toLowerCase().equals("true")) {
        		Log.d("WEBWRAPPER", "Successful");
        		success = true;
        	} else {
        		Log.d("WEBWRAPPER", "Failure");
        		success = false;
        	}
        }
        public WebWrapper(String username, String password) throws ClientProtocolException, URISyntaxException, IOException, JSONException, ServerException {
        	mode = Mode.LOGIN_USER;
        	ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>(2);
        	parameters.add(new BasicNameValuePair(SOURCE_USERNAME_PARAMETER, username));
        	parameters.add(new BasicNameValuePair(PASSWORD_PARAMETER, password));
        	JSONObject result = executeRequest(REGISTRATION_RESOURCE, HttpMethod.POST, parameters);
        	String tmp = result.getString("success");
        	if (tmp.toLowerCase().equals("true")) {
        		Log.d("WEBWRAPPER", "Successful");
        		success = true;
        	} else {
        		Log.d("WEBWRAPPER", "Failure");
        		success = false;
        	}
        	
        }
        public ArrayList<Category> getCategories() {
        	return categories;
        }
        public boolean loginSuccess() {
        	return success;
        }
        private String[] stringToArray(String arr) {
        	return arr.split("%");
        	
        }
        /**
         * Requests the status of the current group with dates encoded for the user's timezone
         * @return A GroupStatus corresponding to this WebWrapper's group
         * @throws JSONException 
         * @throws IOException 
         * @throws URISyntaxException 
         * @throws ClientProtocolException 
         * @throws ParseException 
         * @throws ServerException 
         */
 /*       public GroupStatus getStatus() throws ClientProtocolException, URISyntaxException, IOException, JSONException, ParseException, ServerException
        {
                String isoExpirationDate;
                Date expiration;
                Date current_date;
                Date difference;
                int groupMax;
                int memberCount;
                
                // Get the JSON encoded information from the server
                JSONObject result = executeRequest(UPDATE_RESOURCE, HttpMethod.GET, new ArrayList<NameValuePair>(2));
                
                // Determine remaining time
                isoExpirationDate = result.getString(JSON_DATE_ATTRIBUTE);
                expiration = isoStringToDate(isoExpirationDate);
                current_date = new Date();
                difference = new Date(expiration.getTime() - current_date.getTime());
                
                // Parse other data
                groupMax = result.getInt(JSON_GROUP_MAX_ATTRIBUTE);
                memberCount = result.getInt(JSON_MEMBER_COUNT_ATTRIBUTE);
                
                return new GroupStatus(difference, groupMax, memberCount);
        }*/
        
        /**
         * Determines the name of the group this WebWrapper is
         * responsible for
         * @return The string group name
         */
        public String getCategoryName()
        {
                return categoryName;
        }
        
        /**
         * Requests and decodes all the group contact information
         * @return An array of Contact objects
         * @throws JSONException 
         * @throws IOException 
         * @throws URISyntaxException 
         * @throws ClientProtocolException 
         * @throws ServerException 
         */
        public Category[] downloadCategory() throws ClientProtocolException, URISyntaxException, IOException, JSONException, ServerException
        {
                JSONObject result;
                JSONArray jsonCategoryList;
                ArrayList<Category> javaCategoryList;
                
                // Execute request
                result = executeRequest(UPDATE_RETRIEVAL_RESOURCE, HttpMethod.GET, new ArrayList<NameValuePair>(2));
                
                // Create contact objects (javaContactList) from results (jsonContactList)
                jsonCategoryList = result.getJSONArray(JSON_CATEGORIES_ATTRIBUTE);
                javaCategoryList = new ArrayList<Category>(jsonCategoryList.length());
                for(int i = 0; i<jsonCategoryList.length(); i++)
                        javaCategoryList.set(i, Category.fromJSON((String) jsonCategoryList.get(i)) ); // TODO: a bit kludgy
                
                // TODO: The cast to array is a bit kludgy
                return (Category []) (javaCategoryList.toArray());
        }
        
        /**
         * Publishes the given contact info to the web service
         * @param info The contact information to publish
         * @throws JSONException 
         * @throws IOException 
         * @throws URISyntaxException 
         * @throws ClientProtocolException 
         * @throws ServerException 
         */
        private void addCategoryInfo(Category info) throws ClientProtocolException, URISyntaxException, IOException, JSONException, ServerException
        {
                // Create parameters
                ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>(3);
                parameters.add(new BasicNameValuePair( DATA_PARAMETER, info.toJSON() ));
                
                // Execute the request
                executeRequest(CATEGORY_RESOURCE, HttpMethod.PUT, parameters);
        }
        
        /**
         * Executes an HTTPRequest on the server, attaching a password and group name
         * 
         * @param resource The URL to execute this method on (eg /group or /contact)
         * @param method The HTTP method to use (GET, POST, PUT, DELETE)
         * @param parameters List of NameValuePairs to use as form encoded parameters
         * @return Decoded JSON result from the server
         * @throws URISyntaxException
         * @throws ClientProtocolException
         * @throws IOException
         * @throws JSONException
         * @throws ServerException 
         */
        private JSONObject executeRequest(String resource, HttpMethod method, List<NameValuePair> parameters) throws URISyntaxException, ClientProtocolException, IOException, JSONException, ServerException
        {
                HttpRequestBase request;
                HttpResponse response;
                String jsonContent;
                JSONObject result;
                NameValuePair pair;
                //MultipartEntity data = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                //MultipartEntity data = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                HttpClient httpclient = new DefaultHttpClient();
                
                // Add group name and password
                // TODO: These should be added directly to httpParams
                parameters.add(new BasicNameValuePair(CATEGORY_NAME_PARAMETER, categoryName));
                parameters.add(new BasicNameValuePair(PASSWORD_PARAMETER, password));
                
                // Create resource path and request
                request = createRequest(method, resource, parameters);
                
                // Execute request
                Log.d("WEBWRAPPER", request.getMethod());
                response = httpclient.execute(request);
                Log.d("WEBWRAPPER", response.toString());
                // Parse results and create JSON object
                jsonContent = readResults(response);
                Log.d("WEBWRAPPER", jsonContent.toString());
                result = new JSONObject(jsonContent);
                //result = new String(jsonContent);
                Log.d("WEBWRAPPER", result.toString());
                
                // Check for errors
                if (result.getInt(JSON_ERROR_NUM_ATTRIBUTE) != NO_ERROR)
                        throw new ServerException(result.getString(JSON_ERROR_MESSAGE_ATTRIBUTE));
                
                return result;//result.getJSONObject(JSON_RESULT);
        }
        
        /**
         * Simple factory to create an appropriate request object
         * @param method The method that will be executed
         * @param resource The resource this request is targeting
         * @param parameters the parameters to encode and pass to server
         * @return The appropriate request that corresponds to the provided method
         * @throws UnsupportedEncodingException 
         * @throws URISyntaxException 
         */
        private HttpRequestBase createRequest(HttpMethod method, String resource, List<NameValuePair> parameters) throws UnsupportedEncodingException, URISyntaxException
        {       
                URI uri;
                
                // Generate URI
                // TODO: A bit of a kludge
                if(method == HttpMethod.GET || method == HttpMethod.DELETE)
                {                       
                        // Create URI and insert parameters if needed
                        uri = URIUtils.createURI(DEFAULT_SCHEME, DEFAULT_HOST, -1, resource, 
                                        URLEncodedUtils.format(parameters, "UTF-8"), null);
                }
                else
                {
                        // Create URI and insert parameters if needed
                        uri = URIUtils.createURI(DEFAULT_SCHEME, DEFAULT_HOST, -1, resource, 
                                    null, null);
                }
                
                // Make actual request
                switch (method)
                {
                
                // Create URL encoded values for GET and DELETE requests
                case GET:
                        HttpGet returnGet = new HttpGet(uri);
                        return returnGet;
                
                case DELETE:
                        HttpDelete returnDelete = new HttpDelete(uri);
                        return returnDelete;
                        
                // Create multipart data for POST and PUT
                case PUT:
                        HttpPut returnPut = new HttpPut(uri);
                        returnPut.setEntity(createMultipart(parameters));
                        return returnPut;
                        
                case POST:
                        HttpPost returnPost = new HttpPost(uri);
                        returnPost.setEntity(createMultipart(parameters));
                        return returnPost;
                        
                default:
                        return null; // Unreachable code
                }
        }
        
        /**
         * Create an HttpParams instance for URL encoded parameters
         * @param parameters the parameters to encode
         * @return An Apache HttpParams encoding of the provided parameters
         */
        private HttpParams createParams(List<NameValuePair> parameters) {
                NameValuePair pair;
                HttpParams httpParams = new BasicHttpParams();
                
                // Create http parameters
                for(int i=0; i<parameters.size(); i++)
                {
                        pair = parameters.get(i);
                        httpParams.setParameter(pair.getName(), pair.getValue());
                }
                return httpParams;
        }

        /**
         * Create form style multipart data 
         * @param parameters The parameters to encode
         * @return An HttpEntity encoding of the provided parameters
         * @throws UnsupportedEncodingException 
         */
        private HttpEntity createMultipart(List<NameValuePair> parameters) throws UnsupportedEncodingException {
                return new UrlEncodedFormEntity(parameters);
        }

        /**
         * Returns the raw string body of an HttpResponse
         * @param response the response to read
         * @return Body of the provided response
         * @throws IllegalStateException
         * @throws IOException
         */
        private String readResults(HttpResponse response) throws IllegalStateException, IOException
        {
                // Get the buffered reader from an InputStream and InputStreamReader
                InputStream inputStream = response.getEntity().getContent();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader r = new BufferedReader(reader);
                
                // Create a string builder to 
                StringBuilder total = new StringBuilder();
                
                // Read body from reader
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                
                return total.toString();
        }
        
        /**
         * Converts the given date to an ISO compatible string
         * @param target The date to convert
         * @return A string ISO representation of the target
         */
        private String dateToISOString(Date target)
        {
                SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
                return formatter.format(target);
        }
        
        /**
         * Returns a Date representation of this ISO encoded string datetime
         * @param target The string representation of this datetime
         * @return The Date object representation of target
         * @throws ParseException 
         */
        private Date isoStringToDate(String target) throws ParseException
        {
                SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                return formatter.parse(target);
        }
        
        /**
         * Determines the Date that is the number of given milliseconds from now
         * @param milliseconds The number of milliseconds to offset by
         * @return A new Date object that represents the time a given number of milliseconds from now
         */
        public static Date getDateFromNow(long milliseconds)
        {
                Date now = new Date();
                long totalMilliSec = now.getTime() + milliseconds;
                return new Date(totalMilliSec);
        }
}