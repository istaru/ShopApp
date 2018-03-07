package com.shhb.gd.shop.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by Kiven on 16/12/2.
 */

public class MainAdapter extends FragmentPagerAdapter {
	private ArrayList<Fragment> fragment;// 需要添加到上面的Fragment

	public MainAdapter(FragmentManager fm) {
		super(fm);
	}

	/**
	 * 自定义的构造函数
	 * @param fm
	 * @param fragment
	 */
	public MainAdapter(FragmentManager fm, ArrayList<Fragment> fragment) {
		super(fm);
		this.fragment = fragment;
	}

	@Override
	public Fragment getItem(int arg0) {
		return fragment.get(arg0);// 返回Fragment对象
	}

	@Override
	public int getCount() {
		return fragment.size();// 返回Fragment的个数
	}
	
//	@Override
//	public CharSequence getPageTitle(int position) {
//		return (CharSequence) fragments.get(position);
//	}
}