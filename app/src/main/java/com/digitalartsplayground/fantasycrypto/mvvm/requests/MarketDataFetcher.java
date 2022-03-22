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


public abstract class MarketDataFetcher<RequestObject> {

    private static final String TAG = "NetworkBoundResource";

    private int fetchCount = 0;
    private boolean isLoadDatabase;
    private boolean isCacheData;
    private AppExecutors appExecutors;
    private MediatorLiveData<Resource<RequestObject>> results = new MediatorLiveData<>();

    public MarketDataFetcher(AppExecutors appExecutors, boolean isLoadDatabase, boolean isCacheData) {
        this.appExecutors = appExecutors;
        this.isLoadDatabase = isLoadDatabase;
        this.isCacheData = isCacheData;
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
                    Log.d(TAG, "onChanged: ApiSuccessResponse.");

                    fetchCount = 0;

                    if(isLoadDatabase) {

                        appExecutors.diskIO().execute(new Runnable() {
                            @Override
                            public void run() {

                                saveCallResult((RequestObject) processResponse((ApiResponse.ApiSuccessResponse)requestObjectApiResponse));

                                appExecutors.mainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        results.addSource(loadFromDb(), new Observer<RequestObject>() {
                                            @Override
                                            public void onChanged(@Nullable RequestObject cacheObject) {
                                                if(cacheObject != null)
                                                    setValue(Resource.success(cacheObject));
                                            }
                                        });
                                    }
                                });
                            }
                        });

                    } else {

                        appExecutors.diskIO().execute(new Runnable() {
                            @Override
                            public void run() {

                                RequestObject requestObject = (RequestObject) processResponse((ApiResponse.ApiSuccessResponse)requestObjectApiResponse);

                                if(isCacheData)
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


                }
                else if(requestObjectApiResponse instanceof ApiResponse.ApiEmptyResponse){

                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            setValue(Resource.error("Server Error: Empty Response.", null));
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
                            setValue(Resource.error("Error: Unable to connect with server.", null));
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

    // Called to get the cached data from the database.
    @NonNull @MainThread
    protected abstract LiveData<RequestObject> loadFromDb();

    // Called to create the API call.
    @NonNull @MainThread
    protected abstract LiveData<ApiResponse<RequestObject>> createCall();

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<Resource<RequestObject>> getAsLiveData(){
        return results;
    }
}
