package com.core.framework.util;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.core.framework.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author:jjj
 * time:2017/3/14 12:00
 * TODO:权限申请
 */
public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();
    public static final int CODE_RECORD_AUDIO = 0;
    public static final int CODE_GET_ACCOUNTS = 1;
    public static final int CODE_READ_PHONE_STATE = 2;
    public static final int CODE_CALL_PHONE = 3;
    public static final int CODE_CAMERA = 4;
    public static final int CODE_ACCESS_FINE_LOCATION = 5;
    public static final int CODE_ACCESS_COARSE_LOCATION = 6;
    public static final int CODE_READ_EXTERNAL_STORAGE = 7;
    public static final int CODE_WRITE_EXTERNAL_STORAGE = 8;
    public static final int CODE_SYSTEM_ALERT_WINDOW = 9;
    public static final int CODE_READ_CONTACTS = 10;
    public static final int CODE_MULTI_PERMISSION = 100;

    public static final String PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final String PERMISSION_GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS;
    public static final String PERMISSION_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final String PERMISSION_CALL_PHONE = Manifest.permission.CALL_PHONE;
    public static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    public static final String PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String PERMISSION_ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String PERMISSION_SYSTEM_ALERT_WINDOW = Manifest.permission.SYSTEM_ALERT_WINDOW;
    public static final String PERMISSION_READ_CONTACTS = Manifest.permission.READ_CONTACTS;

    private static final String[] requestPermissions = {
            PERMISSION_RECORD_AUDIO,
            PERMISSION_GET_ACCOUNTS,
            PERMISSION_READ_PHONE_STATE,
            PERMISSION_CALL_PHONE,
            PERMISSION_CAMERA,
            PERMISSION_ACCESS_FINE_LOCATION,
            PERMISSION_ACCESS_COARSE_LOCATION,
            PERMISSION_READ_EXTERNAL_STORAGE,
            PERMISSION_WRITE_EXTERNAL_STORAGE,
            PERMISSION_SYSTEM_ALERT_WINDOW,
            PERMISSION_READ_CONTACTS
    };
    private static final String[] permissionsMsg = {
            "录音",
            "访问GMail账户列表",
            "访问电话状态",
            "拨打电话",
            "相机",
            "定位",
            "定位",
            "文件读取",
            "文件存储",
            "通知栏权限",
            "联系人权限"
    };

    public interface PermissionResultListener {
        void onPermissionFail(int requestCode);

        void onPermissionSuccess(int requestCode);

    }

    /**
     * Requests permission.
     *
     * @param activity
     * @param requestCode request code, e.g. if you need request CAMERA permission,parameters is PermissionUtils.CODE_CAMERA
     */
    public static void requestPermission(final Activity activity, final int requestCode, PermissionResultListener resultListener) {
        if (activity == null) {
            return;
        }
        if (requestCode < 0 || requestCode >= requestPermissions.length) {
            Log.w(TAG, "requestPermission illegal requestCode:" + requestCode);
            return;
        }

        final String requestPermission = requestPermissions[requestCode];

        //如果是6.0以下的手机，ActivityCompat.checkSelfPermission()会始终等于PERMISSION_GRANTED，
        // 但是，如果用户关闭了你申请的权限，ActivityCompat.checkSelfPermission(),会导致程序崩溃(java.lang.RuntimeException: Unknown exception code: 1 msg null)，
        // 你可以使用try{}catch(){},处理异常，也可以在这个地方，低于23就什么都不做，
        // 个人建议try{}catch(){}单独处理，提示用户开启权限。
        if (Build.VERSION.SDK_INT < 23) {
            resultListener.onPermissionSuccess(requestCode);
            return;
        }

        int checkSelfPermission;
        try {
            checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission);
        } catch (RuntimeException e) {
            Toast.makeText(activity, "权限开启失败：" + e.getMessage().toString() + "！请重试~", Toast.LENGTH_SHORT).show();
            resultListener.onPermissionFail(requestCode);
            return;
        }

        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requestPermission)) {
                shouldShowRationale(activity, requestCode, requestPermission, resultListener);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{requestPermission}, requestCode);
            }

        } else {
            resultListener.onPermissionSuccess(requestCode);
        }
    }

    private static void requestMultiResult(Activity activity, String[] permissions, int[] grantResults, PermissionResultListener resultListener) {

        if (activity == null) {
            return;
        }

        Log.d(TAG, "onRequestPermissionsResult permissions length:" + permissions.length);
        Map<String, Integer> perms = new HashMap<>();

        ArrayList<String> notGranted = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            Log.d(TAG, "permissions: [i]:" + i + ", permissions[i]" + permissions[i] + ",grantResults[i]:" + grantResults[i]);
            perms.put(permissions[i], grantResults[i]);
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                notGranted.add(permissions[i]);
            }
        }

        if (notGranted.size() == 0) {
            resultListener.onPermissionSuccess(CODE_MULTI_PERMISSION);
        } else {
            openSettingActivity(activity, -1, resultListener);
        }

    }


    /**
     * 一次申请多个权限 需要的时候修改
     */
    public static void requestMultiPermissions(final Activity activity, String[] permissions, final PermissionResultListener resultListener) {
        final List<String> shouldRationalePermissionsList = getNoGrantedPermission(activity, true, resultListener);

        if (permissions == null || shouldRationalePermissionsList == null) {
            resultListener.onPermissionFail(CODE_MULTI_PERMISSION);
            return;
        }

        if (permissions.length > 0) {
            ActivityCompat.requestPermissions(activity, permissions, CODE_MULTI_PERMISSION);
            Log.d(TAG, "showMessageOKCancel requestPermissions");

        } else if (shouldRationalePermissionsList.size() > 0) {
            showMessageOKCancel(activity, "您需要打开以下权限", new PermissionDoListener() {
                @Override
                public void onCancleClick() {
                    resultListener.onPermissionFail(CODE_MULTI_PERMISSION);
                }

                @Override
                public void onOkClick() {
                    ActivityCompat.requestPermissions(activity, shouldRationalePermissionsList.toArray(new String[shouldRationalePermissionsList.size()]), CODE_MULTI_PERMISSION);
                    Log.d(TAG, "showMessageOKCancel requestPermissions");
                }
            });
        } else {
            resultListener.onPermissionSuccess(CODE_MULTI_PERMISSION);
        }

    }

//     */一次申请多个权限
//    public static void requestMultiPermissions(final Activity activity,  final PermissionResultListener resultListener) {
//
//        final List<String> permissionsList = getNoGrantedPermission(activity, false,resultListener);
//        final List<String> shouldRationalePermissionsList = getNoGrantedPermission(activity, true,resultListener);
//
//        if (permissionsList == null || shouldRationalePermissionsList == null) {
//            resultListener.onErro();
//            return;
//        }
//        Log.d(TAG, "requestMultiPermissions permissionsList:" + permissionsList.size() + ",shouldRationalePermissionsList:" + shouldRationalePermissionsList.size());
//
//        if (permissionsList.size() > 0) {
//            ActivityCompat.requestPermissions(activity, permissionsList.toArray(new String[permissionsList.size()]),
//                    CODE_MULTI_PERMISSION);
//            Log.d(TAG, "showMessageOKCancel requestPermissions");
//
//        } else if (shouldRationalePermissionsList.size() > 0) {
//            showMessageOKCancel(activity, "你需要打开以下权限",
//                    new DialogInterface.OnClickListener2() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            ActivityCompat.requestPermissions(activity, shouldRationalePermissionsList.toArray(new String[shouldRationalePermissionsList.size()]),
//                                    CODE_MULTI_PERMISSION);
//                            Log.d(TAG, "showMessageOKCancel requestPermissions");
//                        }
//                    }, new DialogInterface.OnClickListener2() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            resultListener.onFail();
//                        }
//                    });
//        } else {
//            resultListener.onSuccess(CODE_MULTI_PERMISSION);
//        }
//
//    }

    /**
     * 提示开启权限
     *
     * @param activity
     * @param requestCode
     * @param requestPermission
     * @param resultListener
     */
    private static void shouldShowRationale(final Activity activity, final int requestCode, final String requestPermission, final PermissionResultListener resultListener) {

        showMessageOKCancel(activity, "为了您能正常的使用该功能，请开启" + permissionsMsg[requestCode] + "权限", new PermissionDoListener() {
            @Override
            public void onCancleClick() {
                Toast.makeText(activity, permissionsMsg[requestCode] + "权限开启失败!", Toast.LENGTH_SHORT).show();
                resultListener.onPermissionFail(requestCode);
            }

            @Override
            public void onOkClick() {
                ActivityCompat.requestPermissions(activity, new String[]{requestPermission}, requestCode);
            }
        });
    }

    public interface PermissionDoListener {
        void onCancleClick();

        void onOkClick();
    }

    /**
     * 弹出权限框并让用户选择开启或者关闭
     *
     * @param context
     * @param message
     * @param doListener
     */
    private static void showMessageOKCancel(final Activity context, String message, final PermissionDoListener doListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_permission, null);
        final Dialog dialog = DialogUtil.getCenterDialog(context, view);
        TextView msgTv = (TextView) view.findViewById(R.id.dialogPermission_msgTv);
        msgTv.setText(message);
        view.findViewById(R.id.dialogPermission_cancleTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                doListener.onCancleClick();
            }
        });
        view.findViewById(R.id.dialogPermission_okTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                doListener.onOkClick();
            }
        });
        dialog.show();
    }

    /**
     * @param activity
     * @param requestCode  Need consistent with requestPermission
     * @param permissions
     * @param grantResults
     */
    public static void requestPermissionsResult(final Activity activity, final int requestCode, @NonNull String[] permissions,
                                                @NonNull int[] grantResults, PermissionResultListener resultListener) {

        if (activity == null) {
            return;
        }
        Log.d(TAG, "requestPermissionsResult requestCode:" + requestCode);

        if (requestCode == CODE_MULTI_PERMISSION) {
            //修改后加上
            requestMultiResult(activity, permissions, grantResults, resultListener);
            return;
        }

        if (requestCode < 0 || requestCode >= requestPermissions.length) {
            resultListener.onPermissionFail(requestCode);
            return;
        }

        Log.i(TAG, "onRequestPermissionsResult requestCode:" + requestCode + ",permissions:" + permissions.toString()
                + ",grantResults:" + grantResults.toString() + ",length:" + grantResults.length);

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            resultListener.onPermissionSuccess(requestCode);

        } else {
            Log.i(TAG, "onRequestPermissionsResult PERMISSION NOT GRANTED");
            openSettingActivity(activity, requestCode, resultListener);
        }

    }

    /**
     * 打开设置界面开启权限
     *
     * @param activity
     * @param requestCode
     * @param resultListener
     */
    private static void openSettingActivity(final Activity activity, final int requestCode, final PermissionResultListener resultListener) {
        String msg = "为了您的正常使用，请开启文件存储权限和定位权限";
        if (requestCode != -1) {
            msg = "为了您的正常使用，请开启" + permissionsMsg[requestCode] + "权限";
        }
        showMessageOKCancel(activity, msg, new PermissionDoListener() {
            @Override
            public void onCancleClick() {
                if (requestCode > 0) {
                    Toast.makeText(activity, permissionsMsg[requestCode] + "权限开启失败!", Toast.LENGTH_SHORT).show();
                }
                resultListener.onPermissionFail(requestCode);
            }

            @Override
            public void onOkClick() {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivityForResult(intent, 1122);
            }
        });
    }


    /**
     * @param activity
     * @param isShouldRationale true: return no granted and shouldShowRequestPermissionRationale permissions, false:return no granted and !shouldShowRequestPermissionRationale
     * @return
     */
    public static ArrayList<String> getNoGrantedPermission(Activity activity, boolean isShouldRationale, PermissionResultListener resultListener) {

        ArrayList<String> permissions = new ArrayList<>();

        for (int i = 0; i < requestPermissions.length; i++) {
            String requestPermission = requestPermissions[i];


            int checkSelfPermission = -1;
            try {
                checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission);
            } catch (RuntimeException e) {
                resultListener.onPermissionFail(-1);
                return null;
            }

            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "getNoGrantedPermission ActivityCompat.checkSelfPermission != PackageManager.PERMISSION_GRANTED:" + requestPermission);

                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requestPermission)) {
                    Log.d(TAG, "shouldShowRequestPermissionRationale if");
                    if (isShouldRationale) {
                        permissions.add(requestPermission);
                    }

                } else {

                    if (!isShouldRationale) {
                        permissions.add(requestPermission);
                    }
                    Log.d(TAG, "shouldShowRequestPermissionRationale else");
                }
            }
        }

        return permissions;
    }

}