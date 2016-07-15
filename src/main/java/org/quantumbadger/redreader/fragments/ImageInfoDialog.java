/*******************************************************************************
 * This file is part of RedReader.
 *
 * RedReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RedReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RedReader.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.quantumbadger.redreader.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import org.quantumbadger.redreader.R;
import org.quantumbadger.redreader.account.RedditAccount;
import org.quantumbadger.redreader.account.RedditAccountManager;
import org.quantumbadger.redreader.activities.BaseActivity;
import org.quantumbadger.redreader.common.Constants;
import org.quantumbadger.redreader.common.General;
import org.quantumbadger.redreader.common.LinkHandler;
import org.quantumbadger.redreader.image.ImageInfo;
import org.quantumbadger.redreader.image.ImageSaver;

public final class ImageInfoDialog extends PropertiesDialog implements
		DialogInterface.OnClickListener, BaseActivity.PermissionCallback {

	public static ImageInfoDialog newInstance(final ImageInfo info) {
		final ImageInfoDialog pp = new ImageInfoDialog();
		final Bundle args = new Bundle();
		args.putParcelable("info", info);
		pp.setArguments(args);
		return pp;
	}

	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				((BaseActivity) getActivity())
						.requestPermissionWithCallback(Manifest.permission.WRITE_EXTERNAL_STORAGE, this);
				break;
			default:
				break;
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog infoDialog = (AlertDialog) super.onCreateDialog(savedInstanceState);
		infoDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.action_save), this);
		return infoDialog;
	}

	@Override
	protected String getTitle(Context context) {
		return context.getString(R.string.props_image_title);
	}

	@Override
	protected void prepare(AppCompatActivity context, LinearLayout items) {

		final ImageInfo info = getArguments().getParcelable("info");
		boolean first = true;

		if(info.title != null && info.title.trim().length() > 0) {
			items.addView(propView(context, R.string.props_title, info.title.trim(), first));
			first = false;
		}

		if(info.caption != null && info.caption.trim().length() > 0) {
			items.addView(propView(context, R.string.props_caption, info.caption.trim(), first));
			first = false;
		}

		items.addView(propView(context, R.string.props_url, info.urlOriginal, first));

		if(info.width != null && info.height != null) {
			items.addView(propView(context, R.string.props_resolution, info.width + " x " + info.height, false));
		}
	}

	@Override
	public void onPermissionGranted() {
		ImageInfo info = getArguments().getParcelable("info");
		final RedditAccount anon = RedditAccountManager.getAnon();
		LinkHandler.getImageInfo(getContext(), info.urlOriginal,
				Constants.Priority.IMAGE_VIEW, 0,
				ImageSaver.newImageSaverInfoListener((BaseActivity) getActivity(), anon, info.urlOriginal));
	}

	@Override
	public void onPermissionDenied() {
		General.quickToast(getContext(), R.string.save_image_permission_denied);
	}
}
