package backend;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;

public class DatabaseController implements Serializable {
    private FirebaseFirestore database;

    public DatabaseController() {
        database = FirebaseFirestore.getInstance();
    }

    public void readInstructionsFromDB(final String meatType, final String meatCut, final CookingViewModel model){
        database.collection(meatType)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document :task.getResult()){
                                Log.d("DEBUG", meatCut);
                                if(document.getId().equals(meatCut)){
                                    Log.d("DEBUG", "Matched: " + document.getId());
                                    String dbInstructions = document.getData().get("Instructions").toString();
                                    model.loadInstructions(dbInstructions);
                                    Log.d("DEBUG", "Value: " + document.getData().get("Instructions").toString());
                                    break;
                                }
                                Log.d("DEBUG", document.getId() + document.getData());
                            }
                        } else {
                            Log.d("DEBUG", "Error reading from the database: " + task.getException());
                        }
                    }
                });
    }

    public void readFinalTempFromDB(final String meatType, final String meatCut, final String doneness, final CookingViewModel model){
        database.collection(meatType)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document: task.getResult()){
                                if(document.getId().equals(meatCut)){
                                    Log.d("DEBUG", doneness + " is the doneness");
                                    String dbtemp = document.getData().get(doneness).toString();
                                    model.loadFinalTemp(dbtemp);
                                    Log.d("DEBUG", "Value: " + document.getData().get("Instructions").toString());
                                    break;
                                }
                            }
                        } else{
                            Log.d("DEBUG", "Error reading from the database: " + task.getException());
                            //Toast.makeText(CookingActivity.class, "Error reading from the database: " + task.getException(), Toast.LENGTH_LONG);
                        }
                    }
                });
    }

    public void readCookingTimeFromDB(final String meatType, final String meatCut, final CookingViewModel model){
        database.collection(meatType)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document :task.getResult()){
                                Log.d("DEBUG", meatCut);
                                if(document.getId().equals(meatCut)){
                                    Log.d("DEBUG", "Matched: " + document.getId());
                                    String dbCookingTime = document.getData().get("ECT").toString();
                                    model.loadCookingTime(dbCookingTime);
                                    Log.d("DEBUG", "Value: " + dbCookingTime);
                                    break;
                                }
                                Log.d("DEBUG", document.getId() + document.getData());
                            }
                        } else {
                            Log.d("DEBUG", "Error reading from the database: " + task.getException());
                        }
                    }
                });
    }

    public void readRestTimeFromDB(final String meatType, final String meatCut, final CookingViewModel model){
        database.collection(meatType)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document :task.getResult()){
                                Log.d("DEBUG", meatCut);
                                if(document.getId().equals(meatCut)){
                                    Log.d("DEBUG", "Matched: " + document.getId());
                                    String dbRestTime = document.getData().get("ERT").toString();
                                    model.loadRestTime(dbRestTime);
                                    Log.d("DEBUG", "Value: " + document.getData().get("ERT").toString());
                                    break;
                                }
                                Log.d("DEBUG", document.getId() + document.getData());
                            }
                        } else {
                            Log.d("DEBUG", "Error reading from the database: " + task.getException());
                        }
                    }
                });
    }

    public void readFlippingTimeFromDB(final String meatType, final String meatCut, final CookingViewModel model){
        database.collection(meatType)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document :task.getResult()){
                                Log.d("DEBUG", meatCut);
                                if(document.getId().equals(meatCut)){
                                    Log.d("DEBUG", "Matched: " + document.getId());
                                    String dbFlipTime = document.getData().get("FlipTime").toString();
                                    model.loadFlipTime(dbFlipTime);
                                    Log.d("DEBUG", "Value: " + document.getData().get("FlipTime").toString());
                                    break;
                                }
                                Log.d("DEBUG", document.getId() + document.getData());
                            }
                        } else {
                            Log.d("DEBUG", "Error reading from the database: " + task.getException());
                        }
                    }
                });
    }
}