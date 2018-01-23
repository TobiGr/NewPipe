package org.schabi.newpipe.tours;

import android.content.Context;
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
 * Fragment containing information about the changes in the current version
 */
// TODO rename to ChangesFragment
public class WhatsNewFragment extends Fragment {

    // TODO add clicklistener to about fragment to open whatsNewFragment in a dialog

    private static final String ARG_CHANGES = "changes";
    private String[] changes;

    public WhatsNewFragment() {}

    public static WhatsNewFragment newInstance(String[] changes) {
        WhatsNewFragment fragment = new WhatsNewFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_CHANGES, changes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            changes = getArguments().getStringArray(ARG_CHANGES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_whats_new, container, false);
        TextView titleView = rootView.findViewById(R.id.whats_new_title);
        titleView.setText(getContext().getString(R.string.changes_in_version_title,
                BuildConfig.VERSION_NAME.toString()) );

        LinearLayout changesView = rootView.findViewById(R.id.whats_new_changes);
        for (final String change: changes) {
            TextView changeView = new TextView(getContext());
            changeView.setText(Html.fromHtml("&#8226; " + change));
            //changeView.setPadding();
            changesView.addView(changeView);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArray(ARG_CHANGES, changes);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            changes = savedInstanceState.getStringArray(ARG_CHANGES);
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
