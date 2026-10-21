package com.example.android.mymap.models;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.mymap.MapsActivity;
import com.example.android.mymap.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;


public class SearchDialog extends AppCompatDialogFragment {
    private static final String TAG = "SearchDialog";
    private EditText distance;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.search_dialog_layout, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle("Search");
        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(distance.getText())) {
                    Toast.makeText(getActivity(), "Enter the radius", Toast.LENGTH_LONG).show();
                } else if (Integer.parseInt(distance.getText().toString()) < 100 || Integer.parseInt(distance.getText().toString()) > 10000) {
                    Toast.makeText(getActivity(), "Radius should be between 100-10000", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onClick: distance = " + distance.getText().toString());
                    Intent intent = new Intent(getActivity(), MapsActivity.class);
                    intent.putExtra("distance", distance.getText().toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        distance = view.findViewById(R.id.search_dialog_layout_distance);
        return builder.create();
    }
}
