package backend;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CookingViewModel extends ViewModel {
    private MutableLiveData<String> instructions;
    private MutableLiveData<String> finalTemp;
    private MutableLiveData<String> ECT;
    private MutableLiveData<String> restTime;

    public CookingViewModel(){

    }

    public MutableLiveData<String> getECT() {
        if(ECT == null){
            ECT = new MutableLiveData<>();
        }
        return ECT;
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

    public void loadFinalTemp(String finalTemp) { this.finalTemp.postValue(finalTemp); }

    public void loadInstructions(String instructions){ this.instructions.postValue(instructions); }

    public void loadECT(String ECT){ this.ECT.postValue(ECT); }

    public void loadRestTime(String restTime){ this.restTime.postValue(restTime); }
}
