package com.tessari.jamrec.CustomView;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tessari.jamrec.Activity.FileSelectionDialog;
import com.tessari.jamrec.R;

import java.io.File;
import java.util.ArrayList;

public class SavesListView extends ListView {

    File[] files;
    FileSelectionDialog.OnFileActionChosenListener fileActionChosenListener;
    View view;
    int top1 = 0, top2 = 0;

    public SavesListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



    public void setFiles(final File[] files) {
        this.files = files;
        ArrayList<String> filesList = new ArrayList<>();
        for (File f : files) {
            String name = f.getName();
            filesList.add(name.substring(0, name.lastIndexOf('.')));
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, filesList);
        this.setAdapter(arrayAdapter);
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FileSelectionDialog fsd = new FileSelectionDialog(getContext(),files[i]);
                fsd.setOnFileActionChosenListener(fileActionChosenListener);
                fsd.show();
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            this.setVisibility(GONE);
        else
            this.setVisibility(VISIBLE);
    }

    public void setViewToConstrain(View view){
        this.view = view;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //int maxHeight = (int) getResources().getDimension(R.dimen.saveList_max_height);


//        if(MeasureSpec.getSize(heightMeasureSpec) > maxHeight)
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), heightMeasureSpec);
    }

    public void setOnFileActionChosenListener(FileSelectionDialog.OnFileActionChosenListener listener){
        fileActionChosenListener = listener;
    }
}
