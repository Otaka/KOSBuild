package org.visualeagle.gui.remotewindow;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.visualeagle.gui.remotewindow.fileprovider.RFile;
import org.visualeagle.utils.atable.annotations.ATableField;

/**
 * @author sad
 */
public class RFileWrapper {

    public static final int NAME = 0;
    public static final int LAST_MODIFIED = 1;
    public static final int SIZE = 2;
    private static SimpleDateFormat dataFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private RFile file;

    public RFileWrapper(RFile file) {
        this.file = file;
    }

    public RFile getFile() {
        return file;
    }

    @ATableField(id = NAME, column = "File name")
    public String getName() {
        return file.getName();
    }

    public String getNameStringValue() {
        return (file.isDirectory()) ? "[" + file.getName() + "]" : file.getName();
    }

    @ATableField(id = LAST_MODIFIED, column = "Last modified")
    public long getLastModified() {
        return file.getLastModified();
    }

    public String getLastModifiedStringValue() {
        return dataFormatter.format(new Date(file.getLastModified()));
    }

    @ATableField(id = SIZE, column = "Size")
    public long getSize() {
        return file.getSize();
    }

    public String getSizeStringValue() {
        return file.isDirectory() ? "Folder" : Long.toString(getSize());
    }
}
