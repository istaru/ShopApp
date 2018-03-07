package com.shhb.gd.shop.tools;

import android.os.AsyncTask;

/**
 * Created by Moon on 2016/4/5.
 */
public class AsyncUtil extends AsyncTask<String, Integer, Long> {

    /**
     * 第一个执行的方法
     * 作用：可以在该方法中做一些准备工作，如在界面上显示一个进度条，或者一些控件的实例化。这个方法可以不用实现。
     */
    @Override
    public void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * 执行时机：在onPreExecute 方法执行后马上执行，该方法运行在后台线程中
     * 作用：主要负责执行那些很耗时的后台处理工作。该方法是抽象方法，子类必须实现。
     */
    @Override
    public Long doInBackground(String... params) {
        return null;
    }

    /**
     * 执行时机：这个函数在doInBackground调用publishProgress时被调用后，UI 线程将调用这个方法.虽然此方法只有一个参数,但此参数是一个数组，可以用values[i]来调用
     * 作用：在界面上展示任务的进展情况，例如通过一个进度条进行展示。此实例中，该方法会被执行100次
     */
    @Override
    public void onProgressUpdate(Integer... values) {

    }

    /**
     *onPostExecute方法用于在执行完后台任务后更新UI,显示结果
     *
     */
    @Override
    public void onPostExecute(Long result) {
        super.onPostExecute(result);
    }
}
