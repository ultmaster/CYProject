package net.ultech.cyproject.utils;

import java.util.Stack;

import android.os.Bundle;

public class MainActivityStack {

	public MainActivityStack() {
		super();
		this.mFragmentStack = new Stack<Integer>();
		this.mBundleStack = new Stack<Bundle>();
	}

	private Stack<Integer> mFragmentStack;
	private Stack<Bundle> mBundleStack;

	/**
	 * Remember to update your UI related to the stack after call this method.
	 * 
	 * @param bundle
	 *            The data parent fragment is going to push
	 * @param fragment
	 *            The fragment type requested to push
	 * @param requestLevel
	 *            The level of the fragment requested to push ( Use -1 to
	 *            pushback) Any fragment with priority after requestLevel
	 *            (including requestLevel) will be deleted.
	 */
	public void pushStack(Bundle bundle, Integer fragment, int requestLevel) {
		if (requestLevel != -1) {
			while (mBundleStack.size() > requestLevel) {
				mBundleStack.pop();
				mFragmentStack.pop();
			}
		}
		mBundleStack.push(bundle);
		mFragmentStack.push(fragment);
	}

	/**
	 * Remember to update your UI related to the stack after call this method.
	 * 
	 * @return the last element of mBundleStack. Remember to set the last
	 *         element before you decide to pop.
	 */
	public Bundle popBack() {
		mFragmentStack.pop();
		return mBundleStack.pop();
	}

	public Bundle getBack() {
		return mBundleStack.get(mBundleStack.size() - 1);
	}

	public void setBack(Bundle bundle) {
		mBundleStack.set(mBundleStack.size() - 1, bundle);
	}

}