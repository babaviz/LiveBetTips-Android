package com.livebettips.activities;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.livebettips.R;
import com.livebettips.objects.Api;
import com.livebettips.objects.Profile;
import com.livebettips.objects.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class Login extends ActionBarActivity {

    TextView tv_validEmail,tv_validPassword,tv_resetPassword;
    EditText et_email,et_password;
    Button bt_login;
    String email,password;
    Boolean valid = false;
    User user;
    Context ctx;
    SharedPreferences preferences;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ctx = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(ctx);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Logging in");
        progressDialog.setIndeterminate(true);

        Api.initSlidingMenu(ctx).attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

        preferences = ctx.getSharedPreferences("bettips", MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();

        et_email = (EditText) findViewById(R.id.et_login_email);
        et_password = (EditText) findViewById(R.id.et_login_password);
        tv_validEmail = (TextView) findViewById(R.id.tv_login_validEmail);
        tv_validPassword = (TextView) findViewById(R.id.tv_login_validPassword);
        bt_login = (Button) findViewById(R.id.bt_login_login);
        tv_resetPassword = (TextView) findViewById(R.id.tv_login_resetPassword);

        user = new User();

        bt_login.setEnabled(false);

        et_email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
                    Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
                    email = et_email.getText().toString();
                    Matcher matcher = pattern.matcher(email);
                    if(!matcher.matches()){
                        tv_validEmail.setText("Please Enter a valid Email ID");
                        valid=false;
                    }else{
                        tv_validEmail.setText("");
                        valid=true;
                    }
                }
            }
        });
        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (valid && s.length() >= 5) {
                    password = s.toString();
                    bt_login.setEnabled(true);
                    tv_validPassword.setText("");

                } else {
                    tv_validPassword.setText("Password should be greater than 5 characters");
                    bt_login.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                user.setEmail(et_email.getText().toString());
                user.setPassword(et_password.getText().toString());
                user.setDeviceID(preferences.getString("GCM_REG_ID",""));
                progressDialog.show();

                Api.userInterface.userLogin(user, new Callback<Profile>() {
                @Override
                    public void success(Profile profile, Response response) {

                        Log.d("ID  authToken", profile.getId().toString() +" "+ profile.getAuthToken());
                        editor.putInt("userID", profile.getId());
                        editor.putString("userEmail",user.getEmail());
                        editor.putString("authToken", profile.getAuthToken());
                        editor.putBoolean("isLoggedIn",true);
                        editor.putInt("credit",profile.getCredit());
                        editor.commit();
                        progressDialog.dismiss();

                        Toast.makeText(ctx,"Login Successfully",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ctx,PushedPredictions.class);
                        startActivity(intent);


                    }

                    @Override
                    public void failure(RetrofitError error) {


                        try {
                            if (error.getResponse().getStatus() == 404) {
                                tv_validEmail.setText("Email ID does not exist");
                                tv_validPassword.setText("");
                            }else if(error.getResponse().getStatus()== 409){
                                tv_validEmail.setText("EmailID is not verified");
                                tv_validPassword.setText("");
                            }
                        }catch (NullPointerException e){
                            e.printStackTrace();
                            tv_validEmail.setText("");
                            tv_validPassword.setText("Password is incorrect");
                        }

                        progressDialog.dismiss();
                    }
                });


            }
        });

        tv_resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,ResetPassword.class);
                startActivity(intent);
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Api.slidingMenu.toggle();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent back = new Intent(this,PushedPredictions.class);
        finish();
        startActivity(back);
    }
}
