package com.example.map.ActivitiesAndFragments.Activities;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.map.R;
import com.example.map.Utils.RelateToFragment_OnBack.OnBackPressListener;
import com.example.map.Utils.RelateToFragment_OnBack.RootFragment;

import java.util.ArrayList;
import java.util.List;


public class MainMenuFragment extends RootFragment implements View.OnClickListener {

    protected CustomViewPager pager;

    private ViewPagerAdapter adapter;
    Context context;

    String userId;
    ImageButton profileBtn, starBtn, binderBtn, messageBtn;
    public static final int HOME_TAB = 0;
    public static final int LIKE_TAB = 1;
    public static final int MSG_TAB = 2;
    public static final int PROFILE_TAB = 3;

    public MainMenuFragment() {
        //Required Empty
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        context = getContext();

        pager = view.findViewById(R.id.viewpager);
        pager.setOffscreenPageLimit(3);
        pager.setPagingEnabled(false);
        view.setOnClickListener(this);


        profileBtn = view.findViewById(R.id.profile_btn);
        profileBtn.setOnClickListener(this);

        binderBtn = view.findViewById(R.id.binder_btn);
        binderBtn.setOnClickListener(this);

        starBtn = view.findViewById(R.id.star_btn);
        starBtn.setOnClickListener(this);

        messageBtn = view.findViewById(R.id.message_btn);
        messageBtn.setOnClickListener(this);


        return view;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.profile_btn:
                clickProfile();
                setPagerPosition(PROFILE_TAB);
                break;

            case R.id.binder_btn:
                setPagerPosition(HOME_TAB);
                clickBinder();
                break;

            case R.id.star_btn:
                setPagerPosition(LIKE_TAB);
                clickStar();
                break;

            case R.id.message_btn:
                clickMessage();
                setPagerPosition(MSG_TAB);
                break;
        }
    }


    public void clickProfile() {

        profileBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_profile_color));
        binderBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_binder_gray));
        starBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_mylikes_gray));
        messageBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_message_gray));

    }


    public void clickBinder() {

        profileBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_profile_gray));
        binderBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_binder_color));
        starBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_mylikes_gray));
        messageBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_message_gray));

    }


    public void clickStar() {

        starBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_mylikes_color));
        profileBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_profile_gray));
        binderBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_binder_gray));
        messageBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_message_gray));

    }


    public void clickMessage() {

        profileBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_profile_gray));
        binderBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_binder_gray));
        starBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_mylikes_gray));
        messageBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_message_color));

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Note that we are passing childFragmentManager, not FragmentManager
        adapter = new ViewPagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);

        setupTabIcons();
    }


    public boolean onBackPressed() {
        // currently visible tab Fragment
        Log.e("getCurrentItem", "onBackPressed: " + pager.getCurrentItem());
        OnBackPressListener currentFragment = (OnBackPressListener) adapter.getRegisteredFragment(pager.getCurrentItem());
        if (currentFragment != null) {
            if (pager.getCurrentItem() == HOME_TAB) {
                return currentFragment.onBackPressed();
            } else {
                setPagerPosition(HOME_TAB);
                return true;
            }
        }
        return false;
    }


    private void setupTabIcons() {
/*        Userlikes_F bottomSheetDialog = Userlikes_F.newInstance();
        bottomSheetDialog.show(getActivity().getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");*/
        adapter.addFrag(new Users_F());
        adapter.addFrag(Userlikes_F.newInstance());
        adapter.addFrag(new Inbox_F());
        adapter.addFrag(new Profile_F());

        pager.setCurrentItem(HOME_TAB);
        adapter.notifyDataSetChanged();

        Log.d("", "setupTabIcons");
        Log.d("", "MainMenuActivity.actionType:" + MainMenuActivity.actionType);

        if (MainMenuActivity.actionType.equals("message")) {
            pager.setCurrentItem(2);

        } else if (MainMenuActivity.actionType.equals("match")) {
            pager.setCurrentItem(2);
        } else {
            pager.setCurrentItem(0);
        }

    }


    class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        public void replaceFrag(int pos, Fragment fragment) {
            registeredFragments.remove(pos);
            mFragmentList.remove(pos);
            mFragmentList.add(pos, fragment);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }


    //open the chat fragment and on item click and pass your id and the other person id in which
    //you want to chat with them and this parameter is that is we move from match list or inbox list
    int current_position;

    public void setPagerPosition(int position) {
        pager.setCurrentItem(position);
        current_position = position;
        if (current_position == HOME_TAB) {
            clickBinder();
        } else if (current_position == LIKE_TAB) {
            clickStar();

        } else if (current_position == MSG_TAB) {
            clickMessage();

        } else {
            clickProfile();

        }
    }

}