package com.kosbuild;

import com.kosbuild.jsonparser.JsonObject;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 * @author Dmitry
 */
public class KOSBuildTest {

    @Test
    @Ignore
    public void testMergeWithParent() throws IOException {
        KOSBuild.buildFileExtension = "txt";
        JsonObject element = new KOSBuild().readAndParseFile(new File("./src/test/java/com/kosbuild/resources/buildSystem.json.txt"));
        String resultFile = FileUtils.readFileToString(new File("./src/test/java/com/kosbuild/resources/buildSystemResult.json.txt"), "UTF-8");
        resultFile = resultFile.replace("\r", "").replace("\n", "").replace(" ", "");
        assertEquals(resultFile, element.getAsString().replace("\r", "").replace("\n", "").replace(" ", ""));
    }
}