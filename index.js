/**
 * @typedef {Object} Song
 * @property {string} id
 * @property {string} songUri
 * @property {string} title
 * @property {string} artist
 * @property {string} album
 * @property {string} duration
 * @property {string} path
 */

/**
 * @typedef {Object} Album
 * @property {string} id
 * @property {string} album
 * @property {string} author
 * @property {string} cover
 * @property {string} numberOfSongs
 */

/**
 * @typedef {Object} Artist
 * @property {string} key
 * @property {string} artist
 * @property {string} numberOfAlbums
 * @property {string} numberOfSongs
 * @property {string} id
 */

/**
 * @typedef {Object} Genre
 * @property {string} id
 * @property {string} name
 * @property {string} numberOfSongs
 */

import { NativeModules, Platform } from "react-native";

const { RNAndroidStore, RNReactNativeGetMusicFiles } = NativeModules;
   /**
    * @class RNAndroidAudioStore
    */
export const RNAndroidAudioStore = {
    /**
     * @member
     * @function
     * @async
     * @param {Object} options
     * @param {boolean} [options.blured]
     * @param {boolean} [options.artist]
     * @param {boolean} [options.duration]
     * @param {boolean} [options.title]
     * @param {boolean} [options.id]
     * @param {string} [options.coverFolder]
     * @param {boolean} [options.cover]
     * @param {number} [options.coverResizeRatio]
     * @param {boolean} [options.icon]
     * @param {number} [options.iconSize]
     * @param {number} [options.coverSize]
     * @param {boolean} [options.genre]
     * @param {boolean} [options.album]
     * @param {number} [options.batchNumber]
     * @param {number} [options.minimumSongDuration]
     * @param {number} [options.delay]
     * @param {string} [options.displayName]
     * @param {boolean} [options.isDownload]
     */
    getAll(options) {
        return new Promise((resolve, reject) => {
            if (Platform.OS === "android") {
                RNAndroidStore.getAll(
                    options,
                    tracks => {
                        resolve(tracks);
                    },
                    error => {
                        resolve(error);
                    }
                );
            }
        });
    },

    /**
     * @function
     * @async
     * @param {Object} options
     * @param {string} options.songUri
     * @param {string} [options.coverFolder]
     * @param {boolean} [options.cover]
     * @param {number} [options.coverResizeRatio]
     * @param {boolean} [options.icon]
     * @param {number} [options.iconSize]
     * @param {number} [options.coverSize]
     * @param {boolean} [options.blured]
     * @returns {Promise<Array<Song>>}
     */
    getSongByPath(options) {
        return new Promise((resolve, reject) => {
            if (Platform.OS === "android") {
                RNAndroidStore.getSongByPath(
                    options,
                    tracks => {
                        resolve(tracks);
                    },
                    error => {
                        resolve(error);
                    }
                );
            }
        });
    },
    /**
     * @function
     * @async
     * @param {Object} options
     * @param {string} [options.artist]
     * @param {string} [options.album]
     * @returns {Promise<Array<Song>>}
     */
    getSongs(options = {}) {
        return new Promise((resolve, reject) => {
            if (Platform.OS === "android") {
                RNAndroidStore.getSong(
                    options,
                    albums => {
                        resolve(albums);
                    },
                    error => {
                        resolve(error);
                    }
                );
            }
        });
    },

    /**
     * @async
     * @function
     * @param {Object} options
     * @param {string} options.searchParam
     * @returns {Promise<Array<Song>>}
     */
    search(options = {}) {
        return new Promise((resolve, reject) => {
            if (Platform.OS === "android") {
                RNAndroidStore.search(
                    options,
                    results => {
                        resolve(results);
                    },
                    error => {
                        resolve(error);
                    }
                );
            }
        });
    },
};

const MusicFiles = {
    /** For both Android and IOS */
    getAll(options) {
        return new Promise((resolve, reject) => {
            if (Platform.OS === "android") {
                RNReactNativeGetMusicFiles.getAll(options, (tracks) => {
                    resolve(tracks);
                }, (error) => {
                    resolve(error);
                });
            } else {
                RNReactNativeGetMusicFiles.getAll(options, (tracks) => {
                    if (tracks.length > 0) {
                        resolve(tracks);
                    } else {
                        resolve("Error, you don't have any tracks");
                    }
                });
            }
        });
    },
    /** For Android only */
    addNewPlaylist(options) {
        return new Promise((resolve, reject) => {
            if (Platform.OS === "android") {
                RNReactNativeGetMusicFiles.addNewPlaylist(options, (tracks) => {
                    resolve(tracks);
                }, (error) => {
                    resolve(error);
                });
            }
        });
    },
    /** For Android only */
    addSoundToPlaylist(options) {
        return new Promise((resolve, reject) => {
            if (Platform.OS === "android") {
                RNReactNativeGetMusicFiles.addSoundToPlaylist(options, (tracks) => {
                    resolve(tracks);
                }, (error) => {
                    resolve(error);
                });
            }
        });
    },
    /**
     * @function
     * @async
     * @param {Object} options
     * @returns {Promise<Array<Artist>>}
     */

    getArtists(options = {}) {
        return new Promise((resolve, reject) => {
            if (Platform.OS === "android") {
                RNReactNativeGetMusicFiles.getArtists(
                    options,
                    albums => {
                        resolve(albums);
                    },
                    error => {
                        resolve(error);
                    }
                );
            }
        });
    },
    /**
     * @function
     * @async
     * @param {Object} options
     * @param {string} [options.artist]
     * @returns {Promise<Array<Album>>}
     */

    getAlbums(options = {}) {
        return new Promise((resolve, reject) => {
            if (Platform.OS === "android") {
                RNReactNativeGetMusicFiles.getAlbums(
                    options,
                    albums => {
                        resolve(albums);
                    },
                    error => {
                        resolve(error);
                    }
                );
            }
        });
    },
    /**
     * @function
     * @async
     * @param {Object} options
     * @param {string} [options.artist]
     * @returns {Promise<Array<Album>>}
     */

    getPlaylists(options = {}) {
        return new Promise((resolve, reject) => {
            if (Platform.OS === "android") {
                RNReactNativeGetMusicFiles.getPlaylists(
                    options,
                    albums => {
                        resolve(albums);
                    },
                    error => {
                        resolve(error);
                    }
                );
            }
        });
    },
    /** For Android only */
    /**
     * @function
     * @async
     * @param {Object} options
     * @param {string} [options.artistId]
     * @param {string} [options.albumId]
     * @returns {Promise<Array<Album>>}
     */
    getListSongs(options) {
        return new Promise((resolve, reject) => {
            if (Platform.OS === "android") {
                RNReactNativeGetMusicFiles.getListSongs(options, (tracks) => {
                    resolve(tracks);
                }, (error) => {
                    resolve(error);
                });
            }
        });
    },
    /**
     * @function
     * @async
     * @param {Object} options
     * @param {string} [options.genre]
     * @returns {Promise<Array<Genre>>}
     */
    getGenres(options = {}) {
        return new Promise((resolve, reject) => {
            if (Platform.OS === "android") {
                RNReactNativeGetMusicFiles.getGenres(
                    options,
                    genres => {
                        resolve(genres);
                    },
                    error => {
                        resolve(error);
                    }
                );
            }
        });
    },
    /**
     * @function
     * @async
     * @param {Object} options
     * @param {string} [options.id]
     * @param {string} [options.artist]
     * @param {string} [options.album]
     * @returns {Promise<Array<Genre>>}
     */
    updateSong(options = {}) {
        return new Promise((resolve, reject) => {
            if (Platform.OS === "android") {
                RNReactNativeGetMusicFiles.updateSong(
                    options,
                    result => {
                        resolve(result);
                    },
                    error => {
                        resolve(error);
                    }
                );
            }
        });
    }
 }
 
 export default MusicFiles;