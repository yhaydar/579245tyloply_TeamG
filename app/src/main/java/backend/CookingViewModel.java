package backend;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CookingViewModel extends ViewModel {
    private MutableLiveData<String> instructions;

    public CookingViewModel(){

    }

    public MutableLiveData<String> getInstructions(){
        if(instructions == null){
            instructions = new MutableLiveData<>();
        }
        return instructions;
    }

    public void loadInstructions(String instructions){
       this.instructions.postValue(instructions);
    }
}
