package com.digitalartsplayground.fantasycrypto.mvvm.requests;

import android.util.Log;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.responses.ApiResponse;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.Resource;


public abstract class CoinDataFetcher<RequestObject> {

    private static final String TAG = "NetworkBoundResource";

    private int callCount = 0;
    private String coinID;
    private AppExecutors appExecutors;
    private MediatorLiveData<Resource<RequestObject>> results = new MediatorLiveData<>();

    public CoinDataFetcher(String coinID, AppExecutors appExecutors) {
        this.coinID = coinID;
        this.appExecutors = appExecutors;
        fetchFromNetwork();
    }

    private void fetchFromNetwork(){

        Log.d(TAG, "CoinDataFetcher fetchFromNetwork: called.");


        results.setValue(Resource.loading(null));

        final LiveData<ApiResponse<RequestObject>> apiResponse = createCall();

        results.addSource(apiResponse, new Observer<ApiResponse<RequestObject>>() {
            @Override
            public void onChanged(ApiResponse<RequestObject> requestObjectApiResponse) {
                results.removeSource(apiResponse);

                if(requestObjectApiResponse  instanceof ApiResponse.ApiSuccessResponse){
                    Log.d(TAG, "onChanged: CoinDataFetcher ApiSuccessResponse.");

                    RequestObject requestObject = processResponse((ApiResponse.ApiSuccessResponse)requestObjectApiResponse);
                    setValue(Resource.success(requestObject));
                }
                else if(requestObjectApiResponse instanceof ApiResponse.ApiEmptyResponse){
                    Log.d(TAG, "onChanged: CoinDataFetcher ApiEmptyResponse");
                    setValue(Resource.loading(null));
                }
                else if(requestObjectApiResponse instanceof ApiResponse.ApiErrorResponse){

                    if(callCount < 3) {
                        fetchFromNetwork();
                        callCount++;
                    } else {
                        setValue(Resource.error(
                                ((ApiResponse.ApiErrorResponse) requestObjectApiResponse).getErrorMessage(),
                                null
                        ));
                    }

                }
            }
        });

    }


    private void setValue(Resource<RequestObject> newValue){
        if(results.getValue() != newValue){
            results.setValue(newValue);
        }
    }

    protected abstract RequestObject processResponse(ApiResponse.ApiSuccessResponse response);


    // Called to create the API call.
    @NonNull @MainThread
    protected abstract LiveData<ApiResponse<RequestObject>> createCall();

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<Resource<RequestObject>> getAsLiveData(){
        return results;
    };
}
