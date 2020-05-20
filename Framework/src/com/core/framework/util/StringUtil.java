package com.core.framework.util;

import com.core.framework.develop.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Mark
 * Date: 11-4-18
 * Time: 下午5:06
 * To change this template use File | Settings | File Templates.
 */
public final class StringUtil {


    public static String join(Collection<String> s, String delimiter) {
        if (s.size() == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (String str : s) {
            sb.append(str).append(delimiter);
        }
        if (sb.length() > 0)
            sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    public static String inputStreamToString(final InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        return sb.toString();
    }

    public static String getFromStream(final InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[40960];
        int read;
        while ((read = br.read(buffer, 0, buffer.length)) > 0) {
            sb.append(buffer, 0, read);
        }
        br.close();
        return sb.toString();
    }

    public static Boolean isEmpty(String str) {
        return null == str || str.length() == 0 || "null".equals(str);
    }

    public static Boolean isEmptyTrim(String str) {
        return isEmpty(str) || str.trim().length() == 0;
    }

    public static boolean isNull(String str) {
        return isEmpty(str) || "null".equals(str);
    }

    public static String getValueOrDefault(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    public static String fromBytes(byte[] bytes) {
        StringBuffer buf = new StringBuffer("");
        for (int offset = 0; offset < bytes.length; offset++) {
            int i = bytes[offset];
            if (i < 0)
                i += 256;
            if (i < 16)
                buf.append("0");
            buf.append(Integer.toHexString(i));
        }
        return buf.toString();
    }

    public static JSONObject parseJSON(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            LogUtil.w(e);
            return new JSONObject();
        }
    }

    public static Map<String, Object> parseJSONToHash(String json) {
        JSONObject jo = parseJSON(json);
        Iterator<String> iter = jo.keys();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            while (iter.hasNext()) {
                String key = iter.next();
                map.put(key, jo.get(key));
            }
        } catch (JSONException e) {
            LogUtil.w(e);
        }
        return map;
    }

    public static Map<String, Object> parseHttpParamsToHash(String params) {
        Map<String, Object> map = new HashMap<String, Object>();
        String[] arr = params.split("&");
        for (String kv : arr) {
            if (kv.indexOf("=") > 0) {
                String[] kvArr = kv.split("=");
                map.put(kvArr[0], kvArr[1]);
            }
        }
        return map;
    }

    public static String simpleFormat(String str, Object... replacements) {
        String[] parts = str.split("%s");
        if (parts.length < 2) return str;
        StringBuilder sb = new StringBuilder();
        int rl = replacements.length;
        for (int i = 0; i < parts.length; i++) {
            sb.append(parts[i]);
            if (i < rl)
                sb.append(replacements[i]);
        }
        return sb.toString();
    }


    public static String getLeft(String chatParticipant, String participant) {

        if (StringUtil.isEmpty(chatParticipant)) return chatParticipant;
        String[] ss = chatParticipant.split(participant);
        if (ss.length >= 2)
            return ss[0];
        else return chatParticipant;
    }

    public static String getRight(String chatParticipant, String participant) {

        if (StringUtil.isEmpty(chatParticipant)) return chatParticipant;
        String[] ss = chatParticipant.split(participant);
        if (ss.length >= 2)
            return ss[1];
        else return chatParticipant;
    }

    public static boolean isServer(String jid) {
        return (jid != null) && (jid.startsWith("3_"));
    }

    public static String getFrist(String image_url_small) {
        if (isEmpty(image_url_small)) return image_url_small;

        return image_url_small;
    }

    public static String IDCardValidate(String IDStr) throws ParseException {
        String errorInfo = "";// 记录错误信息
        String[] ValCodeArr = {"1", "0", "x", "9", "8", "7", "6", "5", "4",
                "3", "2"};
        String[] Wi = {"7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7",
                "9", "10", "5", "8", "4", "2"};
        String Ai = "";
        // ================ 号码的长度 15位或18位 ================
        if (IDStr.length() != 15 && IDStr.length() != 18) {
            errorInfo = "身份证号码长度应该为15位或18位。";
            return errorInfo;
        }
        // =======================(end)========================

        // ================ 数字 除最后以为都为数字 ================
        if (IDStr.length() == 18) {
            Ai = IDStr.substring(0, 17);
        } else if (IDStr.length() == 15) {
            Ai = IDStr.substring(0, 6) + "19" + IDStr.substring(6, 15);
        }
        if (isNumeric(Ai) == false) {
            errorInfo = "身份证15位号码都应为数字 ; 18位号码除最后一位外，都应为数字。";
            return errorInfo;
        }
        // =======================(end)========================

        // ================ 出生年月是否有效 ================
        String strYear = Ai.substring(6, 10);// 年份
        String strMonth = Ai.substring(10, 12);// 月份
        String strDay = Ai.substring(12, 14);// 月份
        if (isDataFormat(strYear + "-" + strMonth + "-" + strDay) == false) {
            errorInfo = "身份证生日无效。";
            return errorInfo;
        }
        GregorianCalendar gc = new GregorianCalendar();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
        if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150
                || (gc.getTime().getTime() - s.parse(
                strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) {
            errorInfo = "身份证生日不在有效范围。";
            return errorInfo;
        }
        if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) {
            errorInfo = "身份证月份无效";
            return errorInfo;
        }
        if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) {
            errorInfo = "身份证日期无效";
            return errorInfo;
        }
        // =====================(end)=====================

        // ================ 地区码时候有效 ================
        Hashtable h = GetAreaCode();
        if (h.get(Ai.substring(0, 2)) == null) {
            errorInfo = "身份证地区编码错误。";
            return errorInfo;
        }
        // ==============================================

        // ================ 判断最后一位的值 ================
        int TotalmulAiWi = 0;
        for (int i = 0; i < 17; i++) {
            TotalmulAiWi = TotalmulAiWi
                    + Integer.parseInt(String.valueOf(Ai.charAt(i)))
                    * Integer.parseInt(Wi[i]);
        }
        int modValue = TotalmulAiWi % 11;
        String strVerifyCode = ValCodeArr[modValue];
        Ai = Ai + strVerifyCode;

        if (IDStr.length() == 18) {
            if (Ai.equalsIgnoreCase(IDStr) == false) {
                errorInfo = "身份证无效，不是合法的身份证号码";
                return errorInfo;
            }
        } else {
            return "";
        }
        // =====================(end)=====================
        return "";
    }

    /**
     * 功能：判断字符串是否为数字
     *
     * @param str
     * @return
     */
    private static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (isNum.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 功能：设置地区编码
     *
     * @return Hashtable 对象
     */
    private static Hashtable GetAreaCode() {
        Hashtable hashtable = new Hashtable();
        hashtable.put("11", "北京");
        hashtable.put("12", "天津");
        hashtable.put("13", "河北");
        hashtable.put("14", "山西");
        hashtable.put("15", "内蒙古");
        hashtable.put("21", "辽宁");
        hashtable.put("22", "吉林");
        hashtable.put("23", "黑龙江");
        hashtable.put("31", "上海");
        hashtable.put("32", "江苏");
        hashtable.put("33", "浙江");
        hashtable.put("34", "安徽");
        hashtable.put("35", "福建");
        hashtable.put("36", "江西");
        hashtable.put("37", "山东");
        hashtable.put("41", "河南");
        hashtable.put("42", "湖北");
        hashtable.put("43", "湖南");
        hashtable.put("44", "广东");
        hashtable.put("45", "广西");
        hashtable.put("46", "海南");
        hashtable.put("50", "重庆");
        hashtable.put("51", "四川");
        hashtable.put("52", "贵州");
        hashtable.put("53", "云南");
        hashtable.put("54", "西藏");
        hashtable.put("61", "陕西");
        hashtable.put("62", "甘肃");
        hashtable.put("63", "青海");
        hashtable.put("64", "宁夏");
        hashtable.put("65", "新疆");
        hashtable.put("71", "台湾");
        hashtable.put("81", "香港");
        hashtable.put("82", "澳门");
        hashtable.put("91", "国外");
        return hashtable;
    }

    /**
     * 验证日期字符串是否是YYYY-MM-DD格式
     *
     * @param str
     * @return
     */
    public static boolean isDataFormat(String str) {
        boolean flag = false;
        //String regxStr="[1-9][0-9]{3}-[0-1][0-2]-((0[1-9])|([12][0-9])|(3[01]))";
        String regxStr = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$";
        Pattern pattern1 = Pattern.compile(regxStr);
        Matcher isNo = pattern1.matcher(str);
        if (isNo.matches()) {
            flag = true;
        }
        return flag;
    }

    //身份证正则匹配
//    public static boolean isMatchingPersonId(String id) {
//        Log.e("0000000000000000", "0000000000000000");
////        String p15="^[1-9]\\d{7}((0[1-9])||(1[0-2]))((0[1-9])||(1\\d)||(2\\d)||(3[0-1]))\\d{3}$";
//        String p15 = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";
////        String p18="^[1-9]\\d{5}[1-9]\\d{3}((0[1-9])||(1[0-2]))((0[1-9])||(1\\d)||(2\\d)||(3[0-1]))\\d{3}([0-9]||X||x)$";
////        String p18="^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{4}$";
//        String p18 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X|x)$";
//        Pattern pattern;
//
//        if (id.length() < 16) {
//            pattern = Pattern.compile(p15, Pattern.DOTALL + Pattern.MULTILINE);
//        } else {
//            pattern = Pattern.compile(p18, Pattern.DOTALL + Pattern.MULTILINE);
//        }
//        return pattern.matcher(id).matches();
//
////        if (id.length() <= 15) {
////            return Pattern.compile(p15).matcher(id).matches();
////        } else {
////            return Pattern.compile(p18).matcher(id).matches();
////        }
//////        return pattern15.matcher(id).matches() || pattern18.matcher(id).matches();
//    }

    public static boolean isMatchingPhone(String phone) {
//        String p="^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
        String p = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0-4,5-9]))\\d{8}$";
        Pattern pattern = Pattern.compile(p);
        return pattern.matcher(phone).matches();
    }

    //    public static boolean isMatchingChiness(String name){
//        Pattern p=Pattern.compile("[\u4e00-\u9fa5]");
//        Matcher m=p.matcher(name);
//        return m.matches();
//    }
    public static boolean isMatchingChiness(String name) {
        int n = 0;
        for (int i = 0; i < name.length(); i++) {
            n = (int) name.charAt(i);
            if (!(19968 <= n && n < 40869)) {
                return false;
            }
        }
        return true;
    }


}
