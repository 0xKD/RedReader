package org.quantumbadger.redreader.image;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import org.quantumbadger.redreader.R;
import org.quantumbadger.redreader.account.RedditAccount;
import org.quantumbadger.redreader.activities.BugReportActivity;
import org.quantumbadger.redreader.cache.CacheManager;
import org.quantumbadger.redreader.cache.CacheRequest;
import org.quantumbadger.redreader.common.Constants;
import org.quantumbadger.redreader.common.General;
import org.quantumbadger.redreader.common.RRError;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;


public class ImageSaver extends CacheRequest {
	public static GetImageInfoListener newImageSaverInfoListener(final AppCompatActivity activity,
																 final RedditAccount account,
																 final String postUrl) {
		return new GetImageInfoListener() {
			@Override
			public void onFailure(final @CacheRequest.RequestFailureType int type, final Throwable t, final Integer status, final String readableMessage) {
				final RRError error = General.getGeneralErrorForFailure(activity, type, t, status, postUrl);
				General.showResultDialog(activity, error);
			}

			@Override
			public void onSuccess(final ImageInfo info) {
				ImageSaver imageSaver = new ImageSaver(General.uriFromString(info.urlOriginal), account, null,
						Constants.Priority.IMAGE_VIEW, 0, CacheRequest.DOWNLOAD_IF_NECESSARY,
						Constants.FileType.IMAGE, CacheRequest.DOWNLOAD_QUEUE_IMMEDIATE, false, false, activity, info);
				CacheManager.getInstance(activity).makeRequest(imageSaver);
			}

			@Override
			public void onNotAnImage() {
				General.quickToast(activity, R.string.selected_link_is_not_image);
			}
		};
	}

	private ImageInfo info;
	public ImageSaver(URI url, RedditAccount user, UUID requestSession, int priority, int listId,
						 @DownloadType int downloadType, int fileType, @DownloadQueueType int queueType,
						 boolean isJson, boolean cancelExisting, AppCompatActivity context, ImageInfo info) {
		super(url, user, requestSession, priority, listId, downloadType, fileType, queueType, isJson,
				cancelExisting, context);
		this.info = info;
	}

	@Override
	protected void onCallbackException(Throwable t) {
		BugReportActivity.handleGlobalError(context, t);
	}

	@Override
	protected void onDownloadNecessary() {
		General.quickToast(context, R.string.download_downloading);
	}

	@Override
	protected void onDownloadStarted() {

	}

	@Override
	protected void onFailure(@RequestFailureType int type, Throwable t, Integer status, String readableMessage) {
		final RRError error = General.getGeneralErrorForFailure(context, type, t, status, url.toString());
		General.showResultDialog((AppCompatActivity) context, error);
	}

	@Override
	protected void onProgress(boolean authorizationInProgress, long bytesRead, long totalBytes) {

	}

	@Override
	protected void onSuccess(CacheManager.ReadableCacheFile cacheFile, long timestamp, UUID session,
							 boolean fromCache, String mimetype) {
		String filename = General.filenameFromString(info.urlOriginal);
		File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File dst = new File(directory, filename);

		if (dst.exists()) {
			int count = 0;
			while (dst.exists()) {
				count++;
				dst = new File(directory, count + "_" + filename.substring(1));
			}
		}

		try {
			final InputStream cacheFileInputStream = cacheFile.getInputStream();
			if (cacheFileInputStream == null) {
				notifyFailure(CacheRequest.REQUEST_FAILURE_CACHE_MISS, null, null, "Could not find cached image");
				return;
			}
			General.copyFile(cacheFileInputStream, dst);
		} catch (IOException e) {
			notifyFailure(CacheRequest.REQUEST_FAILURE_STORAGE, e, null, "Could not copy file");
			return;
		}

		General.quickToast(context, context.getString(R.string.action_save_image_success) + " " + dst.getAbsolutePath());
	}
}
