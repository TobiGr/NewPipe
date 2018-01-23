package org.schabi.newpipe.tours;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.schabi.newpipe.BuildConfig;
import org.schabi.newpipe.R;

/**
 * Fragment containing information about the fixed in the current version
 */
public class ChangesFragment extends Fragment {

    // TODO add clicklistener to about fragment to open whatsNewFragment in a dialog

    private static final String ARG_ADDED = "added";
    private static final String ARG_IMPROVED = "improved";
    private static final String ARG_FIXED = "fixed";

    private String[] added;
    private String[] improved;
    private String[] fixed;

    public ChangesFragment() {}

    public static ChangesFragment newInstance(String[] added, String[] improved, String[] fixed) {
        ChangesFragment fragment = new ChangesFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_ADDED, added);
        args.putStringArray(ARG_IMPROVED, improved);
        args.putStringArray(ARG_FIXED, fixed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            added = getArguments().getStringArray(ARG_ADDED);
            improved = getArguments().getStringArray(ARG_IMPROVED);
            fixed = getArguments().getStringArray(ARG_FIXED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_whats_new, container, false);
        TextView titleView = rootView.findViewById(R.id.whats_new_title);
        titleView.setText(getContext().getString(R.string.whats_new_version,
                BuildConfig.VERSION_NAME.toString()) );

        LinearLayout changesView = rootView.findViewById(R.id.whats_new_changes);
        for(int i = 0; i < 3; i++) {
            String[] changes;
            TextView headingTextView = new TextView(getContext());
            headingTextView.setTextSize(18);
            headingTextView.setTypeface(headingTextView.getTypeface(), Typeface.BOLD);
            headingTextView.setTextColor(Color.WHITE);
            headingTextView.setPadding(0, 20, 0, 5);
            if (i == 0) {
                changes = added;
                headingTextView.setText(R.string.new_in_version);
                headingTextView.setPadding(0, 0, 0, 5);
            } else if (i == 1) {
                changes = improved;
                headingTextView.setText(R.string.improved_in_version);
            } else {
                changes = fixed;
                headingTextView.setText(R.string.fixed_in_version);
            }



            changesView.addView(headingTextView);

            for (final String change: changes) {
                TextView changeView = new TextView(getContext());
                changeView.setTextColor(Color.WHITE);
                changeView.setText(Html.fromHtml("&#8226; " + change));
                //changeView.setPadding();
                changesView.addView(changeView);
            }
        }


        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArray(ARG_ADDED, added);
        outState.putStringArray(ARG_IMPROVED, improved);
        outState.putStringArray(ARG_FIXED, fixed);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            added = savedInstanceState.getStringArray(ARG_ADDED);
            improved = savedInstanceState.getStringArray(ARG_IMPROVED);
            fixed = savedInstanceState.getStringArray(ARG_FIXED);
        }
    }

    // TODO remove onAttach and onDetach
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
