package com.l134046zain.assingment3;



import android.database.Cursor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

public class MyDictionary extends AppCompatActivity{

    ListView _listView;
    EditText _edit;
    String[] mProjection = {
            CustomProvider.UID,
            CustomProvider.WORD,
            CustomProvider.MEANING
    };
    Cursor cursor;
    CustomAdapter customAdapter;

    private static final int URL_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_dictionary);

        _listView = (ListView) findViewById(R.id.list);
        _edit = (EditText) findViewById(R.id.editText);

        cursor=getContentResolver().query(CustomProvider.CONTENT_URI,mProjection,null,null,null);

        customAdapter=new CustomAdapter(this,cursor);

        _listView.setAdapter(customAdapter);
        _listView.setTextFilterEnabled(true);

        customAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return getContentResolver().query(CustomProvider.CONTENT_URI,mProjection,"word LIKE ?",new String[]{charSequence.toString()+"%"},null);
            }
        });


        _edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                customAdapter.getFilter().filter(charSequence.toString());
                customAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }




}
