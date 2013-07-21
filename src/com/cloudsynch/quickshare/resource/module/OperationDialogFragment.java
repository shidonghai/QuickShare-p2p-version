package com.cloudsynch.quickshare.resource.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.utils.FileUtil;

/**
 * Created by Xiaohu on 13-6-14.
 */
public class OperationDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final int DELETE = 0;

    public static final int RENAME = 1;

    public static final int COPY = 2;

    public static final int CUT = 3;

    public static final String OPERATION_TYPE = "operation_type";

    private final int UPDATE = 0;

    private final int DELETE_FINISH = 1;

    private final int COPY_FINISH = 2;

    private final int CUT_FINISH = 3;

    private List<File> mFiles;

    private File mDestFile;

    private TextView mSummary;

    private EditText mEdit;

    private int mOperationType;

    private View mBottomLayout;

    private OnCompleteListener mCompleteListener;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("zxh", "handleMessage:" + msg.obj);

            switch (msg.what) {
                case UPDATE:
                    mSummary.setText((String) msg.obj);
                    mBottomLayout.setVisibility(View.GONE);
                    break;
                case DELETE_FINISH:
                    if (null != mCompleteListener) {
                        mCompleteListener.onComplete();
                    }
                    Toast.makeText(getActivity(), R.string.resource_manager_delete_finish,
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                    break;
                case COPY_FINISH:
                    if (null != mCompleteListener) {
                        mCompleteListener.onComplete();
                    }
                    Toast.makeText(getActivity(), R.string.resource_manager_copy_finish,
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                    break;
                case CUT_FINISH:
                    if (null != mCompleteListener) {
                        mCompleteListener.onComplete();
                    }
                    Toast.makeText(getActivity(), R.string.resource_manager_cut_finish,
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_layout, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mOperationType = getArguments().getInt(OPERATION_TYPE);

        TextView title = (TextView) view.findViewById(R.id.title);
        mSummary = (TextView) view.findViewById(R.id.summary);
        mEdit = (EditText) view.findViewById(R.id.edit);
        mBottomLayout = view.findViewById(R.id.bottom);

        if (null == mFiles || mFiles.size() == 0) {
            dismiss();
            return;
        }

        switch (mOperationType) {
            case DELETE:
                title.setText(R.string.resource_manager_delete);
                mSummary.setText(String.format(getString(R.string.resource_manager_delete_summary),
                        mFiles.get(0).getName()));
                mSummary.setVisibility(View.VISIBLE);
                mEdit.setVisibility(View.GONE);
                break;
            case RENAME:
                title.setText(R.string.resource_manager_rename);
                mSummary.setVisibility(View.GONE);
                mEdit.setVisibility(View.VISIBLE);

                String name = mFiles.get(0).getName();
                mEdit.setText(name);
                mEdit.setSelection(0, name.length());
                break;
            case COPY:
                title.setText(R.string.resource_manager_copy);
                mSummary.setVisibility(View.VISIBLE);
                mEdit.setVisibility(View.GONE);
                if (null != mDestFile) {
                    mSummary.setText(String.format(getString(R.string.resource_manager_copy_summary),
                            mDestFile.getName()));
                }
                break;

            case CUT:
                title.setText(R.string.resource_manager_cut);
                mSummary.setVisibility(View.VISIBLE);
                mEdit.setVisibility(View.GONE);
                if (null != mDestFile) {
                    mSummary.setText(String.format(getString(R.string.resource_manager_cut_summary),
                            mDestFile.getName()));
                }
                break;

            default:
                break;
        }

        View cancel = view.findViewById(R.id.cancel);
        View submit = view.findViewById(R.id.submit);
        cancel.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.submit:
                switch (mOperationType) {
                    case DELETE:
                        new DeleteThread().start();
                        break;
                    case RENAME:
                        rename();
                        break;
                    case COPY:
                        new CopyTask().execute();
                        break;
                    case CUT:
                        new CutTask().execute();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    public void setOnCompleteListener(OnCompleteListener listener) {
        mCompleteListener = listener;
    }

    public void setFiles(List<File> files) {
        mFiles = files;
    }

    public void setDestFolder(File folder) {
        mDestFile = folder;

    }


    private void rename() {
        if (TextUtils.isEmpty(mEdit.getText().toString())) {
            Toast.makeText(getActivity(), R.string.resource_manager_empty_name,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // File file = new File(filePath);
        File file = mFiles.get(0);
        File newFile = new File(file.getParentFile(), mEdit
                .getText().toString());
        if (newFile.exists()) {
            Toast.makeText(getActivity(), R.string.resource_manager_file_exist,
                    Toast.LENGTH_SHORT).show();
        } else {
            boolean result = file.renameTo(newFile);
            if (result) {
//                if (null != handler) {
//                    handler.sendEmptyMessage(REFRESH_UI);
//                }
                FileUtil.scanFileAsync(getActivity(), file);
                FileUtil.scanFileAsync(getActivity(), newFile);
                Toast.makeText(getActivity(), R.string.resource_manager_rename_done,
                        Toast.LENGTH_SHORT).show();
            }
            dismiss();
            if (null != mCompleteListener) {
                mCompleteListener.onComplete();
            }
        }
    }

    private void sendMessage(String text) {
        Message message = mHandler.obtainMessage();
        message.what = UPDATE;
        message.obj = text;
        mHandler.sendMessage(message);
    }


    private class DeleteThread extends Thread {
        @Override
        public void run() {
            int count = 0;
            String text = getString(R.string.resource_manager_delete_progress) + count + "/" + mFiles.size();
            sendMessage(text);
            if (null != mFiles) {
                for (int i = 0; i < mFiles.size(); i++) {
                    File file = mFiles.get(i);
                    boolean res = file.delete();
                    count = count + 1;
                    Log.d("zxh", "delete:" + file.getName() + "  " + res + "  count" + count);
                    text = getString(R.string.resource_manager_delete_progress) + count + "/" + mFiles.size();
                    sendMessage(text);
                    FileUtil.scanFileAsync(getActivity(), file);
                }
            }
            mHandler.sendEmptyMessage(DELETE_FINISH);
        }

    }

    public interface OnCompleteListener {
        void onComplete();
    }

    private class CopyTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object... objects) {
            int count = mFiles.size();
            for (int i = 0; i < count; i++) {
                sendMessage(getString(R.string.resource_manager_copy_progress) + (i + 1) + "/" + count);
                File copyFile = new File(mDestFile, System.currentTimeMillis() + "_" + mFiles.get(i).getName());
                try {
                    nioTransferCopy(mFiles.get(i), copyFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            mHandler.sendEmptyMessage(COPY_FINISH);
            return null;
        }

        private void nioTransferCopy(File source, File target) {
            if (null == source || !source.exists() || null == target) {
                return;
            }
            Log.d("zxh", "nioTransferCopy:" + target.getName());
            FileChannel in = null;
            FileChannel out = null;
            FileInputStream inStream = null;
            FileOutputStream outStream = null;
            try {
                inStream = new FileInputStream(source);
                outStream = new FileOutputStream(target);
                in = inStream.getChannel();
                out = outStream.getChannel();
                Log.d("zxh", "nioTransferCopy:11111");
                long size = in.transferTo(0, in.size(), out);
                Log.d("zxh", "nioTransferCopy:" + size);
                FileUtil.scanFileAsync(getActivity(), target);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("zxh", e.toString());
            } finally {
                try {
                    inStream.close();
                    in.close();
                    outStream.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class CutTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object... objects) {
            int count = mFiles.size();
            for (int i = 0; i < count; i++) {
                sendMessage(getString(R.string.resource_manager_cut_progress) + (i + 1) + "/" + count);
                File newFile = new File(mDestFile, mFiles.get(i).getName());
                try {
                    boolean res = mFiles.get(i).renameTo(newFile);
                    FileUtil.scanFileAsync(getActivity(), mFiles.get(i));
                    FileUtil.scanFileAsync(getActivity(), newFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            mHandler.sendEmptyMessage(CUT_FINISH);

            return null;
        }
    }

}
