package org.schabi.newpipe.report;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.json.JSONArray;
import org.json.JSONObject;
import org.schabi.newpipe.ActivityCommunicator;
import org.schabi.newpipe.BuildConfig;
import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.exceptions.ParsedDocumentInfo;
import org.schabi.newpipe.util.ThemeHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * Created by Christian Schabesberger on 24.10.15.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * ErrorActivity.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public class ErrorActivity extends AppCompatActivity {
    // LOG TAGS
    public static final String TAG = ErrorActivity.class.toString();
    // BUNDLE TAGS
    public static final String ERROR_INFO = "error_info";
    public static final String ERROR_LIST = "error_list";
    public static final String ERROR_DOCUMENT_LIST = "error_document_list";

    public static final String ERROR_EMAIL_ADDRESS = "crashreport@newpipe.schabi.org";
    public static final String ERROR_EMAIL_SUBJECT = "Exception in NewPipe " + BuildConfig.VERSION_NAME;

    public static final String ERROR_GITHUB_ISSUE_URL = "https://github.com/TeamNewPipe/NewPipe/issues";

    private String[] errorList;
    private ErrorInfo errorInfo;
    private String[] parsedDocumentList;
    private Class returnActivity;
    private String currentTimeStamp;
    private EditText userCommentBox;

    public static void reportUiError(final AppCompatActivity activity, final Throwable el) {
        reportError(activity, el, activity.getClass(), null,
                ErrorInfo.make(UserAction.UI_ERROR, "none", "", R.string.app_ui_crash));
    }

    public static void reportError(final Context context, final List<Throwable> el, final List<String> docs,
                                   final Class returnActivity, View rootView, final ErrorInfo errorInfo) {
        if (rootView != null) {
            Snackbar.make(rootView, R.string.error_snackbar_message, 3 * 1000)
                    .setActionTextColor(Color.YELLOW)
                    .setAction(R.string.error_snackbar_action, v ->
                            startErrorActivity(returnActivity, context, errorInfo, el, docs)).show();
        } else {
            startErrorActivity(returnActivity, context, errorInfo, el, docs);
        }
    }

    public static void reportError(final Context context, final List<Throwable> el,
                                   final Class returnActivity, View rootView, final ErrorInfo errorInfo) {
        List<String> docs = new ArrayList<>();
        for (Throwable e : el) {
            if (e instanceof ParsedDocumentInfo) {
                docs.add(((ParsedDocumentInfo) e).getDocument());
            } else {
                docs.add(null);
            }
        }

        reportError(context, el, docs, returnActivity, rootView, errorInfo);
    }

    private static void startErrorActivity(Class returnActivity, Context context, ErrorInfo errorInfo,
                                           List<Throwable> el, List<String> docs) {
        ActivityCommunicator ac = ActivityCommunicator.getCommunicator();
        ac.returnActivity = returnActivity;
        Intent intent = new Intent(context, ErrorActivity.class);
        intent.putExtra(ERROR_INFO, errorInfo);
        intent.putExtra(ERROR_LIST, elToSl(el));
        intent.putExtra(ERROR_DOCUMENT_LIST, docs.toArray(new String[0]));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void reportError(final Context context, final Throwable e,
                                   final Class returnActivity, View rootView, final ErrorInfo errorInfo) {
        List<Throwable> el = null;
        if (e != null) {
            el = new Vector<>();
            el.add(e);
        }
        reportError(context, el, returnActivity, rootView, errorInfo);
    }

    // async call
    public static void reportError(Handler handler, final Context context, final Throwable e,
                                   final Class returnActivity, final View rootView, final ErrorInfo errorInfo) {

        List<Throwable> el = null;
        if (e != null) {
            el = new Vector<>();
            el.add(e);
        }
        reportError(handler, context, el, returnActivity, rootView, errorInfo);
    }

    // async call
    public static void reportError(Handler handler, final Context context, final List<Throwable> el,
                                   final Class returnActivity, final View rootView, final ErrorInfo errorInfo) {
        handler.post(() -> reportError(context, el, returnActivity, rootView, errorInfo));
    }

    public static void reportError(final Context context, final CrashReportData report, final ErrorInfo errorInfo) {
        // get key first (don't ask about this solution)
        ReportField key = null;
        for (ReportField k : report.keySet()) {
            if (k.toString().equals("STACK_TRACE")) {
                key = k;
            }
        }
        String[] el = new String[]{report.get(key).toString()};

        Intent intent = new Intent(context, ErrorActivity.class);
        intent.putExtra(ERROR_INFO, errorInfo);
        intent.putExtra(ERROR_LIST, el);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    // errorList to StringList
    private static String[] elToSl(List<Throwable> stackTraces) {
        String[] out = new String[stackTraces.size()];
        for (int i = 0; i < stackTraces.size(); i++) {
            out[i] = getStackTrace(stackTraces.get(i));
        }
        return out;
    }

    private void openPrivacyPolicyDialog(Context context, String action, boolean appendDocument) {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.privacy_policy_title)
                .setMessage(R.string.start_accept_privacy_policy)
                .setCancelable(false)
                .setNeutralButton(R.string.read_privacy_policy, (dialog, which) -> {
                    Intent webIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(context.getString(R.string.privacy_policy_url))
                    );
                    context.startActivity(webIntent);
                })
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    if (action.equals("EMAIL")) { // send on email
                        Intent i = new Intent(Intent.ACTION_SENDTO);
                        i.setData(Uri.parse("mailto:" + ERROR_EMAIL_ADDRESS))
                                .putExtra(Intent.EXTRA_SUBJECT, ERROR_EMAIL_SUBJECT)
                                .putExtra(Intent.EXTRA_TEXT, buildJson(appendDocument));
                        startActivity(Intent.createChooser(i, "Send Email"));
                    } else if (action.equals("GITHUB")) { // open the NewPipe issue page on GitHub
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(ERROR_GITHUB_ISSUE_URL));
                        startActivity(Intent.createChooser(i, "Report issue on GitHub"));
                    }

                })
                .setNegativeButton(R.string.decline, (dialog, which) -> {
                    // do nothing
                })
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.setTheme(this);
        setContentView(R.layout.activity_error);

        Intent intent = getIntent();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.error_report_title);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        Button reportEmailButton = findViewById(R.id.errorReportEmailButton);
        Button copyButton = findViewById(R.id.errorReportCopyButton);
        Button reportGithubButton = findViewById(R.id.errorReportGitHubButton);
        LinearLayout includeDocumentsSwitchContainer = findViewById(R.id.includeDocumentsSwitchContainer);
        SwitchCompat includeDocumentsSwitch = findViewById(R.id.includeDocumentsSwitch);

        userCommentBox = findViewById(R.id.errorCommentBox);
        TextView errorView = findViewById(R.id.errorView);
        TextView infoView = findViewById(R.id.errorInfosView);
        TextView errorMessageView = findViewById(R.id.errorMessageView);

        ActivityCommunicator ac = ActivityCommunicator.getCommunicator();
        returnActivity = ac.returnActivity;
        errorInfo = intent.getParcelableExtra(ERROR_INFO);
        errorList = intent.getStringArrayExtra(ERROR_LIST);
        parsedDocumentList = intent.getStringArrayExtra(ERROR_DOCUMENT_LIST);

        // do not show documentsSwitch when no documents were passed to ErrorActivity
        if (parsedDocumentList == null || parsedDocumentList.length == 0) {
            includeDocumentsSwitchContainer.setVisibility(View.GONE);
        } else {
            boolean documentListIsEmpty = true;
            for (int i = 0; i < parsedDocumentList.length && documentListIsEmpty; i++) {
                if (parsedDocumentList[i] != null && parsedDocumentList[i].length() > 0)
                    documentListIsEmpty = false;
            }
            if (documentListIsEmpty) includeDocumentsSwitchContainer.setVisibility(View.GONE);
        }


        // important add guru meditation
        addGuruMeditation();
        currentTimeStamp = getCurrentTimeStamp();

        reportEmailButton.setOnClickListener((View v) -> {
            openPrivacyPolicyDialog(this, "EMAIL", false);
        });

        copyButton.setOnClickListener((View v) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(
                    "NewPipe error report",
                    buildMarkdown(includeDocumentsSwitch.isChecked()));
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.msg_copied, Toast.LENGTH_SHORT).show();
        });

        reportGithubButton.setOnClickListener((View v) -> {
            openPrivacyPolicyDialog(this, "GITHUB", includeDocumentsSwitch.isChecked());
        });

        includeDocumentsSwitchContainer.setOnClickListener((View v) -> {
            includeDocumentsSwitch.toggle();
        });

        // normal bugreport
        buildInfo(errorInfo);
        if (errorInfo.message != 0) {
            errorMessageView.setText(errorInfo.message);
        } else {
            errorMessageView.setVisibility(View.GONE);
            findViewById(R.id.messageWhatHappenedView).setVisibility(View.GONE);
        }

        errorView.setText(formErrorText(errorList));

        //print stack trace once again for debugging:
        for (String e : errorList) {
            Log.e(TAG, e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.error_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                goToReturnActivity();
                break;
            case R.id.menu_item_share_error: {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, buildJson());
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, getString(R.string.share_dialog_title)));
            }
            break;
        }
        return false;
    }

    private String formErrorText(String[] el) {
        StringBuilder text = new StringBuilder();
        if (el != null) {
            for (int i = 0; i < el.length; i++) {
                text.append(el[i]);
                if (i != el.length - 1) {
                    text.append("-------------------------------------\n");
                }
            }
        }
        return text.toString();
    }

    /**
     * Get the checked activity.
     *
     * @param returnActivity the activity to return to
     * @return the casted return activity or null
     */
    @Nullable
    static Class<? extends Activity> getReturnActivity(Class<?> returnActivity) {
        Class<? extends Activity> checkedReturnActivity = null;
        if (returnActivity != null) {
            if (Activity.class.isAssignableFrom(returnActivity)) {
                checkedReturnActivity = returnActivity.asSubclass(Activity.class);
            } else {
                checkedReturnActivity = MainActivity.class;
            }
        }
        return checkedReturnActivity;
    }

    private void goToReturnActivity() {
        Class<? extends Activity> checkedReturnActivity = getReturnActivity(returnActivity);
        if (checkedReturnActivity == null) {
            super.onBackPressed();
        } else {
            Intent intent = new Intent(this, checkedReturnActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            NavUtils.navigateUpTo(this, intent);
        }
    }

    private void buildInfo(ErrorInfo info) {
        TextView infoLabelView = findViewById(R.id.errorInfoLabelsView);
        TextView infoView = findViewById(R.id.errorInfosView);
        String text = "";

        infoLabelView.setText(getString(R.string.info_labels).replace("\\n", "\n"));

        text += getUserActionString(info.userAction)
                + "\n" + info.request
                + "\n" + getContentCountryString()
                + "\n" + getContentLanguageString()
                + "\n" + info.serviceName
                + "\n" + currentTimeStamp
                + "\n" + getPackageName()
                + "\n" + BuildConfig.VERSION_NAME
                + "\n" + getOsString();

        infoView.setText(text);
    }

    private String buildJson() {
        return buildJson(false);
    }

    private String buildJson(boolean appendDocuments) {
        JSONObject errorObject = new JSONObject();

        try {
            errorObject.put("user_action", getUserActionString(errorInfo.userAction))
                    .put("request", errorInfo.request)
                    .put("content_country", getContentCountryString())
                    .put("content_language", getContentCountryString())
                    .put("service", errorInfo.serviceName)
                    .put("package", getPackageName())
                    .put("version", BuildConfig.VERSION_NAME)
                    .put("os", getOsString())
                    .put("time", currentTimeStamp);

            JSONArray exceptionArray = new JSONArray();
            if (errorList != null) {
                for (String e : errorList) {
                    exceptionArray.put(e);
                }
            }

            JSONArray exceptions = new JSONArray();
            for (int i = 0; i < exceptionArray.length(); i++) {
                JSONObject exception = new JSONObject();
                exception.put("stacktrace", errorList[i]);
                exception.put("document", !appendDocuments || parsedDocumentList[i] == null ? "" : parsedDocumentList[i]);
                exceptions.put(exception);
            }
            errorObject.put("exceptions", exceptions);
            errorObject.put("user_comment", userCommentBox.getText().toString());

            return errorObject.toString(3);
        } catch (Throwable e) {
            Log.e(TAG, "Error while erroring: Could not build json");
            e.printStackTrace();
        }

        return "";
    }

    private String buildMarkdown(boolean appendDocuments) {
        StringBuilder htmlErrorReport = new StringBuilder();
        // basic error info
        htmlErrorReport
                .append("## Exception")
                .append("\n* __User Action:__ ").append(getUserActionString(errorInfo.userAction))
                .append("\n* __Request:__ ").append(errorInfo.request)
                .append("\n* __Content Country:__ ").append(getContentCountryString())
                .append("\n* __Content Language:__ ").append(getContentLanguageString())
                .append("\n* __Service:__ ").append(errorInfo.serviceName)
                .append("\n* __Version:__ ").append(BuildConfig.VERSION_NAME)
                .append("\n* __OS:__ ").append(getOsString())
                .append(userCommentBox.getText().toString()).append("\n");

        // collapse all logs to a single paragraph when there are more than one to keep GitHub issue clean
        if (errorList.length > 1) {
            htmlErrorReport
                    .append("<details><summary><b>Exceptions (")
                    .append(errorList.length + 1)
                    .append(")</b></summary><p>\n");
        }

        // most importantly, don't forget the crash logs ^^
        for (int i = 0; i < errorList.length; i++) {
            boolean appendDocument = appendDocuments && parsedDocumentList[i] != null
                    && parsedDocumentList[i].length() > 0;
            htmlErrorReport
                    .append("<details><summary><b>Crash log ").append(i + 1).append("</b>")
                    .append((appendDocument ? " with parsed document" : ""))
                    .append("</summary><p>\n")
                    .append("\n```\n").append(errorList[i]).append("\n```\n");

            if (appendDocument) {
                htmlErrorReport
                        .append("\n```HTML\n")
                        .append(parsedDocumentList[i])
                        .append("\n```\n");
            }
            htmlErrorReport.append("</details>\n");
        }

        // make sure to close everything
        if (errorList.length > 1) {
            htmlErrorReport.append("</p></details>\n");
        }
        htmlErrorReport.append("<hr>\n");

        return htmlErrorReport.toString();
    }

    private String getUserActionString(UserAction userAction) {
        if (userAction == null) {
            return "Your description is in another castle.";
        } else {
            return userAction.getMessage();
        }
    }

    private String getContentCountryString() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(this.getString(R.string.content_country_key), "none");
    }


    private String getContentLanguageString() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(this.getString(R.string.content_language_key), "none");
    }

    private String getOsString() {
        String osBase = Build.VERSION.SDK_INT >= 23 ? Build.VERSION.BASE_OS : "Android";
        return System.getProperty("os.name")
                + " " + (osBase.isEmpty() ? "Android" : osBase)
                + " " + Build.VERSION.RELEASE
                + " - " + Integer.toString(Build.VERSION.SDK_INT);
    }

    private void addGuruMeditation() {
        //just an easter egg
        TextView sorryView = findViewById(R.id.errorSorryView);
        String text = sorryView.getText().toString();
        text += "\n" + getString(R.string.guru_meditation);
        sorryView.setText(text);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        goToReturnActivity();
    }

    public String getCurrentTimeStamp() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date());
    }

    public static class ErrorInfo implements Parcelable {
        public static final Parcelable.Creator<ErrorInfo> CREATOR = new Parcelable.Creator<ErrorInfo>() {
            @Override
            public ErrorInfo createFromParcel(Parcel source) {
                return new ErrorInfo(source);
            }

            @Override
            public ErrorInfo[] newArray(int size) {
                return new ErrorInfo[size];
            }
        };
        final public UserAction userAction;
        final public String request;
        final public String serviceName;
        @StringRes
        final public int message;

        private ErrorInfo(UserAction userAction, String serviceName, String request, @StringRes int message) {
            this.userAction = userAction;
            this.serviceName = serviceName;
            this.request = request;
            this.message = message;
        }

        protected ErrorInfo(Parcel in) {
            this.userAction = UserAction.valueOf(in.readString());
            this.request = in.readString();
            this.serviceName = in.readString();
            this.message = in.readInt();
        }

        public static ErrorInfo make(UserAction userAction, String serviceName, String request, @StringRes int message) {
            return new ErrorInfo(userAction, serviceName, request, message);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.userAction.name());
            dest.writeString(this.request);
            dest.writeString(this.serviceName);
            dest.writeInt(this.message);
        }
    }
}
