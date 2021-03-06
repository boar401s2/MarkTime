package boar401s2.marktime.storage.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import android.os.Bundle;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.AsyncTask;
import boar401s2.marktime.MarkTime;
import boar401s2.marktime.constants.ResultIDList;
import boar401s2.marktime.constants.TaskIDList;
import boar401s2.marktime.events.AsyncTaskParent;
import boar401s2.marktime.exceptions.UnCaughtException;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class GetAuthToken {
	
	String account;
	AsyncTaskParent parent;
	Activity activityParent;
	String token;
	
	GoogleAccountCredential GACredential; 
	AccountManager am;
	
	public GetAuthToken(AsyncTaskParent parent, Activity activityParent, String account){
		this.account = account;
		this.parent = parent;
		this.activityParent = activityParent;
	}
	
	/**
	 * Runs the task
	 */
	public void run(){
		new GetAuthTokenTask().execute();
	}
	
	/**
	 * Gets the oauth2 token gotten by the task
	 * @return String token
	 */
	public String getToken(){
		return token;
	}
	
	/**
	 * Task that gets the oauth2 token for a user
	 */
	class GetAuthTokenTask extends AsyncTask<Void, String, Integer> {	
		boolean doneTask = false;
		
		@Override
		protected void onPreExecute(){
			Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(MarkTime.activity.getApplicationContext()));
			parent.onPreExecute();
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			final Collection<String> SCOPES = new ArrayList<String>();  
			
	        SCOPES.add("https://spreadsheets.google.com/feeds/");  
	        SCOPES.add("https://docs.google.com/feeds/"); 
	        SCOPES.add("https://docs.googleusercontent.com/");
	        
	        GACredential = GoogleAccountCredential.usingOAuth2(MarkTime.activity.getApplicationContext(), SCOPES);  
	        am = AccountManager.get(MarkTime.activity.getApplicationContext());
			publishProgress("Getting token...");
			GACredential.setSelectedAccountName(account);
	        am.getAuthToken(GACredential.getSelectedAccount(), GACredential.getScope(),  
	                 null, activityParent, new AccountManagerCallback<Bundle>() {  
			@Override  
			public void run(AccountManagerFuture<Bundle> future) {  
				auth(future);
			}
		    }, null);
	        while(doneTask==false){}
			if(token!=null){
				return ResultIDList.RESULT_OK;
			} else {
				return ResultIDList.RESULT_ERROR;
			}
		}
		
		@Override
		protected void onPostExecute(Integer result){
			parent.onPostExecute(TaskIDList.TASK_GET_AUTH_TOKEN, result);
		}
		
		@Override
		public void onProgressUpdate(String... text){
			parent.onStatusChange(text[0]);
		}
		
		/**
		 * Unsure of the use of this method.
		 * @param future
		 */
		public void auth(AccountManagerFuture<Bundle> future){
			try {    
				token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);  
			} catch (OperationCanceledException e) { 
				e.printStackTrace();  
			} catch (AuthenticatorException e) {
				e.printStackTrace();  
			} catch (IOException e) {
				e.printStackTrace();  
			} 
			doneTask = true;
		}
	}

}