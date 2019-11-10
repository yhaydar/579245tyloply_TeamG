package backend;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CookingViewModel extends ViewModel {
    private MutableLiveData<String> instructions;
    private MutableLiveData<String> finalTemp;
    private MutableLiveData<String> cookingTime;
    private MutableLiveData<String> restTime;
    private MutableLiveData<String> flipTime;

    public CookingViewModel(){

    }

    public MutableLiveData<String> getCookingTime() {
        if(cookingTime == null){
            cookingTime = new MutableLiveData<>();
        }
        return cookingTime;
    }

    public MutableLiveData<String> getInstructions(){
        if(instructions == null){
            instructions = new MutableLiveData<>();
        }
        return instructions;
    }

    public MutableLiveData<String> getFinalTemp(){
        if(finalTemp == null){
            finalTemp = new MutableLiveData<>();
        }
        return finalTemp;
    }

    public MutableLiveData<String> getRestTime(){
        if(restTime == null){
            restTime = new MutableLiveData<>();
        }
        return restTime;
    }

    public MutableLiveData<String> getFlipTime(){
        if(flipTime == null){
            flipTime = new MutableLiveData<>();
        }
        return flipTime;
    }

    public void loadFinalTemp(String finalTemp) { this.finalTemp.postValue(finalTemp); }

    public void loadInstructions(String instructions){ this.instructions.postValue(instructions); }

    public void loadCookingTime(String cookingTime){ this.cookingTime.postValue(cookingTime); }

    public void loadRestTime(String restTime){ this.restTime.postValue(restTime); }

    public void loadFlipTime(String flipTime){ this.flipTime.postValue(flipTime);}
}
