package com.chatt.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.utils.Utils;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The Class Register is the Activity class that shows user registration screen
 * that allows user to register itself on Parse server for this Chat app.
 */
public class Register extends CustomActivity
{

	/** The username EditText. */
	private EditText user;

	/** The password EditText. */
	private EditText pwd;

	/** The upload Button. */
	private Button upload;

	/** The email EditText. */
	private EditText email;

    /** The image. */
    byte[] byteArray;

	/* (non-Javadoc)
	 * @see com.chatt.custom.CustomActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		setTouchNClick(R.id.btnReg);

		user = (EditText) findViewById(R.id.user);
		pwd = (EditText) findViewById(R.id.pwd);
		email = (EditText) findViewById(R.id.email);
		upload=(Button)findViewById(R.id.upload);

        setTouchNClick(R.id.upload);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && data != null) {

            Uri selectedImage = data.getData();

            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                byteArray = stream.toByteArray();

                upload.setText("Picture selected");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /* (non-Javadoc)
         * @see com.chatt.custom.CustomActivity#onClick(android.view.View)
         */
	@Override
	public void onClick(View v)
	{
		super.onClick(v);
        if (v.getId() == R.id.btnReg){
		final String u = user.getText().toString();
		final String p = pwd.getText().toString();
		String e = email.getText().toString();
		if (u.length() == 0 || p.length() == 0 || e.length() == 0)
		{
			Utils.showDialog(this, R.string.err_fields_empty);
			return;
		}
		final ProgressDialog dia = ProgressDialog.show(this, null,
				getString(R.string.alert_wait));

		final ParseUser pu = new ParseUser();
        ParseFile file = new ParseFile(u+".png", byteArray);
		pu.setEmail(e);
		pu.setPassword(p);
		pu.setUsername(u);
        pu.put("image",file);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null) {
                    pu.signUpInBackground(new SignUpCallback() {

                        @Override
                        public void done(ParseException e) {
                            dia.dismiss();
                            if (e == null) {
                                UserList.user = pu;
                                startActivity(new Intent(Register.this, UserList.class));
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Utils.showDialog(
                                        Register.this,
                                        getString(R.string.err_singup) + " "
                                                + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                }
                else
                    Toast.makeText(getApplicationContext(), "could not be uploaded - please try again", Toast.LENGTH_LONG).show();
            }
        });


	}
	else if (v.getId() == R.id.upload)
        {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i,1);
        }
	}
}

