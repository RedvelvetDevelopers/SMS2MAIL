package com.experiment.automailsender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
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

/**
 * Created by Sananda on 10-07-2018.
 */

public class SMSBroadCastReceiver extends BroadcastReceiver {
    LoginDataBaseAdapter loginDataBaseAdapter;
    private GoogleAccountCredential mCredential;
    private com.google.api.services.gmail.Gmail mService = null;
    private static final String[] SCOPES = {
            GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_COMPOSE,
            GmailScopes.GMAIL_INSERT,
            GmailScopes.GMAIL_MODIFY,
            GmailScopes.GMAIL_READONLY,
            GmailScopes.MAIL_GOOGLE_COM
    };
    private static final String TAG = SMSBroadCastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (Object aPdusObj : pdusObj) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                    String senderAddress = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();

                    Log.e(TAG, "Received SMS: " + message + ", Sender: " + senderAddress);
                    Toast.makeText(context, "Sender: "
                            + senderAddress, Toast.LENGTH_SHORT).show();
                    new AsyncTask<Object, Void, Void>(){
                        @Override
                        protected Void doInBackground(Object... voids) {
                            String message = (String) voids[0];
                            String senderAddress = (String) voids[1];
                            Context context = (Context) voids[2];
                            try {
                                sendMail("Received SMS: " + message + ", Sender: " + senderAddress, context);
                            } catch (Exception exception){
                                Log.e(TAG, "Exception: " + exception.getMessage());
                            }
                            return null;
                        }
                    }.execute(message, senderAddress, context);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            e.getCause().printStackTrace();
        }
    }

    private void sendMail(final String message, Context context) throws IOException, MessagingException {
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        loginDataBaseAdapter=new LoginDataBaseAdapter(context);
        loginDataBaseAdapter=loginDataBaseAdapter.open();
       String toMilId=loginDataBaseAdapter.getSinlgeEntry();

        String accountName = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE).getString(Constants.PREF_ACCOUNT_NAME, null);
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);

            MimeMessage mimeMessage = createEmail(toMilId, accountName, "SMS2MAIL", message);
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
}