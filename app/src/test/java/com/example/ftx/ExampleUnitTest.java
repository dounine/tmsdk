package com.example.ftx;

import org.junit.Test;

import static org.junit.Assert.*;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void download() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();
        Request request = new Request.Builder().get().url("https://cdn.kuaiyugo.com/SDK/h5_sdk/update/wx1968f4cbe8ebfe5d/update.json").build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            String body = response.body().string();
            System.out.println(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}