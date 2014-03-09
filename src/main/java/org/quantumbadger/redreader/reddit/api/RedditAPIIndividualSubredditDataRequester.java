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

package org.quantumbadger.redreader.reddit.api;

import android.content.Context;
import org.apache.http.StatusLine;
import org.quantumbadger.redreader.account.RedditAccount;
import org.quantumbadger.redreader.cache.CacheManager;
import org.quantumbadger.redreader.cache.CacheRequest;
import org.quantumbadger.redreader.cache.RequestFailureType;
import org.quantumbadger.redreader.common.Constants;
import org.quantumbadger.redreader.common.TimestampBound;
import org.quantumbadger.redreader.io.CacheDataSource;
import org.quantumbadger.redreader.io.RequestResponseHandler;
import org.quantumbadger.redreader.jsonwrap.JsonValue;
import org.quantumbadger.redreader.reddit.things.raw.RawRedditSubreddit;
import org.quantumbadger.redreader.reddit.things.raw.RedditThing;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class RedditAPIIndividualSubredditDataRequester implements CacheDataSource<String, RawRedditSubreddit, SubredditRequestFailure> {

	private final Context context;
	private final RedditAccount user;

	public RedditAPIIndividualSubredditDataRequester(Context context, RedditAccount user) {
		this.context = context;
		this.user = user;
	}

	public void performRequest(final String key,
							   final TimestampBound timestampBound,
							   final RequestResponseHandler<RawRedditSubreddit, SubredditRequestFailure> handler) {

		final CacheRequest aboutSubredditCacheRequest = new CacheRequest(
				Constants.Reddit.getUri("/r/" + key + "/about.json"),
				user,
				null,
				Constants.Priority.API_SUBREDDIT_INVIDIVUAL,
				0,
				CacheRequest.DownloadType.FORCE,
				Constants.FileType.SUBREDDIT_ABOUT,
				true,
				true,
				false,
				context
		) {

			@Override
			protected void onCallbackException(Throwable t) {
				handler.onRequestFailed(new SubredditRequestFailure(t));
			}

			@Override protected void onDownloadNecessary() {}
			@Override protected void onDownloadStarted() {}
			@Override protected void onProgress(long bytesRead, long totalBytes) {}

			@Override
			protected void onFailure(RequestFailureType type, Throwable t, StatusLine status, String readableMessage) {
				handler.onRequestFailed(new SubredditRequestFailure(type, t, status, readableMessage));
			}

			@Override
			protected void onSuccess(CacheManager.ReadableCacheFile cacheFile, long timestamp, UUID session,
									 boolean fromCache, String mimetype) {}

			@Override
			public void onJsonParseStarted(JsonValue result, long timestamp, UUID session, boolean fromCache) {

				try {
					final RedditThing subredditThing = result.asObject(RedditThing.class);
					final RawRedditSubreddit subreddit = subredditThing.asSubreddit();
					subreddit.downloadTime = timestamp;
					handler.onRequestSuccess(subreddit, timestamp);

				} catch(Exception e) {
					handler.onRequestFailed(new SubredditRequestFailure(RequestFailureType.PARSE, e));
				}
			}
		};

		CacheManager.getInstance(context).makeRequest(aboutSubredditCacheRequest);
	}

	public void performRequest(Collection<String> keys,
							   TimestampBound timestampBound,
							   RequestResponseHandler<HashMap<String, RawRedditSubreddit>, SubredditRequestFailure> handler) {
		// TODO batch API? or just make lots of requests and build up a hash map?
		throw new UnsupportedOperationException();
	}

	public void performWrite(RawRedditSubreddit value) {
		throw new UnsupportedOperationException();
	}

	public void performWrite(Collection<RawRedditSubreddit> values) {
		throw new UnsupportedOperationException();
	}
}
