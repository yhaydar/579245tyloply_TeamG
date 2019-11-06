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

    public void readInstructionsFromDB(final String meatType, final String meatCut,final CookingViewModel model){
        //final String instructions = new String();

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
                            }
                            Log.d("DEBUG", document.getId() + document.getData());
                        }
                    } else {
                        Log.d("DEBUG", "Error reading from the database: " + task.getException());
                    }
                }
            });
        //return meatInformation.getInstructions();
    }
}