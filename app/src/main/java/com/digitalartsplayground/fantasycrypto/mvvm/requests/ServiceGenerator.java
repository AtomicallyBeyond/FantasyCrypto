package com.digitalartsplayground.fantasycrypto.mvvm.requests;

import com.digitalartsplayground.fantasycrypto.util.Constants;
import com.digitalartsplayground.fantasycrypto.util.LiveDataCallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    private static OkHttpClient client = new OkHttpClient.Builder()

            // establish connection to server
            .connectTimeout(Constants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)

            // time between each byte read from the server
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)

            // time between each byte sent to server
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)

            .retryOnConnectionFailure(true)

            .build();


    private static Retrofit.Builder retrofitBuilder =
            new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = retrofitBuilder.build();

    private static CoinGeckoApi cryptoApi = retrofit.create(CoinGeckoApi.class);

    public static CoinGeckoApi getCryptoApi(){
        return cryptoApi;
    }
}
