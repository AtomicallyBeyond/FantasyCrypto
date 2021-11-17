package com.digitalartsplayground.fantasycrypto.mvvm;

import android.util.Log;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.digitalartsplayground.fantasycrypto.models.CandleStickData;
import com.digitalartsplayground.fantasycrypto.mvvm.requests.responses.ApiResponse;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.Resource;



public abstract class CoinDataFetcher {

    private static final String TAG = "NetworkBoundResource";

    private AppExecutors appExecutors;
    private MediatorLiveData<Resource<CandleStickData>> results = new MediatorLiveData<>();

    public CoinDataFetcher(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        fetchFromNetwork();
    }

    private void fetchFromNetwork(){

        Log.d(TAG, "CoinDataFetcher fetchFromNetwork: called.");


        results.setValue(Resource.loading(null));

        final LiveData<ApiResponse<CandleStickData>> apiResponse = createCall();

        results.addSource(apiResponse, new Observer<ApiResponse<CandleStickData>>() {
            @Override
            public void onChanged(ApiResponse<CandleStickData> candleStickDataApiResponse) {
                results.removeSource(apiResponse);

                if(candleStickDataApiResponse  instanceof ApiResponse.ApiSuccessResponse){
                    Log.d(TAG, "onChanged: CoinDataFetcher ApiSuccessResponse.");

                    CandleStickData data = processResponse((ApiResponse.ApiSuccessResponse)candleStickDataApiResponse);
                    setValue(Resource.success(data));
                }
                else if(candleStickDataApiResponse instanceof ApiResponse.ApiEmptyResponse){
                    Log.d(TAG, "onChanged: CoinDataFetcher ApiEmptyResponse");
                    setValue(Resource.loading(null));
                }
                else if(candleStickDataApiResponse instanceof ApiResponse.ApiErrorResponse){
                    setValue(Resource.error(
                            ((ApiResponse.ApiErrorResponse) candleStickDataApiResponse).getErrorMessage(),
                            null
                    ));

                    fetchFromNetwork();
                }
            }
        });
    }

    private CandleStickData processResponse(ApiResponse.ApiSuccessResponse response){
        return (CandleStickData) response.getBody();
    }

    private void setValue(Resource<CandleStickData> newValue){
        if(results.getValue() != newValue){
            results.setValue(newValue);
        }
    }


    // Called to create the API call.
    @NonNull @MainThread
    protected abstract LiveData<ApiResponse<CandleStickData>> createCall();

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<Resource<CandleStickData>> getAsLiveData(){
        return results;
    };
}
