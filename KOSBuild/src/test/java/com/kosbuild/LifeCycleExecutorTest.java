/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kosbuild;

import com.kosbuild.plugins.AbstractPlugin;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Dmitry
 */
public class LifeCycleExecutorTest {

    @Test
    public void testExecute() {
        LifeCycleExecutor lce = new LifeCycleExecutor();
        String[] array = lce.obtainFullStepsList(new String[]{AbstractPlugin.INSTALL});
        assertArrayEquals(new String[]{
            AbstractPlugin.VALIDATE, AbstractPlugin.COMPILE_SUBMODULE, AbstractPlugin.COMPILE,
            AbstractPlugin.TEST, AbstractPlugin.PACKAGE, AbstractPlugin.VERIFY, AbstractPlugin.INSTALL
        }, array);

        String[] array2 = lce.obtainFullStepsList(new String[]{AbstractPlugin.CLEAN, AbstractPlugin.INSTALL});
        assertArrayEquals(new String[]{
            AbstractPlugin.CLEAN, AbstractPlugin.VALIDATE, AbstractPlugin.COMPILE_SUBMODULE, AbstractPlugin.COMPILE,
            AbstractPlugin.TEST, AbstractPlugin.PACKAGE, AbstractPlugin.VERIFY, AbstractPlugin.INSTALL
        }, array2);
    }
}
