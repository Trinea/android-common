package cn.trinea.android.common.util;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Log;
import cn.trinea.android.common.util.ShellUtils.CommandResult;

/**
 * PackageUtils
 * <ul>
 * <strong>Install package</strong>
 * <li>{@link PackageUtils#installNormal(Context, String)}</li>
 * <li>{@link PackageUtils#installSilent(Context, String)}</li>
 * <li>{@link PackageUtils#install(Context, String)}</li>
 * </ul>
 * <ul>
 * <strong>Is system application</strong>
 * <li>{@link PackageUtils#isSystemApplication(Context)}</li>
 * <li>{@link PackageUtils#isSystemApplication(Context, String)}</li>
 * <li>{@link PackageUtils#isSystemApplication(PackageManager, String)}</li>
 * </ul>
 * 
 * @author Trinea 2013-5-15
 */
public class PackageUtils {

    public static final String TAG = "PackageUtils";

    /**
     * install according conditions
     * <ul>
     * <li>if system application or rooted, see {@link #installSilent(Context, String)}</li>
     * <li>else see {@link #installNormal(Context, String)}</li>
     * </ul>
     * 
     * @param context
     * @param filePath
     * @return
     */
    public static final int install(Context context, String filePath) {
        if (!PackageUtils.isSystemApplication(context)) {
            boolean isRoot = ShellUtils.checkRootPermission();
            if (!isRoot) {
                return installNormal(context, filePath) ? INSTALL_SUCCEEDED : INSTALL_FAILED_INVALID_URI;
            }
        }

        return installSilent(context, filePath);
    }

    /**
     * install package normal by system intent
     * 
     * @param context
     * @param filePath file path of package
     * @return whether apk exist
     */
    public static boolean installNormal(Context context, String filePath) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        File file = new File(filePath);
        if (file != null && file.length() > 0 && file.exists() && file.isFile()) {
            i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            return true;
        }
        return false;
    }

    /**
     * install package silent by root
     * <ul>
     * <strong>Attentions：</strong>
     * <li>Don't call this on the ui thread, it costs some times.</li>
     * <li>You should add <strong>android.permission.INSTALL_PACKAGES</strong> in manifest, so no need to request root
     * permission, if you are system app.</li>
     * </ul>
     * 
     * @param context file path of package
     * @param filePath file path of package
     * @return {@link PackageUtils#INSTALL_SUCCEEDED} means install success, other means failed. details see
     * {@link PackageUtils#INSTALL_FAILED_*}. same to {@link PackageManager#INSTALL_*}
     */
    public static int installSilent(Context context, String filePath) {
        if (filePath == null || filePath.length() == 0) {
            return INSTALL_FAILED_INVALID_URI;
        }

        File file = new File(filePath);
        if (file == null || file.length() <= 0 || !file.exists() || !file.isFile()) {
            return INSTALL_FAILED_INVALID_URI;
        }

        /**
         * if context is system app, don,t need root permission, but should add <uses-permission
         * android:name="android.permission.INSTALL_PACKAGES" /> in mainfest
         **/
        StringBuilder command = new StringBuilder().append("pm install -r ").append(filePath.replace(" ", "\\ "));
        CommandResult commandResult = ShellUtils.execCommand(command.toString(), !isSystemApplication(context), true);
        if (commandResult.successMsg != null
            && (commandResult.successMsg.contains("Success") || commandResult.successMsg.contains("success"))) {
            return INSTALL_SUCCEEDED;
        }

        Log.e(TAG, new StringBuilder().append("successMsg:").append(commandResult.successMsg).append(", ErrorMsg:")
                                      .append(commandResult.errorMsg).toString());
        if (commandResult.errorMsg != null) {
            if (commandResult.errorMsg.contains("INSTALL_FAILED_ALREADY_EXISTS")) {
                return INSTALL_FAILED_ALREADY_EXISTS;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_INVALID_APK")) {
                return INSTALL_FAILED_INVALID_APK;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_INVALID_URI")) {
                return INSTALL_FAILED_INVALID_URI;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_INSUFFICIENT_STORAGE")) {
                return INSTALL_FAILED_INSUFFICIENT_STORAGE;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_DUPLICATE_PACKAGE")) {
                return INSTALL_FAILED_DUPLICATE_PACKAGE;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_NO_SHARED_USER")) {
                return INSTALL_FAILED_NO_SHARED_USER;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_UPDATE_INCOMPATIBLE")) {
                return INSTALL_FAILED_UPDATE_INCOMPATIBLE;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_SHARED_USER_INCOMPATIBLE")) {
                return INSTALL_FAILED_SHARED_USER_INCOMPATIBLE;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_MISSING_SHARED_LIBRARY")) {
                return INSTALL_FAILED_MISSING_SHARED_LIBRARY;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_REPLACE_COULDNT_DELETE")) {
                return INSTALL_FAILED_REPLACE_COULDNT_DELETE;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_DEXOPT")) {
                return INSTALL_FAILED_DEXOPT;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_OLDER_SDK")) {
                return INSTALL_FAILED_OLDER_SDK;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_CONFLICTING_PROVIDER")) {
                return INSTALL_FAILED_CONFLICTING_PROVIDER;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_NEWER_SDK")) {
                return INSTALL_FAILED_NEWER_SDK;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_TEST_ONLY")) {
                return INSTALL_FAILED_TEST_ONLY;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_CPU_ABI_INCOMPATIBLE")) {
                return INSTALL_FAILED_CPU_ABI_INCOMPATIBLE;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_MISSING_FEATURE")) {
                return INSTALL_FAILED_MISSING_FEATURE;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_CONTAINER_ERROR")) {
                return INSTALL_FAILED_CONTAINER_ERROR;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_INVALID_INSTALL_LOCATION")) {
                return INSTALL_FAILED_INVALID_INSTALL_LOCATION;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_MEDIA_UNAVAILABLE")) {
                return INSTALL_FAILED_MEDIA_UNAVAILABLE;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_VERIFICATION_TIMEOUT")) {
                return INSTALL_FAILED_VERIFICATION_TIMEOUT;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_VERIFICATION_FAILURE")) {
                return INSTALL_FAILED_VERIFICATION_FAILURE;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_PACKAGE_CHANGED")) {
                return INSTALL_FAILED_PACKAGE_CHANGED;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_UID_CHANGED")) {
                return INSTALL_FAILED_UID_CHANGED;
            } else if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_NOT_APK")) {
                return INSTALL_PARSE_FAILED_NOT_APK;
            } else if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_BAD_MANIFEST")) {
                return INSTALL_PARSE_FAILED_BAD_MANIFEST;
            } else if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION")) {
                return INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION;
            } else if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_NO_CERTIFICATES")) {
                return INSTALL_PARSE_FAILED_NO_CERTIFICATES;
            } else if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES")) {
                return INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES;
            } else if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING")) {
                return INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING;
            } else if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME")) {
                return INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
            } else if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID")) {
                return INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID;
            } else if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_MANIFEST_MALFORMED")) {
                return INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            } else if (commandResult.errorMsg.contains("INSTALL_PARSE_FAILED_MANIFEST_EMPTY")) {
                return INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
            } else if (commandResult.errorMsg.contains("INSTALL_FAILED_INTERNAL_ERROR")) {
                return INSTALL_FAILED_INTERNAL_ERROR;
            }
        }
        return INSTALL_FAILED_OTHER;
    }

    /**
     * whether context is system application
     * 
     * @param context
     * @return
     */
    public static boolean isSystemApplication(Context context) {
        if (context == null) {
            return false;
        }

        return isSystemApplication(context, context.getPackageName());
    }

    /**
     * whether packageName is system application
     * 
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isSystemApplication(Context context, String packageName) {
        if (context == null) {
            return false;
        }

        return isSystemApplication(context.getPackageManager(), packageName);
    }

    /**
     * whether packageName is system application
     * 
     * @param packageManager
     * @param packageName
     * @return <ul>
     * <li>if packageManager is null, return false</li>
     * <li>if package name is null or is empty, return false</li>
     * <li>if package name not exit, return false</li>
     * <li>if package name exit, but not system app, return false</li>
     * <li>else return true</li>
     * </ul>
     */
    public static boolean isSystemApplication(PackageManager packageManager, String packageName) {
        if (packageManager == null || packageName == null || packageName.length() == 0) {
            return false;
        }

        try {
            ApplicationInfo app = packageManager.getApplicationInfo(packageName, 0);
            return (app != null && (app.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Installation return code<br/>
     * install success.
     */
    public static final int INSTALL_SUCCEEDED                              = 1;
    /**
     * Installation return code<br/>
     * the package is already installed.
     */
    public static final int INSTALL_FAILED_ALREADY_EXISTS                  = -1;

    /**
     * Installation return code<br/>
     * the package archive file is invalid.
     */
    public static final int INSTALL_FAILED_INVALID_APK                     = -2;

    /**
     * Installation return code<br/>
     * the URI passed in is invalid.
     */
    public static final int INSTALL_FAILED_INVALID_URI                     = -3;

    /**
     * Installation return code<br/>
     * the package manager service found that the device didn't have enough storage space to install the app.
     */
    public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE            = -4;

    /**
     * Installation return code<br/>
     * a package is already installed with the same name.
     */
    public static final int INSTALL_FAILED_DUPLICATE_PACKAGE               = -5;

    /**
     * Installation return code<br/>
     * the requested shared user does not exist.
     */
    public static final int INSTALL_FAILED_NO_SHARED_USER                  = -6;

    /**
     * Installation return code<br/>
     * a previously installed package of the same name has a different signature than the new package (and the old
     * package's data was not removed).
     */
    public static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE             = -7;

    /**
     * Installation return code<br/>
     * the new package is requested a shared user which is already installed on the device and does not have matching
     * signature.
     */
    public static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE        = -8;

    /**
     * Installation return code<br/>
     * the new package uses a shared library that is not available.
     */
    public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY          = -9;

    /**
     * Installation return code<br/>
     * the new package uses a shared library that is not available.
     */
    public static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE          = -10;

    /**
     * Installation return code<br/>
     * the new package failed while optimizing and validating its dex files, either because there was not enough storage
     * or the validation failed.
     */
    public static final int INSTALL_FAILED_DEXOPT                          = -11;

    /**
     * Installation return code<br/>
     * the new package failed because the current SDK version is older than that required by the package.
     */
    public static final int INSTALL_FAILED_OLDER_SDK                       = -12;

    /**
     * Installation return code<br/>
     * the new package failed because it contains a content provider with the same authority as a provider already
     * installed in the system.
     */
    public static final int INSTALL_FAILED_CONFLICTING_PROVIDER            = -13;

    /**
     * Installation return code<br/>
     * the new package failed because the current SDK version is newer than that required by the package.
     */
    public static final int INSTALL_FAILED_NEWER_SDK                       = -14;

    /**
     * Installation return code<br/>
     * the new package failed because it has specified that it is a test-only package and the caller has not supplied
     * the {@link #INSTALL_ALLOW_TEST} flag.
     */
    public static final int INSTALL_FAILED_TEST_ONLY                       = -15;

    /**
     * Installation return code<br/>
     * the package being installed contains native code, but none that is compatible with the the device's CPU_ABI.
     */
    public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE            = -16;

    /**
     * Installation return code<br/>
     * the new package uses a feature that is not available.
     */
    public static final int INSTALL_FAILED_MISSING_FEATURE                 = -17;

    /**
     * Installation return code<br/>
     * a secure container mount point couldn't be accessed on external media.
     */
    public static final int INSTALL_FAILED_CONTAINER_ERROR                 = -18;

    /**
     * Installation return code<br/>
     * the new package couldn't be installed in the specified install location.
     */
    public static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION        = -19;

    /**
     * Installation return code<br/>
     * the new package couldn't be installed in the specified install location because the media is not available.
     */
    public static final int INSTALL_FAILED_MEDIA_UNAVAILABLE               = -20;

    /**
     * Installation return code<br/>
     * the new package couldn't be installed because the verification timed out.
     */
    public static final int INSTALL_FAILED_VERIFICATION_TIMEOUT            = -21;

    /**
     * Installation return code<br/>
     * the new package couldn't be installed because the verification did not succeed.
     */
    public static final int INSTALL_FAILED_VERIFICATION_FAILURE            = -22;

    /**
     * Installation return code<br/>
     * the package changed from what the calling program expected.
     */
    public static final int INSTALL_FAILED_PACKAGE_CHANGED                 = -23;

    /**
     * Installation return code<br/>
     * the new package is assigned a different UID than it previously held.
     */
    public static final int INSTALL_FAILED_UID_CHANGED                     = -24;

    /**
     * Installation return code<br/>
     * if the parser was given a path that is not a file, or does not end with the expected '.apk' extension.
     */
    public static final int INSTALL_PARSE_FAILED_NOT_APK                   = -100;

    /**
     * Installation return code<br/>
     * if the parser was unable to retrieve the AndroidManifest.xml file.
     */
    public static final int INSTALL_PARSE_FAILED_BAD_MANIFEST              = -101;

    /**
     * Installation return code<br/>
     * if the parser encountered an unexpected exception.
     */
    public static final int INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION      = -102;

    /**
     * Installation return code<br/>
     * if the parser did not find any certificates in the .apk.
     */
    public static final int INSTALL_PARSE_FAILED_NO_CERTIFICATES           = -103;

    /**
     * Installation return code<br/>
     * if the parser found inconsistent certificates on the files in the .apk.
     */
    public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;

    /**
     * Installation return code<br/>
     * if the parser encountered a CertificateEncodingException in one of the files in the .apk.
     */
    public static final int INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING      = -105;

    /**
     * Installation return code<br/>
     * if the parser encountered a bad or missing package name in the manifest.
     */
    public static final int INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME          = -106;

    /**
     * Installation return code<br/>
     * if the parser encountered a bad shared user id name in the manifest.
     */
    public static final int INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID        = -107;

    /**
     * Installation return code<br/>
     * if the parser encountered some structural problem in the manifest.
     */
    public static final int INSTALL_PARSE_FAILED_MANIFEST_MALFORMED        = -108;

    /**
     * Installation return code<br/>
     * if the parser did not find any actionable tags (instrumentation or application) in the manifest.
     */
    public static final int INSTALL_PARSE_FAILED_MANIFEST_EMPTY            = -109;

    /**
     * Installation return code<br/>
     * if the system failed to install the package because of system issues.
     */
    public static final int INSTALL_FAILED_INTERNAL_ERROR                  = -110;
    /**
     * Installation return code<br/>
     * other reason
     */
    public static final int INSTALL_FAILED_OTHER                           = -1000000;
}
