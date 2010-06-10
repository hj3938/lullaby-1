package net.sileht.lullaby;

/* Copyright (c) 20010 ABAAKOUK Mehdi  <theli48@gmail.com>
*
* +------------------------------------------------------------------------+
* | This program is free software; you can redistribute it and/or          |
* | modify it under the terms of the GNU General Public License            |
* | as published by the Free Software Foundation; either version 2         |
* | of the License, or (at your option) any later version.                 |
* |                                                                        |
* | This program is distributed in the hope that it will be useful,        |
* | but WITHOUT ANY WARRANTY; without even the implied warranty of         |
* | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          |
* | GNU General Public License for more details.                           |
* |                                                                        |
* | You should have received a copy of the GNU General Public License      |
* | along with this program; if not, write to the Free Software            |
* | Foundation, Inc., 59 Temple Place - Suite 330,                         |
* | Boston, MA  02111-1307, USA.                                           |
* +------------------------------------------------------------------------+
*/

import java.util.ArrayList;

import net.sileht.lullaby.R;
import net.sileht.lullaby.objects.Album;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class AlbumActivity extends Activity{

	private MatrixCursor albumsData;


	private SimpleCursorAdapter mAdapter;


	static class ViewHolder {
		TextView line1;
		TextView line2;
		ImageView play_indicator;
		ImageView icon;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.list_classic);
		ListView lv = (ListView) findViewById(R.id.list);
		ViewUtils e = new ViewUtils(this);
		lv.setOnItemClickListener(e);
		lv.setOnItemLongClickListener(e);
		lv.setOnCreateContextMenuListener(e);
		
		if (albumsData == null) {
			// Tell them we're loading

			setProgressBarVisibility(true);

			albumsData = new MatrixCursor(ViewUtils.mAlbumsColumnName);
			startManagingCursor(albumsData);

			AmpacheRequest request = new AmpacheRequest((Activity) this,
					new String[] { "albums", "" }) {
				@SuppressWarnings("unchecked")
				@Override
				public void add_objects(ArrayList list) {
					for (Album album : (ArrayList<Album>) list) {
						albumsData.newRow().add(album.id).add(album.name).add(
								album.artist).add(album.tracks).add(album.art);
						albumsData.requery();
					}
				}
			};
			request.send();
		}
		mAdapter = new AlbumsAdapter(this, albumsData);
		lv.setAdapter(mAdapter);
	}

	static class AlbumsAdapter extends SimpleCursorAdapter  implements SectionIndexer{

		private final BitmapDrawable mDefaultAlbumIcon;
		private final StringBuilder mBuffer = new StringBuilder();
		
        private AlphabetIndexer mIndexer;
        
		public AlbumsAdapter(Context context, Cursor cursor) {
			super(context, R.layout.track_list_item_child, cursor,new String[] {}, new int[] {});

			Resources r = context.getResources();
			mDefaultAlbumIcon = (BitmapDrawable) r
					.getDrawable(R.drawable.albumart_mp_unknown_list);
			// no filter or dither, it's a lot faster and we can't tell the
			// difference
			mDefaultAlbumIcon.setFilterBitmap(false);
			mDefaultAlbumIcon.setDither(false);

			mIndexer = new AlphabetIndexer(cursor, cursor.getColumnIndex(ViewUtils.ALBUM_NAME), 
                    r.getString(R.string.fast_scroll_numeric_alphabet));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = super.newView(context, cursor, parent);
			ViewHolder vh = new ViewHolder();
			vh.line1 = (TextView) v.findViewById(R.id.line1);
			vh.line2 = (TextView) v.findViewById(R.id.line2);
			vh.play_indicator = (ImageView) v.findViewById(R.id.play_indicator);
			vh.play_indicator.setImageDrawable(null);
			vh.icon = (ImageView) v.findViewById(R.id.icon);
			vh.icon.setBackgroundDrawable(mDefaultAlbumIcon);
			vh.icon.setPadding(0, 0, 1, 0);
			vh.icon.setImageDrawable(null);
			v.setTag(vh);
			return v;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			ViewHolder vh = (ViewHolder) view.getTag();

			String name = cursor.getString(cursor
					.getColumnIndexOrThrow(ViewUtils.ALBUM_NAME));

			int numsongs = cursor.getInt(cursor
					.getColumnIndexOrThrow(ViewUtils.ALBUM_TRACKS));
			
			String artist = cursor.getString(cursor
					.getColumnIndexOrThrow(ViewUtils.ALBUM_ARTIST));

			String displayname = name;
			boolean unknown = name == null;
			if (unknown) {
				displayname = "Unknown";
			}
			vh.line1.setText(displayname);

			final StringBuilder builder = mBuffer;
			builder.delete(0, builder.length());

			if (numsongs == 1) {
				builder.append("1 song");
			} else {
				builder.append(numsongs + " songs");
			}

			vh.line2.setText(artist+" - "+builder.toString());

			ImageView iv = vh.icon;

			// We don't actually need the path to the thumbnail file,
			// we just use it to see if there is album art or not
			String art = cursor.getString(cursor
					.getColumnIndexOrThrow(ViewUtils.ALBUM_ART));

			if (art != null & art.length() != 0) {
				Lullaby.cover.setCachedArtwork(iv, art);
			}

			/*
			 * long currentalbumid = MusicUtils.getCurrentAlbumId(); long aid =
			 * cursor.getLong(0);
			 * 
			 * iv = vh.play_indicator; if (currentalbumid == aid) {
			 * iv.setImageDrawable(mNowPlayingOverlay); } else {
			 * 
			 * iv.setImageDrawable(null); }
			 */
		}

		@Override
        public Object[] getSections() {
            return mIndexer.getSections();
        }

		@Override
        public int getPositionForSection(int sectionIndex) {
            return mIndexer.getPositionForSection(sectionIndex);
        }

		@Override
        public int getSectionForPosition(int position) {
            return 0;
        }
	}
}