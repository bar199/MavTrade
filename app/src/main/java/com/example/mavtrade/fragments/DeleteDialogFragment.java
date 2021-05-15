package com.example.mavtrade.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mavtrade.Following;
import com.example.mavtrade.Post;
import com.example.mavtrade.R;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import static com.parse.Parse.getApplicationContext;

public class DeleteDialogFragment extends DialogFragment {

    public static final String TAG = "DeletePostDialogFM";

    private Dialog dialog;
    private String objectId;
    private String title;
    private String message;

    private TextView tvDialogTitle;
    private TextView tvDialogMessage;
    private Button btnCancel;
    private Button btnConfirmDelete;

    // 0 : delete user account
    // 1 : delete post
    private Boolean deleteType;

    public DeleteDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static DeleteDialogFragment newInstance(String objectId, String title, String message) {
        DeleteDialogFragment fragment = new DeleteDialogFragment();
        Bundle args = new Bundle();

        args.putString("objectId", objectId);
        args.putString("title", title);
        args.putString("message", message);

        fragment.setArguments(args);
        return fragment;
    }

    // Defines the listener interface
    public interface DeleteDialogListener {
        void onFinishDeleteDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delete_dialog, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dialog = getDialog();

        objectId = getArguments().getString("objectId");
        title = getArguments().getString("title");
        message = getArguments().getString("message");

        tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        tvDialogMessage = view.findViewById(R.id.tvDialogMessage);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnConfirmDelete = view.findViewById(R.id.btnConfirmDelete);

        tvDialogTitle.setText(title);
        tvDialogMessage.setText(message);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });

        btnConfirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null && dialog.isShowing()) {
                    DeleteDialogListener listener = (DeleteDialogListener) getTargetFragment();
                    listener.onFinishDeleteDialog();
                    dialog.dismiss();
                }
            }
        });
    }

    /*if (deleteType) {
                        deletePost();
                    } else {
                        deleteUser();
                    }*/

    // Delete User object
    private void deleteUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {
            currentUser.deleteInBackground(e -> {
                if (e != null) {
                    Log.e(TAG, "Issue deleting currentUser");
                }

                DeleteDialogListener listener = (DeleteDialogListener) getTargetFragment();
                listener.onFinishDeleteDialog();
                dialog.dismiss();
            });
        }
    }

}