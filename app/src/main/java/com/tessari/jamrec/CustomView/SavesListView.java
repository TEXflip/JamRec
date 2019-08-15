package com.tessari.jamrec.CustomView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tessari.jamrec.Activity.FileSelectionDialog;

import java.io.File;
import java.util.ArrayList;

public class SavesListView extends ListView {

    File[] files;
    FileSelectionDialog.OnFileActionChosenListener fileActionChosenListener;

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
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    public void setOnFileActionChosenListener(FileSelectionDialog.OnFileActionChosenListener listener){
        fileActionChosenListener = listener;
    }
}
