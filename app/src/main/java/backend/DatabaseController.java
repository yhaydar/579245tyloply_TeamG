package backend;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseController {
    private FirebaseDatabase database;
    DatabaseReference reference;

    public DatabaseController(String ref){
       database = FirebaseDatabase.getInstance();
       reference = database.getReference(ref);
    }

    public void setDatabaseRef(String ref){
        reference = database.getReference(ref);
    }

    public void writeToDB(String ref, String message){
        setDatabaseRef(ref);
        reference.setValue(message);

        //reference.addValueEventListener(new Value);

    }

    public void readFromDB(){
        reference.child("Poultry");
    }
}
