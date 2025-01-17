package com.acktos.rutaplus.controllers;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.acktos.rutaplus.R;
import com.acktos.rutaplus.android.Encrypt;
import com.acktos.rutaplus.android.GCMFunctions;
import com.acktos.rutaplus.android.HttpRequest;
import com.acktos.rutaplus.android.InternalStorage;
import com.acktos.rutaplus.entities.User;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Class for all network connections related to the {@link User} entity
 * and performing data processing before delivery to presentation.
 */

public class UserController {

	private Context context;
	private static final String RESPONSE_TAG="response";
	private static final String ENTERPRISE_TAG="empresa";
	private static final String ENTERPRISE_FAILED_LOGIN="Individual";
	public static final String ID_TAG="id";
	private static final String FIELDS_TAG="fields";
	private static final String RESPONSE_SUCCESS_CODE="200";
	public static final String FILE_NAME_PROFILE="profile.json";
	private static final String TOKEN="ee8099de39d5167fe135baf92fa0df1c";
	private InternalStorage storage;

	public static final String LOGIN_SUCCESS="LOGIN_SUCCESS";
	public static final String LOGIN_FAILED="LOGIN_FAILED";
	public static final String SERVER_ERROR="SERVER_ERROR";
	public static final String LOGIN_ENTERPRISE="LOGIN_ENTERPRISE";
	public static final String RESPONSE_OK="RESPONSE_OK";
	public static final String RESPONSE_FAILED="RESPONSE_FAILED";


	/**
	 * Public constructor through context reference.
	 * @param context
	 */
	public UserController(Context context){
		this.context=context;
		storage=new InternalStorage(context);

	}


	/**
	 * Retrieve user email from internal storage
	 * @return user email
	 */
	public String getUserEmail(){

		String profileData=storage.readFile(FILE_NAME_PROFILE);
		return getJsonValue(profileData,User.KEY_EMAIL);
	}


	/**
	 * Retrieve user ID from internal storage
	 * @return user ID
	 */
	public String getUserId(){

		String profileData=storage.readFile(FILE_NAME_PROFILE);
		return getJsonValue(profileData,User.KEY_ID);
	}


	/**
	 * Retrieve user name from internal storage
	 * @return user ID
	 */
	public String getName(){

		String profileData=storage.readFile(FILE_NAME_PROFILE);
		return getJsonValue(profileData,User.KEY_NAME);
	}


	/**
	 * Retrieve user identification from internal storage
	 * @return user ID
	 */
	public String getCC(){

		String profileData=storage.readFile(FILE_NAME_PROFILE);
		return getJsonValue(profileData,User.KEY_CC);
	}

	/**
	 * Retrieve user password from internal storage
	 * @return user ID
	 */
	public String getPswrd(){
		String profileData=storage.readFile(FILE_NAME_PROFILE);
		return getJsonValue(profileData,User.KEY_PSWRD);
	}

	/**
	 * Retrieve user enterprise from internal storage
	 * @return user ID
	 */
	public String getEnterprise(){
		String profileData=storage.readFile(FILE_NAME_PROFILE);
		return getJsonValue(profileData,User.KEY_ENTERPRISE);
	}

	/**
	 * Checks if users belongs to a company
	 * @return true if the user belongs
	 */
	public boolean isEnterprise(){

		String enterprise=getEnterprise();
		
		if (enterprise!=null){
			if(!enterprise.equals(ENTERPRISE_FAILED_LOGIN)){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
		
	}

    /**
     * This method gets a value from json object string format
     * @param json
     * @param key
     * @return a value for a given key
     */
	public String getJsonValue(String json, String key){

		String value=null;

		if(json!=null){
			try {
				JSONObject jsonObject=new JSONObject(json);
				value=jsonObject.getString(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return value;
	}


    /**
     * Makes a request to user_login service through REST API
     * @param email
     * @param pswrd
     * @return login constant {@value #LOGIN_SUCCESS} or {@value #LOGIN_FAILED} or {@value #LOGIN_ENTERPRISE}
     */
	public String loginUserService(String email,String pswrd){

		String response=SERVER_ERROR;
		String responseData=null;
		String responseCode=null;
		JSONObject jsonObject;


		HttpRequest httpPost=new HttpRequest(context.getString(R.string.url_login_user_service));
		String encrypt=Encrypt.md5(email+pswrd+TOKEN);

		//set post params
		httpPost.setParam("email", email);
		httpPost.setParam("pswrd", pswrd);
		httpPost.setParam("encrypt", encrypt);
		//Log.i("debug email", email);
		//Log.i("debug pswrd",pswrd);

		responseData=httpPost.postRequest();

		if(responseData!=null){
			Log.i("response data",responseData+"");

			try {
				jsonObject = new JSONObject(responseData);
				responseCode=jsonObject.getString(RESPONSE_TAG);
				if(responseCode.equals(RESPONSE_SUCCESS_CODE)){
					saveFileFields(jsonObject);
					//Log.i("debug file",storage.readFile(FILE_NAME_PROFILE));

					JSONObject jsonFields=jsonObject.getJSONObject(FIELDS_TAG);
					//Log.i(this.getClass().getSimpleName(),"empresa:"+jsonFields.getString(ENTERPRISE_TAG));
					if(!jsonFields.getString(ENTERPRISE_TAG).equals(ENTERPRISE_FAILED_LOGIN)){
						response= LOGIN_ENTERPRISE;
					}else{
						response= LOGIN_SUCCESS;
					}

				}else{
					response= LOGIN_FAILED;
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		return response;
	}


    /**
     * Makes a request to change_password service through REST API
     * @param oldPassword
     * @param newPassword
     * @return response constant {@value #RESPONSE_OK} or {@value #RESPONSE_FAILED}
     */
	public String changePassword(String oldPassword, String newPassword){

		String response=SERVER_ERROR;
		String responseData=null;
		String responseCode=null;
		JSONObject jsonObject;
		String userId=getUserId();


		HttpRequest httpPost=new HttpRequest(context.getString(R.string.url_change_password));
		String encrypt=Encrypt.md5(userId+newPassword+TOKEN);

		//set post params
		httpPost.setParam("id", userId);
		httpPost.setParam("pswrd", newPassword);
		httpPost.setParam("encrypt", encrypt);
		//Log.i("debug email",userId);
		//Log.i("debug pswrd",newPassword);

		responseData=httpPost.postRequest();

		if(responseData!=null){
			Log.i("changePassword",responseData+"");

			try {
				jsonObject = new JSONObject(responseData);
				responseCode=jsonObject.getString(RESPONSE_TAG);
				if(responseCode.equals(RESPONSE_SUCCESS_CODE)){

					response=RESPONSE_OK;


				}else{
					response= RESPONSE_FAILED;
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		return response;
	}


    /**
     * Makes a request to remember_password service through REST API
     * @param email
     * @return response constant {@value #RESPONSE_OK} or {@value #RESPONSE_FAILED}
     */
    public String rememberPassword(String email){


        String response=SERVER_ERROR;
        String responseData=null;
        String responseCode=null;
        JSONObject jsonObject;


        HttpRequest httpPost=new HttpRequest(context.getString(R.string.url_remember_password));
        String encrypt=Encrypt.md5(email+TOKEN);

        //set post params
        httpPost.setParam("usuario", email);
        httpPost.setParam("encrypt", encrypt);

        //Log.i("debug email",email);

        responseData=httpPost.postRequest();

        if(responseData!=null){

            //Log.i("response remember pass",responseData);
            try {
                jsonObject = new JSONObject(responseData);
                responseCode=jsonObject.getString(RESPONSE_TAG);
                if(responseCode.equals(RESPONSE_SUCCESS_CODE)){

                    response=RESPONSE_OK;


                }else{
                    response= RESPONSE_FAILED;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return response;

    }



	/**
	 * Register a new User with a User object through REST API.
	 * @param user
	 * @return boolean
	 */
	public boolean addUserService(User user){

		String responseData=null;
		try {
			HttpRequest httpPost=new HttpRequest(context.getString(R.string.url_add_user_service));

			String userAgent=Build.BRAND+" "+Build.MODEL+" Android "+Build.VERSION.RELEASE;
			httpPost.setParam("user_agent",userAgent);

			String name=user.getName();
			if(name!=null && !name.isEmpty()){
				httpPost.setParam("name",name);
			}
			String email=user.getEmail();
			if(email!=null && !email.isEmpty()){
				httpPost.setParam("email",email);
			}

			String id=user.getId();
			if(id!=null && !id.isEmpty()){
				httpPost.setParam("id",id);
			}

			String cc=user.getCc();
			if(cc!=null && !cc.isEmpty()){
				httpPost.setParam("cc",cc);
			}

			String phone=user.getPhone();
			if(phone!=null && !phone.isEmpty()){
				httpPost.setParam("phone",phone);
			}

			String pswrd=user.getPswrd();
			if(pswrd!=null && !pswrd.isEmpty()){
				httpPost.setParam("pswrd",pswrd);
			}

			String gId=user.getgId();
			if(gId!=null && !gId.isEmpty()){
				httpPost.setParam("id_google",gId);
			}
			String mobileId=user.getMobileId();
			if(mobileId!=null && !mobileId.isEmpty()){
				httpPost.setParam("mobile_id",mobileId);
			}

			String encrypt=Encrypt.md5(id+name+cc+email+pswrd+userAgent+gId+mobileId+phone+TOKEN);
			//Log.i("debug encrypt",id+name+cc+email+pswrd+userAgent+gId+phone+TOKEN);
			httpPost.setParam("encrypt",encrypt);

			responseData=httpPost.postRequest();
			//Log.i("response data",responseData);

			JSONObject jsonObject=new JSONObject(responseData);
			String responseCode=jsonObject.getString(RESPONSE_TAG);
			//Log.i("debug responsecode:",responseCode);
			if(responseCode.equals(RESPONSE_SUCCESS_CODE)){

				saveFileFields(jsonObject);
				//Log.i("debug file after save",storage.readFile(FILE_NAME_PROFILE));
				return true;
			}else{
				return false;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

    /**
     * Checks if users already complete profile information.
     * @return true, if the profile is complete.
     * @throws JSONException
     */
	public boolean isProfileCompleted() throws JSONException{

		boolean completed=false;
		String jsonStringUser=storage.readFile(FILE_NAME_PROFILE);
		JSONObject jsonObject=new JSONObject(jsonStringUser);
		String cedula=jsonObject.getString("cc");
		if(cedula.isEmpty()){
			completed= false;
		}else{
			completed=true;
		}
		return completed;
	}

    /**
     * Saves profile information into local storage file.
     * @param jsonObject
     */
	private void saveFileFields(JSONObject jsonObject){

		JSONObject jsonFieldsObject;
		try {

			int appVersion=0;

			if(storage.isFileExists(FILE_NAME_PROFILE)){
				String dataFile=storage.readFile(FILE_NAME_PROFILE);
				JSONObject jsonObjectStorage=new JSONObject(dataFile);

				if(!jsonObjectStorage.isNull(GCMFunctions.KEY_APP_VERSION)){
					appVersion=jsonObjectStorage.getInt(GCMFunctions.KEY_APP_VERSION);
				}

			}

			jsonFieldsObject = jsonObject.getJSONObject(FIELDS_TAG);

			if(appVersion!=0){
				jsonFieldsObject.put(GCMFunctions.KEY_APP_VERSION,appVersion);
			}
			//Log.i("debug jsonFieldsObject "+getClass(),jsonFieldsObject.toString());
			storage.saveFile(FILE_NAME_PROFILE, jsonFieldsObject.toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}


}
