package com.iud.nuevaapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseApp.initializeApp(HomeActivity.this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        TextView empty = findViewById(R.id.empty);

        FloatingActionButton add = findViewById(R.id.addNote);

        // Evento click para el botón add
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view1 = LayoutInflater
                        .from(HomeActivity.this)
                        .inflate(R.layout.add_note_dialog, null);

                TextInputLayout titleLayout, contentLayout;
                titleLayout = view1.findViewById(R.id.titleLayout);
                contentLayout = view1.findViewById(R.id.contentLayout);

                TextInputEditText titleEt, contentEt;
                titleEt = view1.findViewById(R.id.titleET);
                contentEt = view1.findViewById(R.id.contentET);

                AlertDialog alertDialog = new AlertDialog
                        .Builder(HomeActivity.this)
                        .setTitle("Add")
                        .setView(view1)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(Objects.requireNonNull(titleEt.getText().toString().isEmpty())) {
                                    titleLayout.setError("This field is required");
                                } else if (Objects.requireNonNull(contentEt.getText().toString().isEmpty())) {
                                    contentLayout.setError("This field is required");
                                } else {
                                    ProgressDialog dialog = new ProgressDialog(HomeActivity.this);
                                    dialog.setMessage("Storing in Database...");
                                    dialog.show();
                                    Note note = new Note();
                                    note.setTitle(titleEt.getText().toString());
                                    note.setContent(contentEt.getText().toString());
                                    database
                                            .getReference()
                                            .child("notes")
                                            .push()
                                            .setValue(note)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    dialog.dismiss();
                                                    dialogInterface.dismiss();
                                                    Toast.makeText(
                                                            HomeActivity.this,
                                                            "Saved Successfully",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    dialog.dismiss();
                                                    dialogInterface.dismiss();
                                                    Toast.makeText(
                                                            HomeActivity.this,
                                                            "There was an error",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                            });
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create();
                alertDialog.show();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler);

        // Evento que escucha actualizacion en DB y actualiza vista
        database
            .getReference()
            .child("notes")
            .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Note> arrayList = new ArrayList<>();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Note note = dataSnapshot.getValue(Note.class);
                    Objects.requireNonNull(note).setKey(dataSnapshot.getKey());
                    arrayList.add(note);
                }

                if(arrayList.isEmpty()) {
                    empty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    empty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                NoteAdapter adapter = new NoteAdapter(HomeActivity.this, arrayList);
                recyclerView.setAdapter(adapter);

                // Actualizamos el adapter
                adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(Note note) {

                        View view = LayoutInflater
                                .from(HomeActivity.this)
                                .inflate(R.layout.add_note_dialog, null);

                        TextInputEditText titleEt, contentEt;
                        TextInputLayout titleLayout, contentLayout;

                        titleEt = view.findViewById(R.id.titleET);
                        contentEt = view.findViewById(R.id.contentET);

                        titleEt.setText(note.getTitle());
                        contentEt.setText(note.getContent());

                        titleLayout = view.findViewById(R.id.titleLayout);
                        contentLayout = view.findViewById(R.id.contentLayout);

                        ProgressDialog progressDialog = new ProgressDialog(HomeActivity.this);

                        AlertDialog alertDialog = new AlertDialog
                            .Builder(HomeActivity.this)
                            .setTitle("Exit")
                            .setView(view)
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if(Objects.requireNonNull(titleEt.getText().toString().isEmpty())) {
                                        titleLayout.setError("This field is required");
                                    } else if (Objects.requireNonNull(contentEt.getText().toString().isEmpty())) {
                                        contentLayout.setError("This field is required");
                                    } else {
                                        progressDialog.setMessage("Saving...");
                                        progressDialog.show();
                                        Note note1 = new Note();
                                        note1.setTitle(titleEt.getText().toString());
                                        note1.setContent(contentEt.getText().toString());
                                        database
                                            .getReference()
                                            .child("notes")
                                            .child(note.getKey())
                                            .setValue(note1)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    progressDialog.dismiss();
                                                    dialogInterface.dismiss();
                                                    Toast.makeText(
                                                            HomeActivity.this,
                                                            "Saved Successfully",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    dialogInterface.dismiss();
                                                    Toast.makeText(
                                                            HomeActivity.this,
                                                            "There was an error",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                            });
                                    }
                                }
                            })
                            .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    progressDialog.setTitle("Deleting...");
                                    progressDialog.show();
                                    database
                                        .getReference()
                                        .child("notes")
                                        .child(note.getKey())
                                        .removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(
                                                        HomeActivity.this,
                                                        "Deleted Successfull",
                                                        Toast.LENGTH_SHORT)
                                                        .show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                            }
                                        });
                                }
                            })
                            .create();
                        alertDialog.show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}