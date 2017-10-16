package org.visualeagle.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author sad
 */
public class ChunkedTextCollector {

    private final OnTextChanged event;

    private final Map<String, String> titleChunks = new LinkedHashMap<>();
    private String separator = " ";

    public ChunkedTextCollector(OnTextChanged event) {
        this.event = event;
    }

    public ChunkedTextCollector setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public void setTitle(String chunkName, String chunkValue) {
        titleChunks.put(chunkName.toLowerCase(), chunkValue);
        event.textChanged(formatTitle());
    }

    public void setTitles(String... chunkNameValuePairs) {
        if (chunkNameValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("chunkNameValuePairs should be an array of pairs chunk name and chunk value");
        }

        for (int i = 0; i < chunkNameValuePairs.length; i += 2) {
            String name = chunkNameValuePairs[i];
            String value = chunkNameValuePairs[i + 1];
            titleChunks.put(name.toLowerCase(), value);

        }

        event.textChanged(formatTitle());
    }

    /**
     * argument removeChunks determines, if we should actually remove chunks, or
     * just set their values to empty string
     */
    public void clearChunks(boolean removeChunks) {
        if (removeChunks) {
            titleChunks.clear();
        } else {
            for (String key : titleChunks.keySet()) {
                titleChunks.put(key, "");
            }
        }
    }

    public void setTitle(String chunkValue) {
        setTitle("", chunkValue);
    }

    private String formatTitle() {
        if (titleChunks.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String key : titleChunks.keySet()) {
            if (first == false) {
                if (separator != null) {
                    sb.append(separator);
                }
            }
            sb.append(titleChunks.get(key));
            first = false;
        }

        return sb.toString();
    }

    public interface OnTextChanged {

        public void textChanged(String text);
    };
}
