/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kosbuild.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sad
 */
public class UtilsTest {

    @Test
    public void testConcatPaths() {
        assertEquals("D:/a/b/c/d/e", Utils.concatPaths("D:\\", "/a\\", "b/", "/c", "d", "e"));
    }

}
