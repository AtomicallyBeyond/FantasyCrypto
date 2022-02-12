package com.digitalartsplayground.fantasycrypto.mvvm.requests;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.responses.ApiResponse;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.Resource;


public abstract class MarketDataLoader<RequestObject> {


    private int fetchCount = 0;
    private AppExecutors appExecutors;
    private MediatorLiveData<Resource<RequestObject>> results = new MediatorLiveData<>();

    public MarketDataLoader(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        results.setValue(Resource.loading(null));
        fetchFromNetwork();
    }

    private void fetchFromNetwork(){

        final LiveData<ApiResponse<RequestObject>> apiResponse = createCall();

        results.addSource(apiResponse, new Observer<ApiResponse<RequestObject>>() {
            @Override
            public void onChanged(@Nullable final ApiResponse<RequestObject> requestObjectApiResponse) {
                results.removeSource(apiResponse);

                if(requestObjectApiResponse instanceof ApiResponse.ApiSuccessResponse){

                    fetchCount = 0;

                    appExecutors.diskIO().execute(new Runnable() {
                        @Override
                        public void run() {

                            RequestObject requestObject = (RequestObject) processResponse((ApiResponse.ApiSuccessResponse)requestObjectApiResponse);

                            saveCallResult(requestObject);

                            appExecutors.mainThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    setValue(Resource.success(requestObject));
                                }
                            });
                        }
                    });
                }
                else if(requestObjectApiResponse instanceof ApiResponse.ApiEmptyResponse){

                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            setValue(Resource.error("Error: Unable to connect with server, check internet connection.", null));
                        }
                    });
                }
                else if(requestObjectApiResponse instanceof ApiResponse.ApiErrorResponse){

                    if(fetchCount < 2) {
                        fetchFromNetwork();
                        fetchCount++;
                        return;
                    }

                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            setValue(Resource.error("Error: Unable to connect with server, check internet connection.", null));
                        }
                    });


                }
            }
        });
    }

    private RequestObject processResponse(ApiResponse.ApiSuccessResponse response){
        return (RequestObject) response.getBody();
    }

    private void setValue(Resource<RequestObject> newValue){
        if(results.getValue() != newValue){
            results.setValue(newValue);
        }
    }

    // Called to save the result of the API response into the database.
    @WorkerThread
    protected abstract void saveCallResult(@NonNull RequestObject item);


    // Called to create the API call.
    @NonNull @MainThread
    protected abstract LiveData<ApiResponse<RequestObject>> createCall();

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<Resource<RequestObject>> getAsLiveData(){
        return results;
    }
}
