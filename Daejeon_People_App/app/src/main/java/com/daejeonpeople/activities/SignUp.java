package com.daejeonpeople.activities;

import android.content.Intent;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.daejeonpeople.R;
import com.daejeonpeople.connection.connectionValues;
import com.daejeonpeople.support.firebase.Firebase;

import java.util.HashMap;
import java.util.Map;
//민지

public class SignUp extends AppCompatActivity {
    private AQuery aQuery;
    private Button submit;
    private Button emailCertifiedBtn;

    private EditText userName;
    private boolean userNameChecked = false;

    private EditText userId;
    private boolean idChecked = false;

    private EditText userPassword;
    private EditText passwordConfirm;
    private boolean passwordConfirmed = false;

    private Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        firebase = new Firebase();
        aQuery = new AQuery(getApplicationContext());

        submit = (Button) findViewById(R.id.signupSubmit);
        emailCertifiedBtn = (Button) findViewById(R.id.emailCertified);

        userName = (EditText) findViewById(R.id.userName);
        userId = (EditText) findViewById(R.id.userId);
        userPassword = (EditText) findViewById(R.id.userPassword);
        passwordConfirm = (EditText) findViewById(R.id.passwordConfirm);

        if(Email_Certified.emailDemanded) {
            // 이메일 인증이 완료됐다면 버튼 컬러 변경
            emailCertifiedBtn.setTextColor(Color.rgb(111, 186, 119));
        }

        emailCertifiedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 인증 액티비티로 분기
                startActivity(new Intent(getApplicationContext(), Email_Certified.class));
            }
        });

        userName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !userName.getText().toString().isEmpty()) {
                    // 이름이 1글자 이상일 때. 명시적
                    userName.setTextColor(Color.rgb(111, 186, 119));
                    userNameChecked = true;
                } else {
                    // 포커스가 다시 바뀌는 경우, 이름이 비어있는 경우를 방지
                    userNameChecked = false;
                }
            }
        });

        userId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String id = userId.getText().toString();
                if (!hasFocus && !id.isEmpty()) {
                    // EditText가 비어있지 않을 때

                    Map<String, String> params = new HashMap<>();
                    params.put("id", id);

                    aQuery.ajax("http://52.79.134.200/signup/id/check", params, String.class, new AjaxCallback<String>() {
                        @Override
                        public void callback(String url, String response, AjaxStatus status) {
                            int statusCode = status.getCode();
                            if (statusCode == 201) {
                                // 미중복
                                userId.setTextColor(Color.rgb(111, 186, 119));
                                idChecked = true;
                            } else {
                                // 중복
                                userId.setTextColor(Color.rgb(252, 113, 80));
                            }
                        }
                    });
                } else {
                    // 포커스가 다시 바뀌는 경우, ID가 비어있는 경우를 방지
                    idChecked = false;
                }
            }
        });

        passwordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = userPassword.getText().toString();
                String confirm = s.toString();

                if (password.equals(confirm)) {
                    userPassword.setTextColor(Color.rgb(111, 186, 119));
                    passwordConfirm.setTextColor(Color.rgb(111, 186, 119));
                    passwordConfirmed = true;
                } else {
                    passwordConfirm.setTextColor(Color.rgb(252, 113, 80));
                    passwordConfirmed = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Email_Certified.emailDemanded && userNameChecked && idChecked && passwordConfirmed) {
                    Map<String, String> params = new HashMap<>();
                    Intent emailCertifiedIntent = getIntent();

                    String email = emailCertifiedIntent.getExtras().getString("email");
                    String name = userName.getText().toString();
                    String id = userId.getText().toString();
                    String password = userPassword.getText().toString();

                    params.put("email", email);
                    params.put("name", name);
                    params.put("id", id);
                    params.put("password", password);
                    params.put("registration_id", firebase.getFirebaseToken());

                    aQuery.ajax("http://52.79.134.200/signup", params, String.class, new AjaxCallback<String>() {
                        @Override
                        public void callback(String url, String response, AjaxStatus status) {
                            int statusCode = status.getCode();
                            if (statusCode == 201) {
                                Intent intent = new Intent(getApplicationContext(), SignIn.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "회원가입 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    // 체크가 모두 안되어 있을 경우
                }
            }
        });
    }
}
