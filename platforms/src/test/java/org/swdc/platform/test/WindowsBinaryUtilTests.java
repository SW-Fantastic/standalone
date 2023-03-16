package org.swdc.platform.test;

import org.swdc.platforms.WindowsBinaryUtils;

import java.io.File;
import java.util.List;

public class WindowsBinaryUtilTests {

    public static void main(String[] args) {
        System.load(new File("platforms/Platform.dll").getAbsolutePath());
        /*File target = new File("platforms/screen-capture-recorder-x64.dll");
        boolean isSuccess = WindowsBinaryUtils.dllRegister(target);
        System.err.println("com is register " + (isSuccess ? "success" : "failed"));
        isSuccess = WindowsBinaryUtils.dllUnRegister(target);
        System.err.println("com is un-register " + (isSuccess ? "success" : "failed"));*/

        List<String> nativeNS = WindowsBinaryUtils.getSystemDNSServerAddresses();
        for (String addr: nativeNS) {
            System.err.println(addr);
        }
    }

}
