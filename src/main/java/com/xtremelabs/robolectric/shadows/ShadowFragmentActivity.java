package com.xtremelabs.robolectric.shadows;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.tester.android.util.TestFragmentManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Implements(FragmentActivity.class)
public class ShadowFragmentActivity extends ShadowActivity {
    @RealObject
    FragmentActivity realObject;

    private FragmentManager fragmentManager;
    public static final String FRAGMENTS_TAG = "android:fragments";

    public void __constructor__() {
        fragmentManager = new TestFragmentManager(realObject);
    }

    @Implementation
    public FragmentManager getSupportFragmentManager() {
        return fragmentManager;
    }

    @Override
    public void onSaveInstanceState_forBogusActivityShadows(Bundle outState) {
        // We cannot figure out how to pass the RobolectricWiring test without doing this incredibly
        // terrible looking hack.  I am very sorry.
        super.onSaveInstanceState(outState);

        List<SerializedFragmentState> fragmentStates = new ArrayList<SerializedFragmentState>();

        for (Map.Entry<Integer, Fragment> entry : ((TestFragmentManager) fragmentManager).getFragments().entrySet()) {
            fragmentStates.add(new SerializedFragmentState(entry.getKey(), entry.getValue()));
        }

        outState.putSerializable(FRAGMENTS_TAG, fragmentStates.toArray());
    }

    @Override
    public void onRestoreInstanceState_forBogusActivityShadows(Bundle savedInstanceState) {
        // We cannot figure out how to pass the RobolectricWiring test without doing this incredibly
        // terrible looking hack.  I am very sorry.
        super.onRestoreInstanceState(savedInstanceState);

        Object[] stuff = (Object[]) savedInstanceState.getSerializable(FRAGMENTS_TAG);

        for (Object o : stuff) {
            SerializedFragmentState fragmentState = (SerializedFragmentState) o;

            try {
                fragmentManager.beginTransaction().add(fragmentState.containerId, fragmentState.fragmentClass.newInstance(), fragmentState.tag).commit();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
