package com.example.mavtrade.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mavtrade.Post;
import com.example.mavtrade.R;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import static android.app.Activity.RESULT_OK;
import static com.parse.Parse.getApplicationContext;

public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public final static int PICK_PHOTO_CODE = 43;

    private EditText etPrice;
    private EditText etTitle;
    private EditText etDescription;
    private ImageView ivPostImage;
    private Button btnCaptureImage;
    private Button btnLoadImage;
    private Button btnPost;

    private ParseUser currentUser = ParseUser.getCurrentUser();
    private String title;
    private String description;
    private String price;
    private Long numPrice;
    private File photoFile;
    public String photoFileName = "photo.jpg";

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etPrice = view.findViewById(R.id.etPrice);
        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        ivPostImage = view.findViewById(R.id.ivPostImage);
        btnCaptureImage = view.findViewById(R.id.btnCaptureImage);
        btnLoadImage = view.findViewById(R.id.btnLoadImage);
        btnPost = view.findViewById(R.id.btnPost);

        etTitle.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        etPrice.addTextChangedListener(onTextChangedListener());

        btnCaptureImage.setOnClickListener(v -> {
            launchCamera();
        });

        btnLoadImage.setOnClickListener(v -> {
            onPickPhoto();
        });

        btnPost.setOnClickListener(v -> {
            title = etTitle.getText().toString();
            description = etDescription.getText().toString();
            price = etPrice.getText().toString().replaceAll("[$,.]", "");

            if (title.isEmpty()) {
                displayToast("Title cannot be empty");
                return;
            }

            if (description.isEmpty()) {
                displayToast("Description cannot be empty");
                return;
            }

            if (price.isEmpty()) {
                displayToast("Price cannot be empty");
                return;
            }

            if(photoFile == null || ivPostImage.getDrawable() == null) {
                displayToast("There is no image!");
                return;
            }

            numPrice = Long.parseLong(price.replaceAll("[^\\d]", ""));
            savePostImage();
        });
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider.mavtrade", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
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
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivPostImage.setImageBitmap(takenImage);
            } else { // Result was a failure
                displayToast("Picture wasn't taken!");
            }
        } else if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();
            photoFile = getPhotoFileUri(photoFileName);

            InputStream in = null;
            OutputStream out = null;

            try {
                in = getActivity().getContentResolver().openInputStream(photoUri);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "", e);
            }

            try {
                out = new FileOutputStream(getPhotoFileUri(photoFileName));
            } catch (FileNotFoundException e) {
                Log.e(TAG, "", e);
            }

            if (in != null && out != null)
            {
                byte[] buf = new byte[1024];
                int len;

                try {
                    if (in != null) {
                        while ((len=in.read(buf)) > 0) {
                            out.write(buf,0,len);
                        }
                    }
                        out.close();
                        in.close();

                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
            }

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);

            // Load the selected image into a preview
            ivPostImage.setImageBitmap(selectedImage);
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

    private TextWatcher onTextChangedListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Long price;
                Integer intLength;
                String priceFormat = "";

                etPrice.removeTextChangedListener(this);

                try {
                    String original = s.toString().replaceAll("[$,.]", "");

                    if (original.matches("-?\\d+")) {
                        price = Long.parseLong(original.replaceAll("[^\\d]", ""));

                        intLength = countDigit(price);

                        if (intLength >= 3) {
                            Long dollars = price / 100;
                            Long cents = price % 100;

                            DecimalFormat decimalFormat = new DecimalFormat("#.##");
                            decimalFormat.setGroupingUsed(true);
                            decimalFormat.setGroupingSize(3);

                            priceFormat = "$" + decimalFormat.format(dollars) + "." + ((cents < 10) ? "0" : "") + cents;
                        } else {
                            switch (intLength) {
                                case 0:
                                    break;

                                case 1:
                                    priceFormat = "$0.0" + price;
                                    break;

                                case 2:
                                default:
                                    priceFormat = "$0." + price;
                            }
                        }
                    }

                    etPrice.setText(priceFormat);
                    etPrice.setSelection(etPrice.getText().length());
                    etPrice.addTextChangedListener(this);

                } catch (NumberFormatException e) {
                    Log.e(TAG, "Issue formatting price", e);
                }
            }
        };
    }

    private int countDigit(Long number) {
        int count = 0;
        while (number != 0)
        {
            number /= 10;
            ++count;
        }
        return count;
    }

    private void savePostImage() {
        ParseFile postImage = new ParseFile(photoFile);
        postImage.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving post image", e);
                }

                savePost(postImage);
            }
        });
    }

    private void savePost(ParseFile postImage) {
        Post post = new Post();
        post.setTitle(title);
        post.setDescription(description);
        post.setPrice(numPrice);
        post.setImage(postImage);
        post.setUser(currentUser);

        post.saveInBackground(e -> {
            if (e != null) {
                displayToast("Error while saving!");
                Log.e(TAG, "Error while saving", e);
                return;
            }

            Log.i(TAG, "Post was saved successfully!");
            etTitle.setText("");
            etPrice.setText("");
            etDescription.setText("");
            ivPostImage.setImageResource(0);
            displayToast("Post successfully saved!");
        });
    }
}