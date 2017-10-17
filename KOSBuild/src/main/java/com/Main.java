package com;

import com.kosbuild.KOSBuild;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Dmitry
 */
public class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        new KOSBuild().run(args);
    }
}
