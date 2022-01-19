package com.digitalartsplayground.fantasycrypto.mvvm.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.digitalartsplayground.fantasycrypto.models.LineGraphData;
import com.digitalartsplayground.fantasycrypto.mvvm.Repository;
import com.digitalartsplayground.fantasycrypto.util.Resource;
import org.jetbrains.annotations.NotNull;


public class LineChartFragmentViewModel extends AndroidViewModel {

    private long currentTimeStamp;
    private long currentTimeSeconds;
    public final long MILLIS_IN_DAY = 24 * 60 * 60 * 1000;
    public final long oneDayAgo;
    public final long sevenDaysAgo;
    public final long oneMonthAgo;
    public final long threeMonthsAgo;
    public final long oneYearAgo;
    public final long oneDayAgoSeconds;
    public final long threeMonthsAgoSeconds;
    public final long oneYearAgoSeconds;

    private Repository repository;
    private MediatorLiveData<Resource<LineGraphData>> liveDay = new MediatorLiveData<>();
    private MediatorLiveData<Resource<LineGraphData>> liveThreeMonths = new MediatorLiveData<>();
    private MediatorLiveData<Resource<LineGraphData>> liveYear = new MediatorLiveData<>();
    private MutableLiveData<LineGraphData.TimeSpan> liveTimeSpan = new MutableLiveData<>();

    public LineChartFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);

        repository = Repository.getInstance(application);

        currentTimeStamp = System.currentTimeMillis();

        currentTimeSeconds = currentTimeStamp / 1000;

        oneDayAgo = currentTimeStamp - MILLIS_IN_DAY;
        sevenDaysAgo = currentTimeStamp - MILLIS_IN_DAY * 7;
        oneMonthAgo = currentTimeStamp - MILLIS_IN_DAY * 30;
        threeMonthsAgo = currentTimeStamp - MILLIS_IN_DAY * 90;
        oneYearAgo = currentTimeStamp - MILLIS_IN_DAY * 365;

        oneDayAgoSeconds = oneDayAgo / 1000;
        threeMonthsAgoSeconds = threeMonthsAgo / 1000;
        oneYearAgoSeconds = oneYearAgo / 1000;

        liveTimeSpan.setValue(LineGraphData.TimeSpan.DAY);
    }

    public long getTimeSpanLong(LineGraphData.TimeSpan timeSpan) {

        switch (timeSpan) {
            case DAY:
                return oneDayAgo;
            case SEVENDAY:
                return sevenDaysAgo;
            case MONTH:
                return oneMonthAgo;
            case THREEMONTH:
                return threeMonthsAgo;
            default:
                return oneYearAgo;
        }
    }

    public MediatorLiveData<Resource<LineGraphData>> getLiveDay() {
        return liveDay;
    }

    public MediatorLiveData<Resource<LineGraphData>> getLiveThreeMonths() {
        return liveThreeMonths;
    }

    public MediatorLiveData<Resource<LineGraphData>> getLiveYear() {
        return liveYear;
    }

    public void fetchDay(String coinID) {

        LiveData<Resource<LineGraphData>> liveData = repository.getLineGraphData(
                coinID,
                "usd",
                String.valueOf(oneDayAgoSeconds),
                String.valueOf(currentTimeSeconds),
                LineGraphData.TimeSpan.DAY);

        liveDay.addSource(liveData, new Observer<Resource<LineGraphData>>() {
            @Override
            public void onChanged(Resource<LineGraphData> lineGraphDataResource) {

                if(lineGraphDataResource.status == Resource.Status.SUCCESS)
                    liveDay.removeSource(liveData);

                liveDay.setValue(lineGraphDataResource);
            }
        });
    }

    public void fetchThreeMonth(String coinID) {

        LiveData<Resource<LineGraphData>> liveData = repository.getLineGraphData(
                coinID,
                "usd",
                String.valueOf(threeMonthsAgoSeconds),
                String.valueOf(currentTimeSeconds),
                LineGraphData.TimeSpan.THREEMONTH);

        liveThreeMonths.addSource(liveData, new Observer<Resource<LineGraphData>>() {
            @Override
            public void onChanged(Resource<LineGraphData> lineGraphDataResource) {

                if(lineGraphDataResource.status == Resource.Status.SUCCESS)
                    liveThreeMonths.removeSource(liveData);

                liveThreeMonths.setValue(lineGraphDataResource);
            }
        });
    }


    public void fetchOneYear(String coinID) {

        LiveData<Resource<LineGraphData>> liveData = repository.getLineGraphData(
                coinID,
                "usd",
                String.valueOf(oneYearAgoSeconds),
                String.valueOf(currentTimeSeconds),
                LineGraphData.TimeSpan.ONEYEAR);

        liveYear.addSource(liveData, new Observer<Resource<LineGraphData>>() {
            @Override
            public void onChanged(Resource<LineGraphData> lineGraphDataResource) {

                if(lineGraphDataResource.status == Resource.Status.SUCCESS)
                    liveYear.removeSource(liveData);

                liveYear.setValue(lineGraphDataResource);
            }
        });
    }

    public LiveData<LineGraphData.TimeSpan> getLiveTimeSpan() {
        return liveTimeSpan;
    }

    public void setLiveTimeSpan(LineGraphData.TimeSpan timeSpan) {
        liveTimeSpan.setValue(timeSpan);
    }
}


/*        String startTime;

        switch (timeSpan) {
            case DAY:
                startTime = String.valueOf(oneDayAgo);
                break;
            case SEVENDAY:
                startTime = String.valueOf(sevenDaysAgo);
                break;
            case MONTH:
                startTime = String.valueOf(oneMonthAgo);
                break;
            case THREEMONTH:
                startTime = String.valueOf(threeMonthsAgo);
                break;
            default:
                startTime = String.valueOf(oneYearAgo);
                break;
        }*/