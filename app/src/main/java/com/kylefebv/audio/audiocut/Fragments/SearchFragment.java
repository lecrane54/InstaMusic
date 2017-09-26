package com.kylefebv.audio.audiocut.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kylefebv.audio.audiocut.Activities.MainActivity;
import com.kylefebv.audio.audiocut.Adapters.UserListAdapter;
import com.kylefebv.audio.audiocut.Models.User;
import com.kylefebv.audio.audiocut.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class SearchFragment extends Fragment {
    private EditText mEditText;
    private ListView mListView;
    private List<User> mUserList;
    private UserListAdapter mAdapter;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View v = inflater.inflate(R.layout.fragment_search, container, false);
        mEditText = (EditText)v.findViewById(R.id.edittext);
        mListView = (ListView)v.findViewById(R.id.listView1);
        initTextListener();
        mEditText.requestFocus();
        showKeyboard();
        return v;
    }

    private void initTextListener(){


        mUserList = new ArrayList<>();

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String text = mEditText.getText().toString();
                searchForMatch(text);
            }
        });
    }


    private void searchForMatch(String keyword){

        mUserList.clear();
        //update the users list view
        if(keyword.length() ==0){

        }else{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child("users")
                    .orderByChild("name").equalTo(keyword);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        Log.d("dad", "onDataChange: found user:" + singleSnapshot.getValue(User.class).toString());
                        mUserList.add(singleSnapshot.getValue(User.class));
                        //update the users list view
                        updateUsersList();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void updateUsersList(){

        mAdapter = new UserListAdapter(getActivity(), R.layout.userlist_item, mUserList);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideSoftKeyboard();
                if(mAdapter.getItem(position).getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                {
                    Bundle b = new Bundle();
                    b.putString("uid",mAdapter.getItem(position).getUid());
                    b.putString("name",mAdapter.getItem(position).getName());
                    ((MainActivity)getContext()).switchToSingle(b, new UserProfileFragment());
                }else{
                    Bundle b = new Bundle();
                    b.putString("uid",mAdapter.getItem(position).getUid());
                    b.putString("name",mAdapter.getItem(position).getName());
                    ((MainActivity)getContext()).switchToSingle(b,new OtherUserFragment());
                }

            }
        });
    }

    private void hideSoftKeyboard(){
        if(getActivity().getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void showKeyboard(){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
    }
}
