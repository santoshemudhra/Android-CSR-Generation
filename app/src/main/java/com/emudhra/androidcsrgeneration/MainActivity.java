package com.emudhra.androidcsrgeneration;

import android.content.ClipboardManager;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.pkcs.PKCS10CertificationRequest;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

public class MainActivity extends AppCompatActivity {

    Button button, button2;
    TextView textView;
    String alias = "emudhra test";
    private KeyPair pair;
    KeyStore keyStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (Exception e) {
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        textView = findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        //textView.setTextIsSelectable(true);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    if (keyStore.containsAlias(alias)) {

                        deleteKey(alias);

                    }

                    Calendar start = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    end.add(Calendar.YEAR, 1);
                    //noinspection deprecation
                    KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(MainActivity.this)
                            .setAlias(alias)
                            .setSubject(new X500Principal("CN=Test Certificate"))
                            .setSerialNumber(BigInteger.ONE)
                            .setStartDate(start.getTime())
                            .setEndDate(end.getTime())
                            .build();
                    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                    generator.initialize(spec);

                    pair = generator.generateKeyPair();

                    if(pair != null){

                        button2.setEnabled(true);
                        button2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PKCS10CertificationRequest csr = CsrHelper.generateCSR(pair, "Test Certificate");
                    String data = new String(Base64.encode(csr.getEncoded(), Base64.DEFAULT));

                    StringBuilder straddhar = new StringBuilder();
                    straddhar.append("-----BEGIN CERTIFICATE REQUEST-----\n");
                    straddhar.append(data);
                    straddhar.append("-----END CERTIFICATE REQUEST-----\n");

                    String csrFormat = straddhar.toString();

                    textView.setText(csrFormat);

                    button2.setEnabled(false);
                    button2.setBackgroundColor(getResources().getColor(R.color.colorGrey));

                } catch (IOException | OperatorCreationException e) {
                    e.printStackTrace();
                } catch (NullPointerException e){
                    Toast.makeText(MainActivity.this, "Please generate KeyPair", Toast.LENGTH_SHORT).show();
                }
            }
        });

        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(textView.getText());
                Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    public void deleteKey(final String alias) {

        try {
            keyStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

}
