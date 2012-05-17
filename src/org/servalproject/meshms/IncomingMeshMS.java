/*
 * Copyright (C) 2012 The Serval Project
 *
 * This file is part of Serval Software (http://www.servalproject.org)
 *
 * Serval Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.servalproject.meshms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.servalproject.R;
import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.rhizome.Rhizome;
import org.servalproject.rhizome.RhizomeMessage;
import org.servalproject.servald.Identities;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * used to receive MeshMS messages sent by other applications
 */
public class IncomingMeshMS extends IntentService {

	/*
	 * private constants
	 */
	private final String TAG = "IncomingMeshMS";

	/*
	 * call the super constructor with a name for the worker thread
	 */
	public IncomingMeshMS() {
		super("IncomingMeshMS");
	}

	/*
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns, IntentService
	 * stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		ComplexMeshMS cm = intent.getParcelableExtra("complex");
		if (cm != null) {
			Log.v(TAG, "new complex message recieved");
			processComplexMessage(cm);
		}
		SimpleMeshMS  sm = intent.getParcelableExtra("simple");
		if (sm != null) {
			Log.v(TAG, "new simple message recieved");
			processSimpleMessage(sm);
		}
		if (cm == null && sm == null) {
			Log.e(TAG, "empty intent");
		}
	}

	// private method to write a complex message to a binary file
	private void processComplexMessage(ComplexMeshMS message) {
		Log.e(TAG, "complex messages NOT IMPLEMENTED");
		/*
		//TODO add validation of fields?
		// build a protobuf bassed meshsms
		MeshMSProtobuf.MeshMS.Builder mMeshMS = MeshMSProtobuf.MeshMS.newBuilder();

		// add the main metadata
		mMeshMS.setSender(message.getSender());
		mMeshMS.setRecipient(message.getRecipient());
		mMeshMS.setType(message.getType());
		mMeshMS.setTimestamp(message.getTimestamp());

		// add the content
		ArrayList<MeshMSElement> mContentList = message.getContent();

		MeshMSProtobuf.MeshMS.ContentElem.Builder mMeshMSContent;

		for(MeshMSElement mContent: mContentList) {

			if(mContent.getContent() != null) {
				// optional message components must still have a value
				mMeshMSContent = MeshMSProtobuf.MeshMS.ContentElem.newBuilder();
				mMeshMSContent.setType(mContent.getType());
				mMeshMSContent.setContent(mContent.getContent());
				mMeshMS.addContent(mMeshMSContent);
			}
		}

		// write the file
		File mOutbox = new File(getString(R.string.system_path_meshms_outbox));
		mOutbox.mkdirs();

		String mFileName = getString(R.string.system_path_meshms_outbox) + message.getSender() + "-" + message.getRecipient() + ".bin";

		try {
			FileOutputStream mOutput = new FileOutputStream(mFileName);
			mMeshMS.build().writeTo(mOutput);
			mOutput.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Unable to create output file", e);
		} catch (IOException e) {
			Log.e(TAG, "Unable to write the output file", e);
		}
		*/
	}

	// private method to process a simple message
	private void processSimpleMessage(SimpleMeshMS message) {

		// validate the message contents
		if (message.getRecipient() == null) {
			Log.e(TAG, "new simpleMeshMS is missing the recipient field");
			return;
		}

		if (message.getContent() == null) {
			Log.e(TAG, "new simpleMeshMS is missing the content field");
			return;
		}

		if (message.getSender() == null) {
			// replace with the sender configured in batphone
			Log.w(TAG,
					"new simpleMeshMS is missing sender field, using primary batphone number");
			message.setSender(Identities.getCurrentDid());
		}

		// declare helper variables
		ServalBatPhoneApplication mBatphoneApplication = (ServalBatPhoneApplication) getApplicationContext();

		// for the purposes of KiwiEx send all messages via store and forward
		// send message via rhizome
		Log.d(TAG, "sender=" + message.getSender());
		Log.d(TAG, "recipient=" + message.getRecipient());
		Log.d(TAG, "content=" + message.getContent());
		RhizomeMessage rm = new RhizomeMessage(message.getSender(), message.getRecipient(), message.getContent());
		boolean sent = false; // TODO Rhizome.appendMessage(Identities.getCurrentIdentity(), rm);

		if (sent == false) {
			Log.w(TAG, "unable to send new SimpleMeshMS via Rhizome");
		} else {
			Log.i(TAG, "new simpleMeshMS to: " + message.getRecipient()
					+ " has been sent via Rhizome");
		}
	}

}
