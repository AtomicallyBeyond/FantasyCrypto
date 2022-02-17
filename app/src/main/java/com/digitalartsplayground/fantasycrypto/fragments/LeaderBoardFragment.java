package com.digitalartsplayground.fantasycrypto.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.digitalartsplayground.fantasycrypto.MainActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.LeaderBoardViewModel;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.leaderboard.ScoreSubmissionData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import org.jetbrains.annotations.NotNull;
import java.util.List;


public class LeaderBoardFragment extends Fragment {

    private static final String TAG = "GOOGLE_SIGN_IN";
    private LeaderBoardViewModel leaderBoardViewModel;
    private GoogleSignInClient googleSignInClient;
    private LeaderboardsClient leaderboardsClient;
    private ProgressBar progressBar;
    private float balance;
    private ImageButton googleButton;
    private TextView googleMessage;

    public LeaderBoardFragment() {}

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        leaderBoardViewModel = new ViewModelProvider(requireActivity())
                .get(LeaderBoardViewModel.class);
        balance = SharedPrefs.getInstance(requireContext()).getBalance();
        initGoogleClientSignIn();
    }


    private void initGoogleClientSignIn() {

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions);
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.leader_board_fragment, container, false);

        progressBar = view.findViewById(R.id.leaderboard_progress_bar);
        googleButton = view.findViewById(R.id.google_button);
        googleMessage = view.findViewById(R.id.google_message);

        return view;
    }


    @Override
    public void onStart() {

        super.onStart();

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());

        if(account == null) {
            loadGoogleAccount();
        } else {
            loadLeaderBoard(account);
        }
    }

    private void loadLeaderBoard(GoogleSignInAccount account) {

        leaderboardsClient = Games.getLeaderboardsClient(getActivity(), account);
        uploadPortfolioValue();
    }



    private void loadGoogleAccount() {

        progressBar.setVisibility(View.GONE);
        googleMessage.setVisibility(View.VISIBLE);
        googleButton.setVisibility(View.VISIBLE);

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                singInGoogle();
            }
        });
    }


    private void singInGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleActivityResultLauncher.launch(signInIntent);
    }


    private void uploadPortfolioValue(){

        AppExecutors.getInstance().diskIO().execute(new Runnable() {

            private float value = 0;
            private MarketUnit marketUnit;

            @Override
            public void run() {

                List<CryptoAsset> assets = leaderBoardViewModel.getAllAssets();

                if(assets != null) {

                    for(CryptoAsset asset :assets) {

                        marketUnit = leaderBoardViewModel.fetchMarketUnit(asset.getId());
                        if(marketUnit != null) {
                            value += marketUnit.getCurrentPrice() * asset.getAmount();
                        }
                    }
                }

                List<LimitOrder> buyActiveOrders = leaderBoardViewModel.fetchBuyOrders();
                for(LimitOrder limitOrder : buyActiveOrders) {
                    value += limitOrder.getValue();
                }

                List<LimitOrder> sellActiveOrders = leaderBoardViewModel.fetchSellOrders();
                for(LimitOrder limitOrder : sellActiveOrders) {
                    value += (leaderBoardViewModel.fetchMarketUnit(limitOrder.getCoinID()).getCurrentPrice()
                            * limitOrder.getAmount());
                }

                value += balance;

                long formattedValue = (long)(value * 1000000f);

                leaderboardsClient
                        .submitScoreImmediate(getString(R.string.leaderboard_fantasy_crypto), formattedValue)
                        .addOnSuccessListener(new OnSuccessListener<ScoreSubmissionData>() {
                            @Override
                            public void onSuccess(ScoreSubmissionData scoreSubmissionData) {

                                leaderboardsClient.getAllLeaderboardsIntent().addOnSuccessListener(new OnSuccessListener<Intent>() {
                                    @Override
                                    public void onSuccess(Intent intent) {
                                        startActivityForResults(intent);
                                    }
                                });
                            }
                        });

            }//end run
        });//end execute

    }

    private void showLoadingScreen(int visibility) {
        progressBar.setVisibility(visibility);
    }

    private void startActivityForResults(Intent intent) {

        showLoadingScreen(View.GONE);
        googleActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> googleActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        // The Task returned from this call is always completed, no need to attach
                        // a listener.
                        Task<GoogleSignInAccount> completedTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

                            // Signed in successfully, show authenticated UI.
                            loadLeaderBoard(account);

                        } catch (ApiException e) {
                            // The ApiException status code indicates the detailed failure reason.
                            // Please refer to the GoogleSignInStatusCodes class reference for more information.
                            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                        }
                    } else if(result.getResultCode() == Activity.RESULT_CANCELED) {
                        if(getActivity() != null)
                            ((MainActivity)getActivity()).setMarketTab();
                    }
                }
            });

}

/*        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions);
        googleSignInClient.silentSignIn().addOnCompleteListener(requireActivity(), new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<GoogleSignInAccount> task) {
                if(task.isSuccessful()) {

                    leaderboardsClient = Games.getLeaderboardsClient(getActivity(), task.getResult());

                    if(leaderboardsClient != null)
                        uploadPortfolioValue();

                } else {
                    String temp = task.getException().toString();
                    temp = temp + " ";
                }
            }
        });*/