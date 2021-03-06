package com.supermax.base.common.http;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.supermax.base.common.exception.QsException;
import com.supermax.base.common.exception.QsExceptionType;
import com.supermax.base.common.log.L;
import com.supermax.base.common.utils.StreamCloseUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * @Author yinzh
 * @Date 2018/10/19 17:45
 * @Description
 */
public class HttpConverter {
    private final static String TAG = "HeepConverter";

    private Gson gson;

    HttpConverter() {
        this.gson = new Gson();
    }

    Object jsonToObject(String jsonStr, Type type) {
        return gson.fromJson(jsonStr, type);
    }

    RequestBody stringToBody(String methodName, String mimeType, String body) {
        L.i(TAG, "methodName:" + methodName + "  请求体 mimeType:" + mimeType + ", String:" + body);
        return RequestBody.create(MediaType.parse(mimeType), body);
    }

    RequestBody jsonToBody(String methodName, String mimeType, Object object, Type type) {
        String json = gson.toJson(object, type);
        L.i(TAG, "methodName:" + methodName + "  请求体 mimeType:" + mimeType + ", Json : " + json);
        return RequestBody.create(MediaType.parse(mimeType), json);
    }

    RequestBody fileToBody(String methodName, String mimeType, File file) {
        L.i(TAG, "methodName:" + methodName + "  请求体 mimeType:" + mimeType + ", File:" + file.getPath());
        return RequestBody.create(MediaType.parse(mimeType), file);
    }

    RequestBody byteToBody(String methodName, String mimeType, byte[] bytes) {
        L.i(TAG, "methodName:" + methodName + "  请求体 mimeType:" + mimeType + ", bytes length:" + bytes.length);
        return RequestBody.create(MediaType.parse(mimeType), bytes);
    }


    RequestBody stringToFormBody(String methodName, Object formBody) {
        L.i(TAG, "methodName:" + methodName + "  提交表单:" + formBody.getClass().getSimpleName());
        FormBody.Builder builder = new FormBody.Builder();
        if (formBody instanceof Map) {
            Map dataMap = (Map) formBody;
            for (Object key : dataMap.keySet()) {
                String keyStr = String.valueOf(key);
                String valueStr = String.valueOf(dataMap.get(key));
                if (!TextUtils.isEmpty(keyStr) && !TextUtils.isEmpty(valueStr))
                    builder.add(keyStr, valueStr);
            }
        } else if (formBody instanceof String) {
            String formStr = (String) formBody;
            String[] paramArr = formStr.split("&");
            for (String param : paramArr) {
                if (!TextUtils.isEmpty(param)) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && !TextUtils.isEmpty(keyValue[0]) && !TextUtils.isEmpty(keyValue[1])) {
                        builder.add(keyValue[0], keyValue[1]);
                    }
                }
            }
        } else {
            Field[] fieldArr = formBody.getClass().getFields();
            if (fieldArr != null && fieldArr.length > 0) {
                try {
                    for (Field field : fieldArr) {
                        Object value = field.get(formBody);
                        if (value != null) {
                            builder.add(field.getName(), String.valueOf(value));
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.build();
    }


    /**
     * 将Json格式输出
     *
     * @param sourceStr
     * @return
     */
    String formatJson(String sourceStr) {
        if (TextUtils.isEmpty(sourceStr)) return null;
        String str = unicodeToCn(sourceStr);
        int level = 0;

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < str.length(); index++) {
            char c = str.charAt(index);
            if (level > 0 && '\n' == builder.charAt(builder.length() - 1)) {
                builder.append(getLevelStr(level));
            }

            switch (c) {
                case '{':
                case '[':
                    builder.append(c).append("\n");
                    level++;
                    break;
                case ',':
                    builder.append(c).append("\n");
                    break;
                case '}':
                case ']':
                    builder.append("\n");
                    level--;
                    builder.append(getLevelStr(level));
                    builder.append(c);
                    break;
                default:
                    builder.append(c);
                    break;
            }
        }
        return builder.toString();
    }

    private String getLevelStr(int level) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            builder.append("\t");
        }
        return builder.toString();
    }


    /**
     * 字符串中，所有以 \\u 开头的UNICODE字符串，全部替换为汉字
     */
    private String unicodeToCn(final String str) {
        String singlePattern = "[0-9|a-f|A-F]";
        String pattern = singlePattern + singlePattern + singlePattern + singlePattern;
        StringBuilder sb = new StringBuilder();
        int length = str.length();

        for (int i = 0; i < length; ) {
            String tmpStr = str.substring(i);
            if (isStartWithUnicode(pattern, tmpStr)) {//分支1
                sb.append(unicodeToCnSingle(tmpStr));
                i += 6;
            } else {
                sb.append(str, i, i + 1);
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * 字符串是否以Unicode字符开头。约定Unicode字符以 \\u 开头
     */
    private boolean isStartWithUnicode(String pattern, String str) {
        if (TextUtils.isEmpty(str) || !str.startsWith("\\u") || str.length() <= 6) {
            return false;
        }
        String content = str.substring(2, 6);
        return Pattern.matches(pattern, content);
    }

    /**
     * 把"\\u" 开头的单字转换成汉字，如\\u6B65  ----> 步
     */
    private String unicodeToCnSingle(final String str) {
        int code = Integer.decode("0x" + str.substring(2, 6));
        char c = (char) code;
        return String.valueOf(c);
    }

}
