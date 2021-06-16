package com.cinder92.musicfiles;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import android.util.Log;
import android.content.ContentValues;
import android.content.ContentUris;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import wseemann.media.FFmpegMediaMetadataRetriever;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.cinder92.musicfiles.ReactNativeFileManager;

import org.farng.mp3.MP3File;


public class RNReactNativeGetMusicFilesModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private boolean getBluredImages = false;
    private boolean getArtistFromSong = false;
    private boolean getDurationFromSong = true;
    private boolean getTitleFromSong = true;
    private boolean getIDFromSong = false;
    private boolean getCoverFromSong = false;
    private boolean getGenreFromSong = false;
    private boolean getAlbumFromSong = true;
    private boolean getDateFromSong = false;
    private boolean getCommentsFromSong = false;
    private boolean getLyricsFromSong = false;
    private boolean getDisplayNameFromSong = false;
    private boolean getIsDownloadFromSong = false;
    private boolean getAlbumArtistFromSong = false;
    private boolean getAuthorFromSong = false;
    private int minimumSongDuration = 0;
    private int songsPerIteration = 0;
    private int version = Build.VERSION.SDK_INT;
    private String[] STAR = { "*" };

    public RNReactNativeGetMusicFilesModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNReactNativeGetMusicFiles";
    }

    @ReactMethod
    public void getAll(ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        if (options.hasKey("blured")) {
            getBluredImages = options.getBoolean("blured");
        }

        if (options.hasKey("author")) {
            getAuthorFromSong = options.getBoolean("author");
        }

        if (options.hasKey("artist")) {
            getArtistFromSong = options.getBoolean("artist");
        }

        if (options.hasKey("duration")) {
            getDurationFromSong = options.getBoolean("duration");
        }

        if (options.hasKey("title")) {
            getTitleFromSong = options.getBoolean("title");
        }

        if (options.hasKey("id")) {
            getIDFromSong = options.getBoolean("id");
        }

        if (options.hasKey("cover")) {
            getCoverFromSong = options.getBoolean("cover");
        }

        if (options.hasKey("genre")) {
            getGenreFromSong = options.getBoolean("genre");
        }

        if (options.hasKey("album")) {
            getAlbumFromSong = options.getBoolean("album");
        }

        if (options.hasKey("displayName")) {
            getDisplayNameFromSong = options.getBoolean("displayName");
        }

        if (options.hasKey("isDownload")) {
            getIsDownloadFromSong = options.getBoolean("isDownload");
        }

        if (options.hasKey("albumArtist")) {
            getAlbumArtistFromSong = options.getBoolean("albumArtist");
        }

        /*if (options.hasKey("date")) {
            getDateFromSong = options.getBoolean("date");
        }
        if (options.hasKey("comments")) {
            getCommentsFromSong = options.getBoolean("comments");
        }
        if (options.hasKey("lyrics")) {
            getLyricsFromSong = options.getBoolean("lyrics");
        }*/

        if (options.hasKey("batchNumber")) {
            songsPerIteration = options.getInt("batchNumber");
        }

        if (options.hasKey("minimumSongDuration") && options.getInt("minimumSongDuration") > 0) {
            minimumSongDuration = options.getInt("minimumSongDuration");
        } else {
            minimumSongDuration = 0;
        }

        if (version <= 19){
            getSongs(successCallback,errorCallback);
        } else {
            Thread bgThread = new Thread(null,
                    new Runnable() {
                        @Override
                        public void run() {
                            getSongs(successCallback,errorCallback);
                        }
                    }, "asyncTask", 1024
            );
            bgThread.start();
        }
    }

    private void getSongs(final Callback successCallback, final Callback errorCallback){
        ContentResolver musicResolver = getCurrentActivity().getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        if(minimumSongDuration > 0){
            selection += " AND " + MediaStore.Audio.Media.DURATION + " >= " + minimumSongDuration;
        }

        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, null, sortOrder);

        //Log.i("Tienes => ",Integer.toString(musicCursor.getCount()));

        int pointer = 0;

        if (musicCursor != null && musicCursor.moveToFirst()) {

            if (musicCursor.getCount() > 0) {
                WritableArray jsonArray = new WritableNativeArray();
                WritableMap items;


                //FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();

                try {
                    do {
                        try {
                            WritableNativeMap item = getSongsData(musicCursor, mmr);
                            jsonArray.pushMap(item);

                            if (songsPerIteration > 0) {

                                if (songsPerIteration > musicCursor.getCount()) {
                                    if (pointer == (musicCursor.getCount() - 1)) {
                                        WritableMap params = Arguments.createMap();
                                        params.putArray("batch", jsonArray);
                                        sendEvent(reactContext, "onBatchReceived", params);
                                    }
                                } else {
                                    if (songsPerIteration == jsonArray.size()) {
                                        WritableMap params = Arguments.createMap();
                                        params.putArray("batch", jsonArray);
                                        sendEvent(reactContext, "onBatchReceived", params);
                                        jsonArray = new WritableNativeArray();
                                    } else if (pointer == (musicCursor.getCount() - 1)) {
                                        WritableMap params = Arguments.createMap();
                                        params.putArray("batch", jsonArray);
                                        sendEvent(reactContext, "onBatchReceived", params);
                                    }
                                }

                                pointer++;
                            }
                        } catch (Exception e) {
                            // An error in one message should not prevent from getting the rest
                            // There are cases when a corrupted file can't be read and a RuntimeException is raised

                            // Let's discuss how to deal with these kind of exceptions
                            // This song will be ignored, and incremented the pointer in order to this plugin work
                            pointer++;

                            continue; // This is redundant, but adds meaning
                        }

                    } while (musicCursor.moveToNext());

                    if (songsPerIteration == 0) {
                        successCallback.invoke(jsonArray);
                    }

                } catch (RuntimeException e) {
                    errorCallback.invoke(e.toString());
                } catch (Exception e) {
                    errorCallback.invoke(e.getMessage());
                } finally {
                    mmr.release();
                }
            }else{
                Log.i("com.tests","Error, you dont' have any songs");
                successCallback.invoke("Error, you dont' have any songs");
            }
        }else{
            Log.i("com.tests","Something get wrong with musicCursor");
            errorCallback.invoke("Something get wrong with musicCursor");
        }
    }

    private WritableNativeMap getSongsData(Cursor musicCursor, MediaMetadataRetriever mmr) {
        WritableNativeMap items = new WritableNativeMap();

        long songId = musicCursor.getLong(musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID));

        if (getIDFromSong) {
            items.putString("id", String.valueOf(songId));
        }

        String songPath = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        //MP3File mp3file = new MP3File(songPath);

        Log.e("musica",songPath);

        if (songPath != null && songPath != "") {

            String fileName = songPath.substring(songPath.lastIndexOf("/") + 1);

            //by default, always return path and fileName
            items.putString("path", songPath);
            items.putString("fileName", fileName);

            try {
                mmr.setDataSource(songPath);
            } catch (Exception e) {
                Log.e("embedImage","No embed image");
            }

            //String songTimeDuration = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
            // int songIntDuration = Integer.parseInt(songTimeDuration);

            if (getAlbumFromSong) {
                String album = mmr.extractMetadata(mmr.METADATA_KEY_ALBUM);
                if (album != null) {
                    items.putString("album", album);
                }
                String albumId = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                if (albumId != null) {
                    items.putString("albumId", albumId);
                }
            }

            if (getArtistFromSong) {
                String artist = mmr.extractMetadata(mmr.METADATA_KEY_ARTIST);
                if (artist != null) {
                    items.putString("artist", artist);
                }
                String artistId = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
                if (artistId != null) {
                    items.putString("artistId", artistId);
                }
            }

            if (getAuthorFromSong) {
                String author = mmr.extractMetadata(mmr.METADATA_KEY_AUTHOR);
                if (author != null) {
                    items.putString("author", author);
                }
            }

            if (getAlbumArtistFromSong) {
                String albumArtist = mmr.extractMetadata(mmr.METADATA_KEY_ALBUMARTIST);
                if (albumArtist != null) {
                    items.putString("albumArtist", albumArtist);
                }
            }

            if (getTitleFromSong) {
                String title = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                if (title != null) {
                    items.putString("title", title);
                }
            }

            if (getGenreFromSong) {
                String genre = mmr.extractMetadata(mmr.METADATA_KEY_GENRE);
                if (genre != null) {
                    items.putString("genre", genre);
                }
            }

            if (getDurationFromSong) {
                items.putString("duration", mmr.extractMetadata(mmr.METADATA_KEY_DURATION));
            }

            if (getDisplayNameFromSong) {
                String displayName = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                if (displayName != null) {
                    items.putString("displayName", displayName);
                }
            }

            if (getCoverFromSong) {

                ReactNativeFileManager fcm = new ReactNativeFileManager();

                String encoded = "";
                String blurred = "";
                try {
                    byte[] albumImageData = mmr.getEmbeddedPicture();

                    if (albumImageData != null) {
                        Bitmap songImage = BitmapFactory.decodeByteArray(albumImageData, 0, albumImageData.length);

                        try {
                            String pathToImg = Environment.getExternalStorageDirectory() + "/" + songId + ".jpg";
                            encoded = fcm.saveImageToStorageAndGetPath(pathToImg, songImage);
                            items.putString("cover", "file://" + encoded);
                        } catch (Exception e) {
                            // Just let images empty
                            Log.e("error in image", e.getMessage());
                        }

                        if (getBluredImages) {
                            try {
                                String pathToImg = Environment.getExternalStorageDirectory() + "/" + songId + "-blur.jpg";
                                blurred = fcm.saveBlurImageToStorageAndGetPath(pathToImg, songImage);
                                items.putString("blur", "file://" + blurred);
                            } catch (Exception e) {
                                Log.e("error in image-blured", e.getMessage());
                            }
                        }
                    }
                }catch (Exception e) {
                    Log.e("embedImage","No embed image");
                }
            }
        }

        return items;
    }

    @ReactMethod
    public void addNewPlaylist(ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        if (options.hasKey("name")) {
            addNewPlaylist(options.getString("name"), successCallback, errorCallback);
        }
    }

    private void addNewPlaylist(String name, final Callback successCallback, final Callback errorCallback) {
        ContentResolver resolver = getCurrentActivity().getContentResolver();
        final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        final String[] columns = { MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME };
        final Cursor playlistCursor = resolver.query(uri, columns, null, null, null);
        try {
            if (playlistCursor != null && playlistCursor.moveToFirst()) {
                WritableArray jsonArray = new WritableNativeArray();
                WritableMap items;
                if (playlistCursor.getCount() > 0) {
                    items = new WritableNativeMap();
                    items.putString("playlistId", playlistCursor.getString(playlistCursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.PLAYLIST_ID)));
                    items.putString("playOrder", playlistCursor.getString(playlistCursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.PLAY_ORDER)));
                    jsonArray.pushMap(items);
                }
                successCallback.invoke(jsonArray);
            }
        } catch (RuntimeException e) {
            errorCallback.invoke(e.toString());
        } catch (Exception e) {
            errorCallback.invoke(e.getMessage());
        } finally {
        }
    }

    @ReactMethod
    public void addSoundToPlaylist(ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        if (options.hasKey("playlistId") && options.hasKey("audioId")) {
            addSoundToPlaylist((long)options.getDouble("playlistId"), options.getString("audioId"), successCallback, errorCallback);
        }
    }

    private void addSoundToPlaylist(long playlistId, String audioId, final Callback successCallback, final Callback errorCallback) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
            "external", playlistId);
        ContentResolver resolver = getCurrentActivity().getContentResolver();
        ContentValues values = new ContentValues();
            // values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, pos);
            values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
            values.put(MediaStore.Audio.Playlists.Members.PLAYLIST_ID,
                    playlistId);
        try {
            resolver.insert(uri, values);
            successCallback.invoke(true);
        } catch (RuntimeException e) {
            errorCallback.invoke(e.toString());
        } catch (Exception e) {
            errorCallback.invoke(e.getMessage());
        } finally {
        }
    }

    @ReactMethod
    public void getArtists(ReadableMap options, final Callback successCallback, final Callback errorCallback) {

        WritableArray jsonArray = new WritableNativeArray();

        String[] projection = new String[] { MediaStore.Audio.Artists.ARTIST_KEY, MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS, MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                MediaStore.Audio.Artists._ID };
        Cursor cursor = getCurrentActivity().getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                WritableMap item = new WritableNativeMap();
                item.putString("key", String.valueOf(cursor.getString(0)));
                item.putString("artist", String.valueOf(cursor.getString(1)));
                item.putString("numberOfAlbums", String.valueOf(cursor.getString(2)));
                item.putString("numberOfSongs", String.valueOf(cursor.getString(3)));
                item.putString("id", String.valueOf(cursor.getString(4)));
                jsonArray.pushMap(item);
            } while (cursor.moveToNext());
        } else {
            String msg = "cursor is either null or empty ";
            Log.e("Musica", msg);
        }
        Log.e("MusicaAlbums", String.valueOf(jsonArray));
        cursor.close();
        successCallback.invoke(jsonArray);
    }

    @ReactMethod
    public void getAlbums(ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        ContentResolver resolver = getCurrentActivity().getContentResolver();
        WritableArray jsonArray = new WritableNativeArray();

        String[] projection = new String[] { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.ALBUM_ART,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS };
        if (options.hasKey("artist")) {
            String searchParam = "%" + options.getString("artist") + "%";
            Cursor cursor = resolver.query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection,
                    MediaStore.Audio.Albums.ARTIST + " Like ?", new String[] { searchParam }, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    WritableMap item = getAlbumsData(cursor);
                    jsonArray.pushMap(item);
                } while (cursor.moveToNext());
            } else {
                String msg = "cursor is either null or empty ";
                Log.e("Musica", msg);
            }
            Log.e("MusicaAlbums", String.valueOf(jsonArray));
            cursor.close();
            successCallback.invoke(jsonArray);
        } else {
            Cursor cursor = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    WritableMap item = getAlbumsData(cursor);
                    jsonArray.pushMap(item);
                } while (cursor.moveToNext());
            } else {
                String msg = "cursor is either null or empty ";
                Log.e("Musica", msg);
            }
            Log.e("MusicaAlbums", String.valueOf(jsonArray));
            cursor.close();
            successCallback.invoke(jsonArray);
        }
    }

    private WritableMap getAlbumsData(Cursor cursor) {
        WritableMap item = new WritableNativeMap();
        item.putString("id", String.valueOf(cursor.getLong(0)));
        item.putString("album", String.valueOf(cursor.getString(1)));
        String author = cursor.getString(2);
        if (author != null) {
            item.putString("author", author);
        }
        String cover = cursor.getString(3);
        if (cover != null) {
            item.putString("cover", cover);
        }
        item.putString("numberOfSongs", String.valueOf(cursor.getString(4)));
        return item;
    }

    @ReactMethod
    public void getPlaylists(ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        ContentResolver resolver = getCurrentActivity().getContentResolver();
        WritableArray jsonArray = new WritableNativeArray();

        String[] projection = new String[] { MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists._COUNT,
                MediaStore.Audio.Playlists.NAME, MediaStore.Audio.Playlists.DATE_ADDED, MediaStore.Audio.Playlists.DATE_MODIFIED };
        if (options.hasKey("artist")) {
            String searchParam = "%" + options.getString("artist") + "%";
            Cursor cursor = resolver.query(
                    MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, projection,
                    MediaStore.Audio.Playlists.NAME + " Like ?", new String[] { searchParam }, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    WritableMap item = getPlaylistsData(cursor);
                    jsonArray.pushMap(item);
                } while (cursor.moveToNext());
            } else {
                String msg = "cursor is either null or empty ";
                Log.e("Musica", msg);
            }
            Log.e("MusicaPlaylists", String.valueOf(jsonArray));
            cursor.close();
            successCallback.invoke(jsonArray);
        } else {
            Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, projection, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    WritableMap item = getPlaylistsData(cursor);
                    jsonArray.pushMap(item);
                } while (cursor.moveToNext());
            } else {
                String msg = "cursor is either null or empty ";
                Log.e("Musica", msg);
            }
            Log.e("MusicaPlaylists", String.valueOf(jsonArray));
            cursor.close();
            successCallback.invoke(jsonArray);
        }
    }

    private WritableMap getPlaylistsData(Cursor cursor) {
        WritableMap item = new WritableNativeMap();
        item.putString("id", String.valueOf(cursor.getLong(0)));
        item.putString("numberOfSongs", cursor.getString(1));
        item.putString("name", cursor.getString(2));
        item.putString("dateAdded", cursor.getString(3));
        item.putString("dateModified", cursor.getString(4));
        return item;
    }

    @ReactMethod
    public void getListSongs(ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        ContentResolver musicResolver = getCurrentActivity().getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        WritableArray jsonArray = new WritableNativeArray();
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            if (options.hasKey("albumId") || options.hasKey("artistId")) {
                String selection = "is_music != 0";
                if (options.hasKey("albumId")) {
                    selection = selection + " and album_id = " + options.getString("albumId");
                }
                if (options.hasKey("artistId")) {
                    selection = selection + " and artist_id = " + options.getString("artistId");
                }

                Cursor cursor = musicResolver.query(musicUri, null, selection, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        WritableNativeMap item = getSongsData(cursor, mmr);
                        jsonArray.pushMap(item);
                    } while (cursor.moveToNext());
                } else {
                    String msg = "cursor is either null or empty ";
                    Log.e("Musica", msg);
                }
                cursor.close();
            } else {
                if (options.hasKey("genreId")) {
                    Long genreID = Long.parseLong(options.getString("genreId"));
                    Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreID); 

                    Cursor cursor = musicResolver.query(uri, null, null, null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            WritableNativeMap item = getSongsData(cursor, mmr);
                            jsonArray.pushMap(item);
                        } while (cursor.moveToNext());
                    } else {
                        String msg = "cursor is either null or empty ";
                        Log.e("Musica", msg);
                    }
                    cursor.close();
                }
            }
            Log.e("MusicaAlbums", String.valueOf(jsonArray));
            successCallback.invoke(jsonArray);
        } catch (RuntimeException e) {
            errorCallback.invoke(e.toString());
        } catch (Exception e) {
            errorCallback.invoke(e.getMessage());
        } finally {
            mmr.release();
        }
        
        
    }

    @ReactMethod
    public void getGenres(ReadableMap options, final Callback successCallback, final Callback errorCallback) {

        WritableArray jsonArray = new WritableNativeArray();
        Cursor genrecursor;
        Cursor tempcursor;
        long genreId;
        String GenreName;
        if (options.hasKey("genre")) {
            int index;
            Uri uri;
            String[] genreProjection = { MediaStore.Audio.Genres.NAME, MediaStore.Audio.Genres._ID };
            String[] projection = new String[] { MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media._ID };
            String Selection = MediaStore.Audio.Genres.NAME + " Like ?";
            
            try {
                genrecursor = getCurrentActivity().getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                        genreProjection, Selection, new String[] { "%"+options.getString("genre")+"%" }, null);
                if (genrecursor != null && genrecursor.getCount() > 0) {
                    genrecursor.moveToFirst();

                    do {
                        GenreName = genrecursor.getString(0);
                        Log.e("Tag-Genre name", GenreName);
                        index = genrecursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID);
                        genreId = Long.parseLong(genrecursor.getString(index));
                        uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId);

                        tempcursor = getCurrentActivity().getContentResolver().query(uri, projection, null, null, null);
                        if (tempcursor.moveToFirst()) {

                            do {
                                WritableMap item = new WritableNativeMap();
                                if (GenreName != null) {
                                    item.putString("genre", GenreName);
                                    item.putString("title", String.valueOf(tempcursor.getString(0)));
                                    item.putString("artist", String.valueOf(tempcursor.getString(1)));
                                    item.putString("album", String.valueOf(tempcursor.getString(2)));
                                    item.putDouble("duration", tempcursor.getInt(3));
                                    item.putString("path", String.valueOf(tempcursor.getString(4)));
                                    item.putString("id", String.valueOf(tempcursor.getString(5)));
                                    jsonArray.pushMap(item);
                                }
                            } while (tempcursor.moveToNext());
                        }
                        tempcursor.close();
                    } while (genrecursor.moveToNext());
                } else {
                    String msg = "cursor is either null or empty ";
                    Log.e("Musica", msg);
                }
                Log.e("MusicaGenres", String.valueOf(jsonArray));
                genrecursor.close();
                successCallback.invoke(jsonArray);
            } catch (RuntimeException e) {
                errorCallback.invoke(e.toString());
            } catch (Exception e) {
                errorCallback.invoke(e.getMessage());
            } finally {
            }
        } else {
            try {
                String[] projection = new String[] { MediaStore.Audio.Genres.NAME, MediaStore.Audio.Genres._ID };
                genrecursor = getCurrentActivity().getContentResolver()
                        .query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, projection, null, null, null);
                if (genrecursor != null && genrecursor.getCount() > 0) {
                    genrecursor.moveToFirst();
                    do {
                        WritableMap item = new WritableNativeMap();
                        GenreName = genrecursor.getString(0);
                        genreId = genrecursor.getLong(1);
                        if (GenreName != null) {
                            item.putString("name", GenreName);
                            item.putString("id", genrecursor.getString(1));
                            tempcursor = getCurrentActivity().getContentResolver().query(MediaStore.Audio.Genres.Members.getContentUri("external", genreId), null, null, null, null);
                            if (tempcursor != null) {
                                item.putDouble("numberOfSongs", tempcursor.getCount());
                            }
                            tempcursor.close();
                            jsonArray.pushMap(item);
                        }
                    } while (genrecursor.moveToNext());
                } else {
                    String msg = "cursor is either null or empty ";
                    Log.e("Musica", msg);
                }
                Log.e("MusicaGenre", String.valueOf(jsonArray));
                genrecursor.close();
                successCallback.invoke(jsonArray);
            } catch (RuntimeException e) {
                errorCallback.invoke(e.toString());
            } catch (Exception e) {
                errorCallback.invoke(e.getMessage());
            } finally {
            }
        }

    }

    @ReactMethod
    public void updateSong(ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        if (options.hasKey("id")) {
            String id = options.getString("id");
            String genre = options.getString("genre");
            String artist = options.getString("artist");
            String album = options.getString("album");
            String name = options.getString("name");
            try {
                ContentResolver resolver = getCurrentActivity().getContentResolver();
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(id));
                ContentValues values = new ContentValues();

                if (genre != null) {
                    values.put(MediaStore.Audio.Genres.NAME, genre);
                }
                if (album != null) {
                    values.put(MediaStore.Audio.Media.ALBUM, album);
                }
                if (artist != null) {
                    values.put(MediaStore.Audio.Media.ARTIST, artist);
                }
                if (name != null) {
                    values.put(MediaStore.Audio.Media.TITLE, name);
                }
                resolver.update(uri, values, null, null);

                WritableArray jsonArray = new WritableNativeArray();
                WritableMap result = new WritableNativeMap();
                result.putBoolean("isSuccess", true);
                jsonArray.pushMap(result);
                successCallback.invoke(jsonArray);
            } catch (RuntimeException e) {
                errorCallback.invoke(e.toString());
            } catch (Exception e) {
                errorCallback.invoke(e.getMessage());
            } finally {
            }
        } else {
            errorCallback.invoke("Invalid parameters");
        }
    }

    @ReactMethod
    public void createGenre(ReadableMap options, final Callback successCallback, final Callback errorCallback) {
        if (options.hasKey("name")) {
            String name = options.getString("name");
            
            try {
                ContentResolver resolver = getCurrentActivity().getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Genres.NAME, name);

                Uri newUri = resolver.insert(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, values);
                WritableArray jsonArray = new WritableNativeArray();
                WritableMap result = new WritableNativeMap();
                result.putBoolean("isSuccess", true);
                jsonArray.pushMap(result);
                successCallback.invoke(jsonArray);
            } catch (RuntimeException e) {
                errorCallback.invoke(e.toString());
            } catch (Exception e) {
                errorCallback.invoke(e.getMessage());
            } finally {
            }
        } else {
            WritableArray jsonArray = new WritableNativeArray();
            WritableMap result = new WritableNativeMap();
            result.putBoolean("isSuccess", false);
            jsonArray.pushMap(result);
            successCallback.invoke(jsonArray);
        }
    }

    // @ReactMethod
    // public void updateGenre(ReadableMap options, final Callback successCallback, final Callback errorCallback) {
    //     if (options.hasKey("name") && options.hasKey("id")) {
    //         String genreID = options.getString("id");
    //         String name = options.getString("name");
    //         Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", Long.parseLong(genreID));
            
    //         try {
    //             ContentResolver resolver = getCurrentActivity().getContentResolver();
    //             ContentValues values = new ContentValues();
    //             values.put(MediaStore.Audio.Media.IS_PENDING, 1);
    //             resolver.update(uri, values, null, null);

    //             values.clear();
    //             values.put(MediaStore.Audio.Media.IS_PENDING, 0);
    //             values.put(MediaStore.Audio.Genres.NAME, name);
    //             String whereGenre = MediaStore.Audio.Genres.Members.AUDIO_ID + "=?";
    //             String[] whereVal = { genreID };

    //             Integer rows = resolver.update(uri, values, whereGenre, whereVal);
    //             // resolver.insert(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, values);
    //             WritableArray jsonArray = new WritableNativeArray();
    //             WritableMap result = new WritableNativeMap();
    //             result.putBoolean("isSuccess", true);
    //             jsonArray.pushMap(result);
    //             successCallback.invoke(jsonArray);
    //         } catch (RuntimeException e) {
    //             errorCallback.invoke(e.toString());
    //         } catch (Exception e) {
    //             errorCallback.invoke(e.getMessage());
    //         } finally {
    //         }
    //     } else {
    //         WritableArray jsonArray = new WritableNativeArray();
    //         WritableMap result = new WritableNativeMap();
    //         result.putBoolean("isSuccess", false);
    //         jsonArray.pushMap(result);
    //         successCallback.invoke(jsonArray);
    //     }
    // }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
}