package com.pewds.oussa.pleixt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.ArrayList;

@SuppressLint("AppCompatCustomView")
public class MonitoringEditText extends EditText
{
    private final Context context ;
    ArrayList<GoEditTextListener> listeners;
    public MonitoringEditText(Context context) {
        super(context);
        listeners = new ArrayList<>();
        this.context = context;
    }

    public MonitoringEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        listeners = new ArrayList<>();
        this.context = context;
    }

    public MonitoringEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        listeners = new ArrayList<>();
        this.context = context;
    }


    public void addListener(GoEditTextListener listener) {
        try {
            listeners.add(listener);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Here you can catch paste, copy and cut events
     */
    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean consumed = super.onTextContextMenuItem(id);
        switch (id){
            case android.R.id.cut:
                onTextCut();
                break;
            case android.R.id.paste:
                onTextPaste();
                break;
            case android.R.id.copy:
                onTextCopy();
        }
        return consumed;
    }

    public void onTextCut(){
    }

    public void onTextCopy(){
    }

    /**
     * adding listener for Paste for example
     */
    public void onTextPaste(){
        for (GoEditTextListener listener : listeners) {
            listener.onUpdate();
        }
    }
    public interface GoEditTextListener {
        void onUpdate();
    }
}