package com.example.mavtrade.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.target.Target;
import com.example.mavtrade.LoginActivity;
import com.example.mavtrade.R;
import com.example.mavtrade.UserInfo;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.parse.Parse.getApplicationContext;


public class SettingsFragment extends Fragment {
    public static final String TAG = "SettingsFragment";
    public final static int PICK_PHOTO_CODE = 43;

    private Button btnSave;
    private ImageView ivUserImage;
    private Button btnEditPhoto;
    private EditText etSettingsUsername;
    private EditText etSettingsEmail;
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnDeleteAccount;
    private Button btnLogout;

    private Boolean goodUserName = true;
    private Boolean goodEmail = true;
    private Boolean goodCurrentPassword = true;
    private Boolean goodNewPassword = true;

    private String userInfoId;
    private Boolean photoUpdated;

    private String userName;
    private String userEmail;
    private String password;
    private Boolean userNameUpdated = false;
    private Boolean userEmailUpdated = false;
    private Boolean passwordUpdated = false;

    private Boolean infoUpdated = false;

    private File photoFile;
    public String photoFileName = "photo.jpg";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSave = getView().findViewById(R.id.btnSave);
        ivUserImage = getView().findViewById(R.id.ivUserImage);
        btnEditPhoto = getView().findViewById(R.id.btnEditPhoto);
        etSettingsUsername = getView().findViewById(R.id.etSettingsUsername);
        etSettingsEmail = getView().findViewById(R.id.etSettingsEmail);
        etCurrentPassword = getView().findViewById(R.id.etCurrentPassword);
        etNewPassword = getView().findViewById(R.id.etNewPassword);
        etConfirmPassword = getView().findViewById(R.id.etConfirmPassword);
        btnDeleteAccount = getView().findViewById(R.id.btnDeleteAccount);
        btnLogout = getView().findViewById(R.id.btnLogout);

        queryUserImage();
        userName = ParseUser.getCurrentUser().getUsername();
        userEmail = ParseUser.getCurrentUser().getEmail();

        // Initialize username and email to database values
        etSettingsUsername.setText(userName);
        etSettingsEmail.setText(userEmail);

        etSettingsUsername.addTextChangedListener(onUsernameChangedListener());
        etSettingsEmail.addTextChangedListener(onEmailChangedListener());
        etCurrentPassword.addTextChangedListener(onCurrentPasswordChangedListener());
        etNewPassword.addTextChangedListener(onNewPasswordChangedListener());
        etConfirmPassword.addTextChangedListener(onConfirmPasswordChangedListener());

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (goodUserName && goodEmail && goodCurrentPassword && goodNewPassword) {
                    // Update photo if photo was changed
                    if (photoUpdated) {
                        updateUserImage();
                        infoUpdated = true;
                    }

                    // Update user information if information was changed
                    if (userNameUpdated || userEmailUpdated || passwordUpdated) {
                        updateUserInfo();
                        infoUpdated = true;
                    }

                    if (infoUpdated) {
                        displayToast("Saved changes!");
                        infoUpdated = false;
                    }

                } else {
                    displayToast("Cannot save with errors. Please fix and try again.");
                }
            }
        });

        btnEditPhoto.setOnClickListener(v -> onPickPhoto());

        /*btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/

        btnLogout.setOnClickListener(v -> {
            ParseUser.logOut();
            ParseUser currentUser = ParseUser.getCurrentUser();
            if(currentUser == null) {
                goLoginActivity();
            } else {
                displayToast("Error logging out");
            }
        });
    }

    private void queryUserImage() {
        ParseQuery<UserInfo> query = ParseQuery.getQuery(UserInfo.class);
        query.include(UserInfo.KEY_USER);
        query.whereEqualTo(UserInfo.KEY_USER, ParseUser.getCurrentUser());

        query.getFirstInBackground(new GetCallback<UserInfo>() {
            @Override
            public void done(UserInfo userProfile, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with profile image", e);
                    return;
                }

                // Save user's information id
                userInfoId = userProfile.getObjectId();

                ParseFile profileImage = userProfile.getProfileImage();
                if (profileImage != null) {
                    Glide.with(getContext()).load(profileImage.getUrl())
                            .circleCrop()
                            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .into(ivUserImage);

                    // Image is has not been changed
                    photoUpdated = false;
                }
            }
        });
    }

    private TextWatcher onUsernameChangedListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().trim().isEmpty()) {
                    goodUserName = false;
                    etSettingsUsername.setError("This field cannot be blank");

                } else {
                    goodUserName = true;
                    String newUsername = s.toString().trim();
                    userNameUpdated = !newUsername.equals(userName);

                    if (userNameUpdated) {
                        userName = newUsername;
                    }
                }
            }
        };
    }

    private TextWatcher onEmailChangedListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().trim().isEmpty()) {
                    goodEmail = false;
                    etSettingsEmail.setError("This field cannot be blank");

                } else {
                    String newEmail = s.toString().trim();
                    Boolean isValidEmail = (Patterns.EMAIL_ADDRESS.matcher(newEmail).matches());
                    /*String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                    if (newEmail.matches(EMAIL_PATTERN)) {
                        goodEmail = true;
                        userEmailUpdated = !newEmail.equals(userEmail);

                    } else {
                        goodEmail = false;
                        etSettingsEmail.setError("Not a valid email address");
                    }*/

                    if (isValidEmail) {
                        goodEmail = true;
                        userEmailUpdated = !newEmail.equals(userEmail);

                        if (userEmailUpdated) {
                            userEmail = newEmail;
                        }

                    } else {
                        goodEmail = false;
                        etSettingsEmail.setError("Not a valid email address");
                    }
                }
            }
        };
    }

    private TextWatcher onCurrentPasswordChangedListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) {
                    etCurrentPassword.setError(null);

                    String currentPassword = s.toString();

                    ParseUser.logInInBackground(ParseUser.getCurrentUser().getUsername(), currentPassword, (user, e) -> {
                        if (user != null) {
                            // Correct password
                            goodCurrentPassword = true;
                        } else {
                            // Incorrect password
                            goodCurrentPassword = false;
                            etCurrentPassword.setError("Incorrect password");
                        }
                    });
                }
            }
        };
    }

    private TextWatcher onNewPasswordChangedListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {

                String newPassword = s.toString();
                String confirmPassword = etConfirmPassword.getText().toString();
                String currentPassword = etCurrentPassword.getText().toString();

                if (confirmPassword.isEmpty() && newPassword.isEmpty()) {
                    passwordUpdated = false;
                    goodNewPassword = true;

                    etCurrentPassword.setError(null);
                    etNewPassword.setError(null);
                    etConfirmPassword.setError(null);

                } else if (!s.toString().isEmpty()) {

                    if (currentPassword.isEmpty()) {
                        goodCurrentPassword = false;
                        etCurrentPassword.setError("This field cannot be blank");
                    }

                    etNewPassword.setError(null);

                    if (newPassword.equals(confirmPassword)) {
                        goodNewPassword = true;

                    } else {
                        goodNewPassword = false;
                        etConfirmPassword.setError("Does not match new password");
                    }

                }
            }
        };
    }

    private TextWatcher onConfirmPasswordChangedListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {

                String confirmPassword = s.toString();
                String newPassword = etNewPassword.getText().toString();
                String currentPassword = etCurrentPassword.getText().toString();

                if (confirmPassword.isEmpty() && newPassword.isEmpty()) {
                    passwordUpdated = false;
                    goodNewPassword = true;

                    etCurrentPassword.setError(null);
                    etNewPassword.setError(null);
                    etConfirmPassword.setError(null);

                } else if (!confirmPassword.isEmpty() && newPassword.isEmpty()) {
                    if (currentPassword.isEmpty()) {
                        goodCurrentPassword = false;
                        etCurrentPassword.setError("This field cannot be blank");
                    }

                    goodNewPassword = false;
                    etNewPassword.setError("This field cannot be blank");

                } else if (!confirmPassword.isEmpty() && !newPassword.isEmpty()) {
                    if (currentPassword.isEmpty()) {
                        goodCurrentPassword = false;
                        etCurrentPassword.setError("This field cannot be blank");
                    }

                    etNewPassword.setError(null);

                    if (confirmPassword.equals(newPassword)) {
                        password = confirmPassword;
                        goodNewPassword = true;
                        passwordUpdated = true;

                    } else {
                        goodNewPassword = false;
                        etConfirmPassword.setError("Does not match new password");
                    }
                }
            }
        };
    }

    private void updateUserImage() {
        ParseFile postImage = new ParseFile(photoFile);
        ParseQuery<UserInfo> query = ParseQuery.getQuery(UserInfo.class);

        query.getInBackground(userInfoId, new GetCallback<UserInfo>() {
            @Override
            public void done(UserInfo newImage, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue saving profile image", e);
                    return;
                }

                newImage.setProfileImage(postImage);

                newImage.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Issue saving new user image");
                        }
                    }
                });
            }
        });
    }

    private void updateUserInfo() {
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {
            currentUser.setUsername(userName);
            currentUser.setEmail(userEmail);

            if (passwordUpdated) {
                currentUser.setPassword(password);
            }

            currentUser.saveInBackground(e -> {
                if (e != null) {
                    Log.e(TAG, "Issue saving user information", e);
                }
            });
        }
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto() {
        // Create intent for picking a photo from the gallery
        // Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PHOTO_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();
            photoFile = getPhotoFileUri(photoFileName);

            if (photoFile != null) {
                Glide.with(getContext()).load(photoFile)
                        .circleCrop()
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .into(ivUserImage);

                // Image has been changed
                photoUpdated = true;
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "Failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getActivity().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to create directory", e);
        }
        return image;
    }

    private void goLoginActivity() {
        Intent i = new Intent(getContext(), LoginActivity.class);
        startActivity(i);
        getActivity().finish();
    }

    private void displayToast(String message) {
        // Inflate toast XML layout
        View layout = getLayoutInflater().inflate(R.layout.toast,
                (ViewGroup) getActivity().findViewById(R.id.toast_layout_root));

        // Fill in the message into the textview
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(message);

        // Construct the toast, set the view and display
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}