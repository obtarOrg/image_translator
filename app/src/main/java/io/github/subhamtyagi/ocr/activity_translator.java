package io.github.subhamtyagi.ocr;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.PermissionRequest;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.load.Key;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.androidannotations.rest.spring.api.MediaType;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class activity_translator extends AppCompatActivity implements TextToSpeech.OnInitListener{

    AlertDialog.Builder builder1;
    DatabaseHelper dbHelper;
    AlertDialog dialog1;
    LayoutInflater inflater;
    ImageView ivspeak;
    public ProgressDialog mProgressDialog;
    public EditText f210qu, f211rt;
    String speakfromlangcode = "en-IN";
    String speaktolangcode = "en-IN";
    Spinner spinner, spinner2;
    int status;
    private View parent_view;
    public TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translator);

        parent_view = findViewById(android.R.id.content);
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        this.dbHelper = databaseHelper;
        databaseHelper.openDataBase();
        initTranslator();
        this.builder1 = new AlertDialog.Builder(this);
        this.inflater = getLayoutInflater();

        this.ivspeak = (ImageView) findViewById(R.id.ivspeak);
        this.spinner = (Spinner) findViewById(R.id.fspinner);
        this.spinner2 = (Spinner) findViewById(R.id.sspinner);
        this.f210qu = (EditText) findViewById(R.id.querytext);
        this.f211rt = (EditText) findViewById(R.id.resulttext);


        this.ivspeak.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= 23) {
                    activity_translator.this.askPermission("1");
                } else {

                    activity_translator activity_translator = activity_translator.this;
                    activity_translator.speakfromlangcode = Languages.getSpeakLangCode(activity_translator.spinner.getSelectedItemPosition());
                    activity_translator.this.trans();

                }
            }
        });


        findViewById(R.id.imgHistory).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(activity_translator.this, HistoryActivity.class);
                startActivity(intent);
            }
        });


        ArrayList arrayList = new ArrayList();
        Collections.addAll(arrayList, Languages.getSpeakLang());
        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_display, arrayList);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_drop_down);
        this.spinner.setAdapter(arrayAdapter);
        this.spinner.setSelection(14);
        this.spinner2.setAdapter(arrayAdapter);
        this.spinner2.setSelection(6);
        this.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
                activity_translator activity_translator = activity_translator.this;
                activity_translator.speakfromlangcode = Languages.getSpeakLangCode(activity_translator.spinner.getSelectedItemPosition());
                ((TextView) adapterView.getChildAt(0)).setTextColor(activity_translator.this.getResources().getColor(R.color.colorPrimary));
            }
        });


        this.spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
                activity_translator activity_translator = activity_translator.this;
                activity_translator.speaktolangcode = Languages.getSpeakLangCode(activity_translator.spinner2.getSelectedItemPosition());
                ((TextView) adapterView.getChildAt(0)).setTextColor(activity_translator.this.getResources().getColor(R.color.colorPrimary));
            }
        });


        findViewById(R.id.imageView2).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                activity_translator.this.f210qu.setText("");
                activity_translator.this.f211rt.setText("");
                int selectedItemPosition = activity_translator.this.spinner.getSelectedItemPosition();
                activity_translator.this.spinner.setSelection(activity_translator.this.spinner2.getSelectedItemPosition());
                activity_translator.this.spinner2.setSelection(selectedItemPosition);
            }
        });
        String stringExtra = getIntent().getStringExtra("text");
        this.f210qu.setText(stringExtra);
        this.f211rt.setMovementMethod(new ScrollingMovementMethod());


        findViewById(R.id.translate).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (activity_translator.this.f210qu.length() > 0) {
                    activity_translator activity_translator = activity_translator.this;
                    activity_translator.hideKeyboard(activity_translator);
                    ProgressDialog unused = activity_translator.this.mProgressDialog = new ProgressDialog(activity_translator.this);
                    activity_translator.this.mProgressDialog.setMessage("Translating");
                    activity_translator.this.mProgressDialog.setProgressStyle(0);
                    activity_translator.this.mProgressDialog.setCancelable(false);
                    activity_translator.this.mProgressDialog.show();
                    try {
                        String encode = URLEncoder.encode(activity_translator.this.f210qu.getText().toString().trim(), Key.STRING_CHARSET_NAME);
                        ReadJSONFeedTask readJSONFeedTask = new ReadJSONFeedTask();
                        readJSONFeedTask.execute("https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + Languages.getsptrCode(activity_translator.this.spinner.getSelectedItemPosition()) + "&tl=" + Languages.getsptrCode(activity_translator.this.spinner2.getSelectedItemPosition()) + "&dt=t&ie=UTF-8&oe=UTF-8&q=" + encode);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    Snackbar.make(parent_view, "Enter Text", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.clearall).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                activity_translator.this.f210qu.setText("");
                activity_translator.this.f211rt.setText("");
            }
        });


        findViewById(R.id.copyp).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            public void onClick(View view) {
                if (activity_translator.this.f210qu.length() > 0) {
                    ((ClipboardManager) activity_translator.this.getApplicationContext().getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("text", activity_translator.this.f210qu.getText()));
                    Snackbar.make(parent_view, "Text Copied", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Snackbar.make(parent_view, "No Text!", Snackbar.LENGTH_SHORT).show();

            }
        });

        findViewById(R.id.copyq).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            public void onClick(View view) {
                if (activity_translator.this.f211rt.length() > 0) {
                    ((ClipboardManager) activity_translator.this.getApplicationContext().getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("text", activity_translator.this.f211rt.getText()));
                    Snackbar.make(parent_view, "Text Copied", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Snackbar.make(parent_view, "No Text!", Snackbar.LENGTH_SHORT).show();
            }
        });


        findViewById(R.id.speakt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (activity_translator.this.status == -1 || activity_translator.this.status == -2) {
                    Snackbar.make(parent_view, "This Language is not supported.", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                activity_translator.this.tts.speak(activity_translator.this.f211rt.getText().toString(), 0, (HashMap) null);

            }
        });
        findViewById(R.id.sharet).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (activity_translator.this.f211rt.length() > 0) {
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType(MediaType.TEXT_PLAIN);
                    intent.putExtra("android.intent.extra.TEXT", activity_translator.this.f211rt.getText().toString());
                    activity_translator.this.startActivity(Intent.createChooser(intent, "Share with"));
                    return;
                }
                Snackbar.make(parent_view, "No Text!", Snackbar.LENGTH_SHORT).show();

            }
        });
    }

    private void initTranslator() {
        this.tts = new TextToSpeech(this, this);
    }

    public void hideKeyboard(Activity activity) {
        @SuppressLint("WrongConstant") InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService("input_method");
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus == null) {
            currentFocus = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }

    public void onInit(int i) {
        if (this.status == 0) {
            int language = this.tts.setLanguage(new Locale(this.speakfromlangcode));
            if (language == -1 || language == -2) {
                Log.e("TTS", "This Language is not supported");
            } else {
                Log.e("TTS", "This Language is supported");
            }
        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }


    public void trans() {
        SpeechRecognizer createSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent intent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
        intent.putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form");
        intent.putExtra("android.speech.extra.LANGUAGE", this.speakfromlangcode);
        intent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", this.speakfromlangcode);
        intent.putExtra("android.speech.extra.MAX_RESULTS", 10);
        intent.putExtra("calling_package", getPackageName());
        createSpeechRecognizer.startListening(intent);
        createSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            public void onBeginningOfSpeech() {
            }

            public void onBufferReceived(byte[] bArr) {
            }

            public void onEndOfSpeech() {
            }

            public void onEvent(int i, Bundle bundle) {
            }

            public void onPartialResults(Bundle bundle) {
            }

            public void onRmsChanged(float f) {
            }

            public void onReadyForSpeech(Bundle bundle) {
                activity_translator.this.builder1.setView(activity_translator.this.inflater.inflate(R.layout.speakdialog, (ViewGroup) null));
                activity_translator text_activity = activity_translator.this;
                text_activity.dialog1 = text_activity.builder1.create();
                activity_translator.this.dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                activity_translator.this.dialog1.show();
                ((TextView) activity_translator.this.dialog1.findViewById(R.id.dlang)).setText(Languages.getSpeakLang()[activity_translator.this.spinner.getSelectedItemPosition()]);
            }

            public void onError(int i) {
                String str;

                switch (i) {
                    case 2:
                        str = "Network Error, Please Check Your Internet";
                        break;
                    case 3:
                        str = "Audio Error, Please Try Again";
                        break;
                    case 4:
                        str = "I Can not Connect To Server";
                        break;
                    case 5:
                        str = "I Can't Hear You, Please Try Again";
                        break;
                    case 6:
                        str = " I Can't Hear You, Please Try Again";
                        break;
                    case 7:
                        str = "I Don't Understand, Please Try Again";
                        break;
                    case 8:
                        str = "Recognizer Busy, Please Try Later";
                        break;
                    default:
                        str = "Didn't understand, please try again.";
                        break;
                }
                Snackbar.make(parent_view, "Network Error, Please Check Your Internet.", Snackbar.LENGTH_SHORT).show();


                if (activity_translator.this.dialog1 != null) {
                    activity_translator.this.dialog1.dismiss();
                }
            }

            public void onResults(Bundle bundle) {
                ArrayList<String> stringArrayList = bundle.getStringArrayList("results_recognition");
                activity_translator.this.dialog1.dismiss();
                activity_translator.this.f210qu.setText(stringArrayList.get(0).trim());

            }
        });
    }


    public void onDestroy() {

        TextToSpeech textToSpeech = this.tts;
        if (textToSpeech != null) {
            textToSpeech.stop();
            this.tts.shutdown();
        }
        super.onDestroy();
    }

    public void onPause() {

        TextToSpeech textToSpeech = this.tts;
        if (textToSpeech != null) {
            textToSpeech.stop();
            this.tts.shutdown();
        }
        super.onPause();
    }


    private class ReadJSONFeedTask extends AsyncTask<String, Void, String> {
        private ReadJSONFeedTask() {
        }

        public void onPreExecute() {
            super.onPreExecute();
        }

        public String doInBackground(String... strArr) {
            return activity_translator.this.readJSONFeed(strArr[0]);
        }

        public void onPostExecute(String str) {
            activity_translator.this.mProgressDialog.dismiss();
            if (str.equals("[\"ERROR\"]")) {
                Snackbar.make(parent_view, "Network Error, Please Check Your Internet.", Snackbar.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONArray jSONArray = new JSONArray(str);
                String str2 = "";
                for (int i = 0; i < jSONArray.getJSONArray(0).length(); i++) {
                    str2 = str2 + jSONArray.getJSONArray(0).getJSONArray(i).getString(0);
                }
                activity_translator.this.f211rt.setText(str2);
                activity_translator.this.dbHelper.addSearchHistory(activity_translator.this.spinner.getSelectedItem().toString(), activity_translator.this.f210qu.getText().toString().trim(), activity_translator.this.spinner2.getSelectedItem().toString(), activity_translator.this.f211rt.getText().toString().trim());
                activity_translator.this.speaktolangcode = Languages.getSpeakLangCode(activity_translator.this.spinner2.getSelectedItemPosition());
                activity_translator.this.status = activity_translator.this.tts.setLanguage(new Locale(activity_translator.this.speaktolangcode));
                activity_translator.this.tts.setLanguage(new Locale(activity_translator.this.speaktolangcode));
            } catch (Exception e) {

            }
        }
    }

    public String readJSONFeed(String str) {
        StringBuilder sb = new StringBuilder();
        try {
            HttpResponse execute = new DefaultHttpClient().execute(new HttpGet(str));
            if (execute.getStatusLine().getStatusCode() == 200) {
                InputStream content = execute.getEntity().getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content));
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    sb.append(readLine);
                }
                content.close();
            } else {

            }
        } catch (Exception e) {
            sb.append("[\"ERROR\"]");
        }
        return sb.toString();
    }

    public void askPermission(final String str) {
        Dexter.withActivity(this).withPermissions("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO").withListener(new MultiplePermissionsListener() {
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                if (!multiplePermissionsReport.areAllPermissionsGranted()) {
                    return;
                }
                if (str.equals("1")) {
                    activity_translator activity_translator = activity_translator.this;
                    activity_translator.speakfromlangcode = Languages.getSpeakLangCode(activity_translator.spinner.getSelectedItemPosition());
                    activity_translator.this.trans();
                }
            }

            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }


}
