package net.minestom.server.registry;

import java.util.Map;

/**
 * Class used to represent the contents of a versions/{version}/{version}.json file
 * Structured in a way that makes loading with Gson easy.
 * Only concerned with helping extracting data from the server jar, lots of features may be missing.
 */
class VersionInfo {

    private Map<String, DownloadObject> downloads;

    private VersionInfo() {

    }

    public Map<String, DownloadObject> getDownloadableFiles() {
        return downloads;
    }

    static class DownloadObject {
        private String url;
        private String sha1;
        private long size;

        public String getUrl() {
            return url;
        }

        public long getSize() {
            return size;
        }

        public String getSha1() {
            return sha1;
        }
    }
}
