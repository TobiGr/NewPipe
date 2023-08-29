package org.schabi.newpipe.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.schabi.newpipe.R;
import org.schabi.newpipe.databinding.PlaylistControlBinding;
import org.schabi.newpipe.fragments.list.playlist.PlaylistControlViewHolder;
import org.schabi.newpipe.player.PlayerType;
import org.schabi.newpipe.util.NavigationHelper;

public class PlaylistControlView extends LinearLayout {

    public PlaylistControlView(final Context context) {
        super(context);
    }

    public PlaylistControlView(final Context context, final AttributeSet attr) {
        super(context, attr);
    }

    public PlaylistControlView(final Context context, @Nullable final AttributeSet attrs,
                               final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void initListeners(final AppCompatActivity activity,
                              final PlaylistControlBinding playlistControlBinding,
                              final PlaylistControlViewHolder fragment,
                              final boolean showHoldToAppendTip) {
        playlistControlBinding.playlistCtrlPlayAllButton.setOnClickListener(view -> {
            NavigationHelper.playOnMainPlayer(activity, fragment.getPlayQueue());
            showHoldToAppendTipIfNeeded(activity, showHoldToAppendTip);
        });
        playlistControlBinding.playlistCtrlPlayPopupButton.setOnClickListener(view -> {
            NavigationHelper.playOnPopupPlayer(activity, fragment.getPlayQueue(), false);
            showHoldToAppendTipIfNeeded(activity, showHoldToAppendTip);
        });
        playlistControlBinding.playlistCtrlPlayBgButton.setOnClickListener(view -> {
            NavigationHelper.playOnBackgroundPlayer(activity, fragment.getPlayQueue(), false);
            showHoldToAppendTipIfNeeded(activity, showHoldToAppendTip);
        });

        playlistControlBinding.playlistCtrlPlayPopupButton.setOnLongClickListener(view -> {
            NavigationHelper.enqueueOnPlayer(activity, fragment.getPlayQueue(), PlayerType.POPUP);
            return true;
        });
        playlistControlBinding.playlistCtrlPlayBgButton.setOnLongClickListener(view -> {
            NavigationHelper.enqueueOnPlayer(activity, fragment.getPlayQueue(), PlayerType.AUDIO);
            return true;
        });
    }

    private void showHoldToAppendTipIfNeeded(final AppCompatActivity activity,
                                             final boolean show) {
        if (show && PreferenceManager.getDefaultSharedPreferences(activity)
                .getBoolean(activity.getString(R.string.show_hold_to_append_key), true)) {
            Toast.makeText(activity, R.string.hold_to_append, Toast.LENGTH_SHORT).show();
        }
    }
}
