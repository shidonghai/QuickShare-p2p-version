
package com.cloudsynch.quickshare.ui;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.cloudsynch.quickshare.R;

public class OperationDialog extends PopupWindow {

    public static final int TYPE_APP = 0;

    public static final int TYPE_IMAGE = 1;

    public static final int TYPE_MUSIC = 2;

    public static final int TYPE_VIDEO = 3;

    public static final int TYPE_FILE = 4;

    private final int Y_OFF_BASE = -40;

    private int mScreenH;

    private Context mContext;

    public OperationDialog(Context context) {
        super(context);
        mContext = context;
        setWindowLayoutMode(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        setFocusable(true);
        setOutsideTouchable(true);

        // If do not setBackgroundDrawable, setOutsideTouchable will no used.
        setBackgroundDrawable(new BitmapDrawable());

        WindowManager windowManage = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        mScreenH = windowManage.getDefaultDisplay().getHeight();
    }

    @Override
    public void showAsDropDown(View anchor) {
        int xOff = getXoff(anchor);
        int yOff = getYoff(anchor);
        super.showAsDropDown(anchor, xOff, yOff);
    }

    private int getXoff(View anchor) {
        int anchorWidth = anchor.getWidth();
        getContentView().measure(0, 0);
        int contentWidth = getContentView().getMeasuredWidth();

        if (anchorWidth > contentWidth) {
            return anchorWidth / 2 - contentWidth / 2;
        }
        return 0;
    }

    private int getYoff(View anchor) {
        int anchorHeight = anchor.getHeight();
        getContentView().measure(0, 0);
        int contentHeight = getContentView().getMeasuredHeight();
        int[] location = new int[2];
        anchor.getLocationInWindow(location);
        int y = location[1] + anchorHeight;
        int spaceLeft = mScreenH - y;
        Log.d("zxh", "spaceLeft:" + spaceLeft + "   contentHeight:"
                + contentHeight);
        if (spaceLeft < contentHeight) {
            return -(contentHeight + anchorHeight + Y_OFF_BASE);
        }

        return Y_OFF_BASE;

    }

    public void setContent(final OnClickListener itemClickListener) {
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.history_item_menu, null);
        view.findViewById(R.id.history_item_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onClick(null, R.id.history_item_open);
            }
        });
        view.findViewById(R.id.history_item_del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onClick(null, R.id.history_item_del);
            }
        });
        view.findViewById(R.id.history_item_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onClick(null, R.id.history_item_share);
            }
        });
        view.findViewById(R.id.history_item_attribute).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        itemClickListener.onClick(null, R.id.history_item_attribute);
                    }
                });
        linearLayout.addView(view);
        setContentView(linearLayout);
    }

}
