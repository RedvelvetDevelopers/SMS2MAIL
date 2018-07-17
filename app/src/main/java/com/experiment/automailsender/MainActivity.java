package com.experiment.automailsender;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.experiment.automailsender.Utlis.Utils;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Arrays;

import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static com.experiment.automailsender.Utlis.Utils.REQUEST_AUTHORIZATION;

/**
 * Created by Sananda on 10-07-2018.
 */

public class MainActivity extends AppCompatActivity {

    LoginDataBaseAdapter loginDataBaseAdapter;
    private static final int PERMISSION_REQUEST_CODE = 1234;
    private com.google.api.services.gmail.Gmail mService = null;
    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {
            GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_COMPOSE,
            GmailScopes.GMAIL_INSERT,
            GmailScopes.GMAIL_MODIFY,
            GmailScopes.GMAIL_READONLY,
            GmailScopes.MAIL_GOOGLE_COM
    };
    public static String toMilId;
    private String userName;
    EditText edtToAddress;
    Button CreateAccount,btnDeleteAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        requestContactPermission();
        loginDataBaseAdapter=new LoginDataBaseAdapter(this);
        loginDataBaseAdapter=loginDataBaseAdapter.open();
        toMilId=loginDataBaseAdapter.getSinlgeEntry();
        edtToAddress = (EditText) findViewById(R.id.to_address);

        if(toMilId.equalsIgnoreCase("NOT EXIST"))
        {
            edtToAddress.setText("");
        }
        else{
            edtToAddress.setText(toMilId);
            edtToAddress.setEnabled(false);
        }

        CreateAccount=(Button) findViewById(R.id.createAccount);
        btnDeleteAccount=(Button)findViewById(R.id.deleteAccount);

        CreateAccount.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
// TODO Auto-generated method stub

                userName=edtToAddress.getText().toString();
                loginDataBaseAdapter.insertEntry(userName);
                edtToAddress.setEnabled(false);
                Toast.makeText(getApplicationContext(), "To Account Successfully Created ", Toast.LENGTH_LONG).show();
                init();
                requestContactPermission();

            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
// TODO Auto-generated method stub

                userName=edtToAddress.getText().toString();
                loginDataBaseAdapter.deleteEntry();
                Toast.makeText(getApplicationContext(), "To Account Successfully Deleted ", Toast.LENGTH_LONG).show();
                edtToAddress.setText("");
                edtToAddress.setEnabled(true);

            }
        });
    }



private void sendMail(final String message, Context context) throws IOException, MessagingException {
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
        context, Arrays.asList(SCOPES))
        .setBackOff(new ExponentialBackOff());

        String accountName = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_NAME,
        Context.MODE_PRIVATE).getString(Constants.PREF_ACCOUNT_NAME, null);
        if (accountName != null) {
        mCredential.setSelectedAccountName(accountName);

        MimeMessage mimeMessage = createEmail(accountName, accountName, "SMS2MAIL", message);
        Message messageWithEmail = createMessageWithEmail(mimeMessage);
        // GMail's official method to send email with oauth2.0

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.gmail.Gmail.Builder(
        transport, jsonFactory, mCredential)
        .setApplicationName(context.getResources().getString(R.string.app_name))
        .build();

        messageWithEmail = mService.users().messages().send(accountName, messageWithEmail).execute();
        Toast.makeText(context, "ID : "+messageWithEmail.getId(), Toast.LENGTH_SHORT).show();
        }
        }

private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        InternetAddress tAddress = new InternetAddress(to);
        InternetAddress fAddress = new InternetAddress(from);

        email.setFrom(fAddress);
        email.addRecipient(javax.mail.Message.RecipientType.TO, tAddress);
        email.setSubject(subject);

        // Create Multipart object and add MimeBodyPart objects to this object
        Multipart multipart = new MimeMultipart();

        BodyPart textBody = new MimeBodyPart();
        textBody.setText(bodyText);
        multipart.addBodyPart(textBody);

        // Set the multipart object to the message object
        email.setContent(multipart);
        return email;
        }

private Message createMessageWithEmail(MimeMessage email)
        throws MessagingException, IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        email.writeTo(bytes);
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
        }

    private void requestContactPermission() {

        int hasContactPermission =ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECEIVE_SMS);

        if(hasContactPermission != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]   {Manifest.permission.RECEIVE_SMS},
                    PERMISSION_REQUEST_CODE);
        }else {
            startService(new Intent(MainActivity.this, MyService.class));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                // Check if the only required permission has been granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("Permission", "Contact permission has now been granted. Showing result.");
                    Toast.makeText(this,"Contact Permission is Granted",Toast.LENGTH_SHORT).show();
                    startService(new Intent(MainActivity.this, MyService.class));
                } else {
                    Log.i("Permission", "Contact permission was NOT granted.");
                }
                break;
            case Utils.REQUEST_PERMISSION_GET_ACCOUNTS:
                startActivityForResult(mCredential.newChooseAccountIntent(), Utils.REQUEST_ACCOUNT_PICKER);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case Utils.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_NAME,
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Constants.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        Toast.makeText(MainActivity.this, "Saved"+" "+accountName, Toast.LENGTH_SHORT).show();

                        sendEmail();
                    }
                }
                break;
        }
    }

    private void init() {
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        chooseAccount();
    }

    // Storing Mail ID using Shared Preferences
    private void chooseAccount() {
        if (Utils.checkPermission(getApplicationContext(), Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE).getString(Constants.PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                sendEmail();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential.newChooseAccountIntent(), Utils.REQUEST_ACCOUNT_PICKER);
            }
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.GET_ACCOUNTS}, Utils.REQUEST_PERMISSION_GET_ACCOUNTS);
        }
    }

    private void sendEmail(){
        loginDataBaseAdapter=new LoginDataBaseAdapter(MainActivity.this);
        loginDataBaseAdapter=loginDataBaseAdapter.open();
        toMilId=loginDataBaseAdapter.getSinlgeEntry();

        new AsyncTask<Object, Void, Void>(){
            @Override
            protected Void doInBackground(Object... voids) {
                String message = (String) voids[0];
                String senderAddress = (String) voids[1];
                Context context = (Context) voids[2];

                try {
                    sendMail("Received SMS: " + message + ", Sender: " + senderAddress, context);
                } catch(UserRecoverableAuthIOException exception){
                    startActivityForResult(
                            exception.getIntent(),
                            REQUEST_AUTHORIZATION);
                } catch (Exception exception){
                    Log.e("Main", "Exception: " + exception.getMessage());
                }
                return null;
            }
        }.execute("Test", toMilId, MainActivity.this);
    }
}