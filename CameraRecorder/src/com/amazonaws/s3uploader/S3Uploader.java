/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.s3uploader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.dongwen.cn.R;

public class S3Uploader  {

	private AmazonS3Client s3Client = new AmazonS3Client(
			new BasicAWSCredentials(Constants.ACCESS_KEY_ID,
					Constants.SECRET_KEY));

	private Context mContext;
	public S3Uploader(Context context) {
		mContext = context;
	}

	public void uploadDataInfo(String fileNames[]) {
		S3PutObjectTask s3PutObjectTask = new S3PutObjectTask();
		s3PutObjectTask.execute(fileNames);
	}

	// Display an Alert message for an error or failure.
	protected void displayAlert(String title, String message) {

		AlertDialog.Builder confirm = new AlertDialog.Builder(mContext);
		confirm.setTitle(title);
		confirm.setMessage(message);

		confirm.setNegativeButton(
				mContext.getString(R.string.ok),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();
					}
				});

		confirm.show().show();
	}

	protected void displayErrorAlert(String title, String message) {

		AlertDialog.Builder confirm = new AlertDialog.Builder(mContext);
		confirm.setTitle(title);
		confirm.setMessage(message);

		confirm.setNegativeButton(
				mContext.getString(R.string.ok),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						((Activity)mContext).finish();
					}
				});

		confirm.show().show();
	}

	private class S3PutObjectTask extends AsyncTask<String[], Void, S3TaskResult[]> {

		ProgressDialog dialog;

		protected void onPreExecute() {
			dialog = new ProgressDialog(mContext);
			dialog.setMessage(mContext
					.getString(R.string.uploading));
			dialog.setCancelable(false);
			dialog.show();
		}

		protected void onPostExecute(S3TaskResult results[]) {
			dialog.dismiss();

			StringBuilder errorMessage = new StringBuilder();
			for(int i = 0; i < results.length; i++){
				if(results[i] != null){
				    String message = results[i].getErrorMessage();
                    if (message != null) {
                        errorMessage.append(results[i].getErrorMessage());
                        errorMessage.append("\n");
                    }
				}
			}

            if (errorMessage.toString().length() > 0) {

                displayErrorAlert(mContext.getString(R.string.upload_failure_title),
                        errorMessage.toString());
            } else {
                displayErrorAlert("", "Upload successfully!");
            }
		}

		@Override
		protected S3TaskResult[] doInBackground(String[]... filePaths) {

			if (filePaths[0] == null || filePaths[0].length == 0) {
				return null;
			}

            String bucket = Constants.getFileBucket();
            if(!s3Client.doesBucketExist(bucket))
                s3Client.createBucket(bucket);

			S3TaskResult result[] = new S3TaskResult[filePaths[0].length];
			for(int i = 0; i < filePaths[0].length; i++){
				result[i] = new S3TaskResult();
				try {
					String filePath = filePaths[0][i];
					// Content type is determined by file extension.
					PutObjectRequest por = new PutObjectRequest(
							bucket, filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length()),
							new java.io.File(filePath));
					s3Client.putObject(por);
				} catch (Exception exception) {
					result[i] .setErrorMessage("Error: " + exception.getMessage());
				}
			}

			return result;
		}
	}

//	private class S3GeneratePresignedUrlTask extends
//			AsyncTask<Void, Void, S3TaskResult> {
//		
//		protected S3TaskResult doInBackground(Void... voids) {
//
//			S3TaskResult result = new S3TaskResult();
//
//			try {
//				// Ensure that the image will be treated as such.
//				ResponseHeaderOverrides override = new ResponseHeaderOverrides();
//				override.setContentType("image/jpeg");
//
//				// Generate the presigned URL.
//
//				// Added an hour's worth of milliseconds to the current time.
//				Date expirationDate = new Date(
//						System.currentTimeMillis() + 3600000);
//				GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(
//						Constants.getPictureBucket(), Constants.PICTURE_NAME);
//				urlRequest.setExpiration(expirationDate);
//				urlRequest.setResponseHeaders(override);
//
//				URL url = s3Client.generatePresignedUrl(urlRequest);
//
//				result.setUri(Uri.parse(url.toURI().toString()));
//
//			} catch (Exception exception) {
//
//				result.setErrorMessage(exception.getMessage());
//			}
//
//			return result;
//		}
//
//		protected void onPostExecute(S3TaskResult result) {
//			
//			if (result.getErrorMessage() != null) {
//
//				displayErrorAlert(
//						S3Uploader.this
//								.getString(R.string.browser_failure_title),
//						result.getErrorMessage());
//			} else if (result.getUri() != null) {
//
//				// Display in Browser.
//				startActivity(new Intent(Intent.ACTION_VIEW, result.getUri()));
//			}
//		}
//	}


	private class S3TaskResult {
		String errorMessage = null;
		Uri uri = null;

		public String getErrorMessage() {
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}

		public Uri getUri() {
			return uri;
		}

		public void setUri(Uri uri) {
			this.uri = uri;
		}
	}
}
